package objective.taskboard.issueBuffer;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import objective.taskboard.auth.CredentialsHolder;
import objective.taskboard.auth.authorizer.permission.ProjectAdministrationPermission;
import objective.taskboard.data.Issue;
import objective.taskboard.data.ProjectsUpdateEvent;
import objective.taskboard.data.TaskboardIssue;
import objective.taskboard.data.Team;
import objective.taskboard.data.User;
import objective.taskboard.database.IssuePriorityService;
import objective.taskboard.domain.converter.JiraIssueToIssueConverter;
import objective.taskboard.domain.converter.ParentProvider;
import objective.taskboard.issue.CardStatusOrderCalculator;
import objective.taskboard.issue.IssueUpdate;
import objective.taskboard.issue.IssueUpdateType;
import objective.taskboard.issue.IssuesUpdateEvent;
import objective.taskboard.jira.FrontEndMessageException;
import objective.taskboard.jira.JiraIssueService;
import objective.taskboard.jira.JiraService;
import objective.taskboard.jira.ProjectService;
import objective.taskboard.jira.ProjectUpdateEvent;
import objective.taskboard.jira.RetrofitErrorParser;
import objective.taskboard.jira.client.JiraIssueDto;
import objective.taskboard.jira.data.Transition;
import objective.taskboard.jira.data.WebhookEvent;
import objective.taskboard.repository.TeamCachedRepository;
import objective.taskboard.task.IssueEventProcessScheduler;
import objective.taskboard.task.JiraEventProcessor;
import retrofit.RetrofitError;

@Service
public class IssueBufferService implements ApplicationListener<ProjectUpdateEvent> {
    private static final String CACHE_FILENAME = "issues.dat";
    public static final int THREAD_POOL_SIZE = 1;

    private static final Logger log = LoggerFactory.getLogger(IssueBufferService.class);

    @Autowired
    public ApplicationEventPublisher eventPublisher;

    @Autowired
    private JiraIssueToIssueConverter issueConverter;

    @Autowired
    private JiraIssueService jiraIssueService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private JiraService jiraBean;

    @Autowired
    private IssuePriorityService issuePriorityService;

    @Autowired
    private IssueEventProcessScheduler issueEvents;
    
    @Autowired
    private CardRepoService cardsRepoService;

    @Autowired
    private TeamCachedRepository teamRepo;

    @Autowired
    private CardStatusOrderCalculator statusOrderCalculator;

    @Autowired
    private ProjectAdministrationPermission projectAdministrationPermission;
    
    private CardRepo cardsRepo;
    
    private final ParentProvider parentProviderFetchesMissingParents = parentKey -> {
        Issue parent = cardsRepo.get(parentKey);
        if (parent == null)
            parent = updateIssueBufferFetchParentIfNeeded(jiraBean.getIssueByKeyAsMaster(parentKey));
        return Optional.of(parent);
    };

    private IssueBufferState state = IssueBufferState.uninitialised;

    
    private List<IssueUpdate> issuesUpdatedByEvent = new ArrayList<>();
    private Set<String> projectsUpdatedByEvent = new HashSet<>();

    @PostConstruct
    private void loadCache() {
        cardsRepo = cardsRepoService.from(CACHE_FILENAME);
        if (cardsRepo.size() > 0) {
            state = IssueBufferState.ready;
            return;
        }
        
        log.info("Card repo is not initialized. This might take some time..");
        updateIssueBuffer();
        log.info("Card repo initialized");
    }
    
    private synchronized void saveCache() {
        cardsRepo.commit();
    }

    public IssueBufferState getState() {
        return state;
    }

    public synchronized void updateIssueBuffer() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            updateState(state.start());

            IssueBufferServiceSearchVisitor visitor = new IssueBufferServiceSearchVisitor(issueConverter, this);
            jiraIssueService.searchAllProjectIssues(visitor, cardsRepo);

            log.info("Issue buffer - processed " + visitor.getProcessedCount() + " issues");

            updateState(state.done());
            saveCache();
        } catch(RequiresReindexException e) {
            updateState(IssueBufferState.requiresReindex);
            log.error("objective.taskboard.issueBuffer.IssueBufferService.updateIssueBuffer", e);
        } catch(RetrofitError e) {
            updateState(state.error());
            String message = RetrofitErrorParser.parseExceptionMessage(e);
            log.error("objective.taskboard.issueBuffer.IssueBufferService.updateIssueBuffer - Failed to bring issues - Errors:\n" + message, e);
        }catch(Exception e) {
            updateState(state.error());
            log.error("objective.taskboard.issueBuffer.IssueBufferService.updateIssueBuffer - Failed to bring issues", e);
        }
        finally {
            log.debug("updateIssueBuffer time spent " +stopWatch.getTime());
        }
    }

    private void updateState(IssueBufferState state) {
        this.state = state;
        if (state.isInitialized())
            eventPublisher.publishEvent(new IssueCacheUpdateEvent(this, state));
    }

    public synchronized Issue updateIssueBuffer(final String key) {
        Optional<JiraIssueDto> foundIssue =  jiraBean.getIssueByKey(key);
        if (!foundIssue.isPresent()) 
            return cardsRepo.remove(key);
        return updateIssueBufferFetchParentIfNeeded(foundIssue.get());
    }

    public synchronized Issue updateIssueBufferFetchParentIfNeeded(final JiraIssueDto jiraIssue) {
        final Issue issue = issueConverter.convertSingleIssue(jiraIssue, parentProviderFetchesMissingParents);
        putIssue(issue);

        return getIssueByKey(issue.getIssueKey());
    }

    private synchronized Issue putJiraIssue(JiraIssueDto jiraIssue) {
        final Issue issue = issueConverter.convertSingleIssue(jiraIssue, parentProviderFetchesMissingParents);
        putIssue(issue);

        return getIssueByKey(issue.getIssueKey());
    }

    public synchronized void notifyProjectUpdate(final String projectKey) {
        projectsUpdatedByEvent.add(projectKey);
    }

    @Override
    public void onApplicationEvent(ProjectUpdateEvent event) {
        notifyProjectConfigurationUpdated(event.getProjectKey());
    }

    private synchronized void notifyProjectConfigurationUpdated(final String projectKey) {
        this.startBatchUpdate();
        this.getAllIssues().stream()
                .filter(i -> i.getProjectKey().equals(projectKey))
                .forEach(i -> notifyIssueUpdate(i));
        this.finishBatchUpdate();
    }

    public synchronized void notifyIssueUpdate(final Issue issue) {
        issuesUpdatedByEvent.add(new IssueUpdate(issue, IssueUpdateType.UPDATED));
    }

    public synchronized void updateByEvent(WebhookEvent event, final String issueKey, Optional<JiraIssueDto> issue) {
        Issue issueBeforeUpdate = cardsRepo.get(issueKey);
        updateCardFromWebhookEvent(event, issueKey, issue);
        Issue issueAfterUpdate = cardsRepo.get(issueKey);
        boolean issueDidntExistBefore = issueBeforeUpdate == null;
        if (issueDidntExistBefore)
            // created
            scheduleNotificationsForLinkedIssues(issueAfterUpdate);
        else
        if (issueAfterUpdate == null)
            // removed
            scheduleNotificationsForLinkedIssues(issueBeforeUpdate);
        else
            // updated
            scheduleNotificationsForLinkedIssues(issueAfterUpdate);
    }

    private synchronized void updateCardFromWebhookEvent(WebhookEvent event, final String issueKey, Optional<JiraIssueDto> issue) {
        if (event == WebhookEvent.ISSUE_DELETED) {
            issuesUpdatedByEvent.add(new IssueUpdate(cardsRepo.get(issueKey), IssueUpdateType.DELETED));
            cardsRepo.remove(issueKey);
            return;
        }
        if (!issue.isPresent())
            return;

        Issue updated = putJiraIssue(issue.get());

        IssueUpdateType updateType = IssueUpdateType.UPDATED;
        if (event == WebhookEvent.ISSUE_CREATED)
            updateType = IssueUpdateType.CREATED;

        issuesUpdatedByEvent.add(new IssueUpdate(updated, updateType));
    }

    public synchronized void startBatchUpdate() {
        issuesUpdatedByEvent.clear();
        projectsUpdatedByEvent.clear();
    }

    public synchronized void finishBatchUpdate() {
        saveCache();
        
        if (issuesUpdatedByEvent.size() > 0)
            eventPublisher.publishEvent(new IssuesUpdateEvent(this, issuesUpdatedByEvent));
        issuesUpdatedByEvent.clear();

        if (projectsUpdatedByEvent.size() > 0)
            eventPublisher.publishEvent(new ProjectsUpdateEvent(this, projectsUpdatedByEvent.toArray(new String[0])));
        projectsUpdatedByEvent.clear();
    }

    public synchronized List<Issue> getVisibleIssues() {
        return cardsRepo.values().stream()
                .filter(this::isAccessible)
                .collect(Collectors.toList());
    }

    public synchronized List<Issue> getVisibleIssuesByIds(List<Long> issuesIds) {
        return issuesIds.stream()
                .map(id->cardsRepo.getById(id))
                .filter(this::isAccessible)
                .collect(Collectors.toList());
    }

    private boolean isAccessible(Issue t) {
        if (t == null)
            return false;
        return projectService.isNonArchivedAndUserHasAccess(t.getProjectKey()) && t.isVisible();
    }
    
    public synchronized List<Issue> getAllIssues() {
        return cardsRepo.values().stream().collect(toList());
    }

    public synchronized Issue getIssueByKey(String key) {
        return cardsRepo.get(key);
    }

    public synchronized Optional<Issue> getIssueByKey(String key, boolean onlyVisible) {
        Issue issue = cardsRepo.get(key);
        if(onlyVisible && this.isAccessible(issue)){
            return Optional.ofNullable(issue);
        }else if(issue != null && projectService.isNonArchivedAndUserHasAccess(issue.getProjectKey())){
            return Optional.ofNullable(issue);
        }
        return Optional.empty();
    }

    public synchronized List<Transition> transitions(String issueKey) {
        Issue issue = getIssueByKey(issueKey);
        List<Transition> transitions = jiraBean.getTransitions(issueKey);
        
        transitions.forEach(t -> t.order = (long) statusOrderCalculator.computeStatusOrder(issue.getType(), t.to.id) );
        return transitions;
    }

    public synchronized Issue addMeAsAssignee(String issueKey) {
        return addAssigneeToIssue(issueKey, CredentialsHolder.username());
    }
    
    public synchronized Issue addAssigneeToIssue(String issueKey, String username) {
        Issue issue = getIssueCopyByKeyOrCry(issueKey);
        if (issue.getAssignees().stream().map(a -> a.name).anyMatch(name -> name.equals(username)))
            return issue;
        
        LinkedList<User> assigneeList = new LinkedList<>(issue.getAssignees());
        assigneeList.add(new User(username));

        if (jiraBean.assignToUsers(issue.getIssueKey(), assigneeList))
            return updateIssueBuffer(issueKey);

        return issue;
    }

    public Issue removeAssigneeFromIssue(String issueKey, String username) {
        Issue issue = getIssueCopyByKeyOrCry(issueKey);
        
        List<User> assignees =
                issue.getAssignees().stream()
                .collect(Collectors.toList());
        
        Iterator<User> it = assignees.iterator();
        while(it.hasNext()) {
            User anAssignee = it.next();
            if (anAssignee.isAssigned() && anAssignee.name.equals(username))
                it.remove();
        }

        if (jiraBean.assignToUsers(issue.getIssueKey(), assignees))
            return updateIssueBuffer(issueKey);

        return issue;
    }
    
    public synchronized Issue addTeamToIssue(String issueKey, Long teamId) {
        Issue issue = getIssueCopyByKeyOrCry(issueKey);
        Team teamToAdd = getTeamByIdOrCry(teamId);
        issue.addTeam(teamToAdd);
        return syncIssueTeams(issueKey, issue);
    }

    public synchronized Issue removeTeamFromIssue(String issueKey, Long teamToReplace) {
        Issue issue = getIssueCopyByKeyOrCry(issueKey);
        Team teamToRemove = getTeamByIdOrCry(teamToReplace);
        issue.removeTeam(teamToRemove);
        return syncIssueTeams(issueKey, issue);
    }

    public synchronized Issue replaceTeamInIssue(String issueKey, Long previousTeam, Long replacementTeam) {
        Issue issue = getIssueCopyByKeyOrCry(issueKey);
        Optional<Team> teamToRemove = teamRepo.findById(previousTeam);
        Team teamToAdd = getTeamByIdOrCry(replacementTeam);
        issue.replaceTeam(teamToRemove, teamToAdd);
        return syncIssueTeams(issueKey, issue);
    }

    public Issue restoreDefaultTeams(String issueKey) {
        Issue issue = getIssueCopyByKeyOrCry(issueKey);
        issue.restoreDefaultTeams();
        return syncIssueTeams(issueKey, issue);
    }

    public Issue saveDescription(String issueKey, String description) {
       jiraBean.saveDescription(issueKey, description);

        Issue issue = getIssueCopyByKeyOrCry(issueKey);
        
        if (!issue.getDescription().equals(description)) {
            issue.setDescription(description);
            issue.setRemoteIssueUpdatedDate(new Date());
            putIssue(issue);
        }
        
        return issue;
    }

    public Issue saveClassOfService(String issueKey, String classOfService) {
        jiraBean.saveClassOfService(issueKey, classOfService);

        Issue issue = getIssueCopyByKeyOrCry(issueKey);

        if (!issue.getClassOfServiceValue().equals(classOfService)) {
            issue.setClassOfServiceValue(classOfService);
            issue.setRemoteIssueUpdatedDate(new Date());
            putIssue(issue);
        }

        return issue;
    }

    private Issue syncIssueTeams(String issueKey, Issue issue) {
        jiraBean.setTeams(issue.getIssueKey(),issue.getRawAssignedTeamsIds());
        return updateIssueBuffer(issueKey);
    }

    private Issue getIssueCopyByKeyOrCry(String issueKey) {
        Issue issue = getIssueByKey(issueKey);
        if (issue == null)
            throw new IssueNotFoundException(issueKey);

        return issue.copy();
    }

    private Team getTeamByIdOrCry(Long teamId) {
        Optional<Team> teamToAdd = teamRepo.findById(teamId);
        if (!teamToAdd.isPresent())
            throw new IllegalArgumentException("Team id " + teamId + " not found");
        return teamToAdd.get();
    }

    public Issue doTransition(String issueKey, Long transitionId, Map<String, Object> fields) {
        jiraBean.doTransition(issueKey, transitionId, fields);
        return updateIssueBuffer(issueKey);
    }

    private void putIssue(Issue issue) {
        cardsRepo.putOnlyIfNewer(issue);
    }

    public synchronized List<Issue> reorder(String[] issues) {
        ReorderIssuesAction action = new ReorderIssuesAction(issues);
        return action.call();
    }
    
    private synchronized void updateIssuesPriorities(List<TaskboardIssue> issuesPriorities) {
        for (TaskboardIssue taskboardIssue : issuesPriorities) { 
            Issue issue = cardsRepo.get(taskboardIssue.getIssueKey());
            issueEvents.add(new InternalUpdateIssue(issue.getProjectKey(), issue.getIssueKey()));
        }
    }

    /**
     * Update issue in buffer and notify the taskboard
     * @param issue to update
     * @return if issue is updated in buffer and notified the taskboard successfully
     */
    public synchronized boolean updateIssue(Issue issue) {
        if (!cardsRepo.putOnlyIfNewer(issue))
            return false;
        issueEvents.add(new InternalUpdateIssue(issue.getProjectKey(), issue.getIssueKey()));
        scheduleNotificationsForLinkedIssues(issue);
        return true;
    }

    private void scheduleNotificationsForLinkedIssues(Issue issue) {
        if (issue == null)
            return;
        scheduleNotificationForParent(issue);
        scheduleNotificationForSubtasks(issue);
    }

    private synchronized void scheduleNotificationForParent(Issue theIssue) {
        if (theIssue.getParent() == null)
            return;
        Optional<Issue> parentCard = Optional.ofNullable(getIssueByKey(theIssue.getParent()));
        if (!parentCard.isPresent())
            return;

        issuesUpdatedByEvent.add(new IssueUpdate(parentCard.get(), IssueUpdateType.UPDATED));
        scheduleNotificationForParent(parentCard.get());
    }

    private synchronized void scheduleNotificationForSubtasks(Issue theIssue) {
        for (Issue subtask : theIssue.getSubtaskCards()) {
            issueEvents.add(new InternalUpdateIssue(subtask.getProjectKey(), subtask.getIssueKey()));
            scheduleNotificationForSubtasks(subtask);
        }
    }

    private class InternalUpdateIssue implements JiraEventProcessor {

        private final String projectKey;
        private final String issueKey;

        public InternalUpdateIssue(String projectKey, String issueKey) {
            this.projectKey = projectKey;
            this.issueKey = issueKey;
        }

        @Override
        public String getDescription() {
            return "InternalUpdateIssue - projectKey: " + projectKey + " issue: " + issueKey;
        }

        @Override
        public void processEvent() {
            issuesUpdatedByEvent.add(new IssueUpdate(cardsRepo.get(issueKey), IssueUpdateType.UPDATED));
        }
    }
    
    public synchronized void reset() {
        state = IssueBufferState.uninitialised;
        cardsRepo.clear();
        updateIssueBuffer();
    }

    public class ReorderIssuesAction implements Callable<List<Issue>> {

        private final String[] issueKeys;

        public ReorderIssuesAction(String[] issueKeys) {
            this.issueKeys = issueKeys;
        }

        @Override
        public List<Issue> call() {
            validateUserPermissions();

            List<TaskboardIssue> updated = issuePriorityService.reorder(issueKeys);
            updateIssuesPriorities(updated);

            List<Issue> updatedIssues = new LinkedList<>();
            for (String issue : issueKeys)
                updatedIssues.add(getIssueByKey(issue));

            return updatedIssues;
        }

        private void validateUserPermissions() {
            Set<String> projectsWithoutPermission = Arrays.stream(issueKeys)
                    .map(IssueBufferService.this::getIssueByKey)
                    .map(Issue::getProjectKey)
                    .distinct()
                    .filter(projectKey -> !projectAdministrationPermission.isAuthorizedFor(projectKey))
                    .collect(Collectors.toSet());
            if(!projectsWithoutPermission.isEmpty()) {
                throw new FrontEndMessageException("User doesn't have permission to reoder issues of " + projectsWithoutPermission);
            }
        }
    }
    
    public class IssueNotFoundException extends RuntimeException {
        public IssueNotFoundException(String issueKey) {
            super("Issue with key " + issueKey + " not found");
        }

        private static final long serialVersionUID = 1L;
    }
}