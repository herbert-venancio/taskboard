/*-
 * [LICENSE]
 * Taskboard
 * - - -
 * Copyright (C) 2015 - 2016 Objective Solutions
 * - - -
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * [/LICENSE]
 */
package objective.taskboard.issueBuffer;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import objective.taskboard.data.Issue;
import objective.taskboard.data.IssuePriorityOrderChanged;
import objective.taskboard.data.TaskboardIssue;
import objective.taskboard.database.IssuePriorityService;
import objective.taskboard.domain.converter.JiraIssueToIssueConverter;
import objective.taskboard.domain.converter.ParentProvider;
import objective.taskboard.issue.IssueUpdate;
import objective.taskboard.issue.IssueUpdateType;
import objective.taskboard.issue.IssuesUpdateEvent;
import objective.taskboard.jira.JiraIssueService;
import objective.taskboard.jira.JiraService;
import objective.taskboard.jira.ProjectService;
import objective.taskboard.task.IssueEventProcessScheduler;

@Service
public class IssueBufferService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IssueBufferService.class);

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
    
    private Map<String, Issue> issueBuffer = new LinkedHashMap<>();
    
    private final ParentProvider parentProviderFetchesMissingParents = parentKey -> {
        Issue parent = issueBuffer.get(parentKey);
        if (parent == null)
            parent = updateIssueBufferFetchParentIfNeeded(jiraBean.getIssueByKeyAsMaster(parentKey));
        return Optional.of(parent);
    };
    
    private final ParentProvider parentProviderRejectsIfMissingParent = parentKey ->  {
        Issue parent = issueBuffer.get(parentKey);
        if (parent == null)
            throw new IllegalArgumentException("Parent issue " + parentKey + " not available. This is probably a bug.");
        return Optional.of(parent);
    };   

    private IssueBufferState state = IssueBufferState.uninitialised;


    private boolean isUpdatingTaskboardIssuesBuffer = false;

    private List<IssueUpdate> issuesUpdatedByEvent = new ArrayList<>();

    public IssueBufferState getState() {
        return state;
    }

    public synchronized void updateIssueBuffer() {
        if (isUpdatingTaskboardIssuesBuffer)
            return;
        
        isUpdatingTaskboardIssuesBuffer = true;
        Thread thread = new Thread(() -> {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            try {
                updateState(state.start());
                
                IssueBufferServiceSearchVisitor visitor = new IssueBufferServiceSearchVisitor(issueConverter);
                jiraIssueService.searchAllWithParents(visitor);
                
                Map<String, Issue> newIssueBuffer = visitor.getIssuesByKey();
                log.info("Issue buffer service - processed " + newIssueBuffer.size()+ " issues");
                setIssues(newIssueBuffer);
                
                updateState(state.done());
            }catch(Exception e) {
                updateState(state.error());
                log.error("objective.taskboard.issueBuffer.IssueBufferService.updateIssueBuffer - Failed to bring issues", e);
            }
            finally {
                log.debug("updateIssueBuffer time spent " +stopWatch.getTime());
                isUpdatingTaskboardIssuesBuffer = false;
            }
        });
        thread.setName("Buffer.update");
        thread.setDaemon(true);
        thread.start();
    }

    private void updateState(IssueBufferState state) {
        this.state = state;
        eventPublisher.publishEvent(new IssueCacheUpdateEvent(this, state));
    }

    public Issue updateIssueBuffer(final String key) {
        Optional<com.atlassian.jira.rest.client.api.domain.Issue> foundIssue = jiraIssueService.searchIssueByKey(key);
        if (!foundIssue.isPresent()) 
            return issueBuffer.remove(key);
       return updateIssueBufferFetchParentIfNeeded(foundIssue.get());
    }

    public synchronized Issue updateIssueBufferFetchParentIfNeeded(final com.atlassian.jira.rest.client.api.domain.Issue jiraIssue) {
        final Issue issue = issueConverter.convertSingleIssue(jiraIssue, parentProviderFetchesMissingParents);
        putIssue(issue);

        updateSubtasks(issue.getIssueKey());

        return issue;
    }

    public synchronized Issue updateByEvent(WebhookEvent event, final String key, com.atlassian.jira.rest.client.api.domain.Issue jiraIssue) {
        if (event == WebhookEvent.ISSUE_DELETED || jiraIssue == null) {
            issuesUpdatedByEvent.add(new IssueUpdate(issueBuffer.get(key), IssueUpdateType.DELETED));
            return issueBuffer.remove(key);
        }

        Issue updated = updateIssueBufferFetchParentIfNeeded(jiraIssue);

        IssueUpdateType updateType = IssueUpdateType.UPDATED;
        if (event == WebhookEvent.ISSUE_CREATED)
            updateType = IssueUpdateType.CREATED;

        issuesUpdatedByEvent.add(new IssueUpdate(updated, updateType));

        return updated;
    }

    public synchronized void startBatchUpdate() {
        issuesUpdatedByEvent.clear();
    }

    public synchronized void finishBatchUpdate() {
        if (issuesUpdatedByEvent.size() > 0)
            eventPublisher.publishEvent(new IssuesUpdateEvent(this, issuesUpdatedByEvent));
        issuesUpdatedByEvent.clear();
    }

    private synchronized void updateSubtasks(String key) {
        List<String> subtasksKeys = getSubtasksKeys(key);
        jiraIssueService.searchIssuesByKeys(subtasksKeys, 
            issue -> putIssue(issueConverter.convertSingleIssue(issue, parentProviderRejectsIfMissingParent))                   
        );
    }

    private List<String> getSubtasksKeys(String key) {
        List<String> subtasksKeys = issueBuffer.values().stream()
            .filter(i -> key.equals(i.getParent()))
            .map(i -> i.getIssueKey())
            .collect(toList());

        if (subtasksKeys.isEmpty())
            return newArrayList();

        List<String> allSubtasksKeys = newArrayList(subtasksKeys);
        for (String subtaskKey : subtasksKeys)
            allSubtasksKeys.addAll(getSubtasksKeys(subtaskKey));

        return allSubtasksKeys;
    }

    public synchronized List<Issue> getIssues() {
        List<Issue> collect = issueBuffer.values().stream()
                .filter(t -> projectService.isProjectVisible(t.getProjectKey()))
                .filter(t -> !t.isDeferred())
                .collect(toList());
        return collect;
    }

    public synchronized Issue getIssueByKey(String key) {
        return issueBuffer.get(key);
    }

    public List<objective.taskboard.data.Issue> getIssueSubTasks(objective.taskboard.data.Issue issue) {
        List<Issue> subtasks= new ArrayList<>();
        jiraIssueService.searchIssueSubTasksAndDemandedByKey(issue.getIssueKey(), 
            jiraIssue -> subtasks.add(issueConverter.convertSingleIssue(jiraIssue, parentProviderRejectsIfMissingParent)));
        return subtasks;
    }

    public synchronized Issue toggleAssignAndSubresponsavelToUser(String issueKey) {
        jiraBean.toggleAssignAndSubresponsavelToUser(issueKey);
        return updateIssueBuffer(issueKey);
    }

    public synchronized Issue doTransitionByName(Issue issue, String transition, String resolution) {
        jiraBean.doTransitionByName(issue, transition, resolution);
        updateIssueBuffer(issue.getIssueKey());
        return getIssueByKey(issue.getIssueKey());
    }

    public synchronized void updateIssuesPriorities(List<TaskboardIssue> issuesPriorities) {
        for (TaskboardIssue taskboardIssue : issuesPriorities) { 
            Issue issue = issueBuffer.get(taskboardIssue.getProjectKey());
            issue.setPriorityOrder(taskboardIssue.getPriority());
            issue.setUpdatedDate(taskboardIssue.getUpdated());
            issueEvents.add(WebhookEvent.ISSUE_UPDATED, issue.getIssueKey(), null);
        }
    }

    private synchronized void setIssues(Map<String, Issue> issues) {
        issueBuffer = issues;
    }

    private void putIssue(Issue issue) {
        issueBuffer.put(issue.getIssueKey(), issue);
    }

    @EventListener
    protected void onAfterSave(IssuePriorityOrderChanged event) {
        TaskboardIssue entity = event.getTarget();
        issueBuffer.get(entity.getProjectKey()).setPriorityOrder(entity.getPriority());
    }

    public void reset() {
        state = IssueBufferState.uninitialised;
        updateIssueBuffer();
        while (isUpdatingTaskboardIssuesBuffer)
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                return;
            }
    }

    public synchronized List<Issue> reorder(String[] issues) {
        List<TaskboardIssue> updated = issuePriorityService.reorder(issues);
        updateIssuesPriorities(updated);
        
        List<Issue> updatedIssues = new LinkedList<Issue>();
        for (String issue : issues) 
            updatedIssues.add(this.getIssueByKey(issue));
        
        return updatedIssues;
    }

}
