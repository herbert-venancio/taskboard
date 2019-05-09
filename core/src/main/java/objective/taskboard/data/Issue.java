package objective.taskboard.data;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.SerializationUtils;

import objective.taskboard.config.SpringContextBridge;
import objective.taskboard.cycletime.CycleTime;
import objective.taskboard.database.IssuePriorityService;
import objective.taskboard.domain.IssueColorService;
import objective.taskboard.domain.converter.CardVisibilityEvalService;
import objective.taskboard.domain.converter.IssueTeamService;
import objective.taskboard.domain.converter.IssueTeamService.TeamOrigin;
import objective.taskboard.filter.LaneService;
import objective.taskboard.jira.MetadataService;
import objective.taskboard.jira.ProjectService;
import objective.taskboard.jira.data.Version;
import objective.taskboard.jira.properties.JiraProperties;
import objective.taskboard.jira.properties.JiraProperties.BallparkMapping;
import objective.taskboard.utils.DateTimeUtils;

public class Issue extends IssueScratch implements Serializable {
    private static final long serialVersionUID = 8513934402068368820L;

    private transient Issue parentCard;

    private transient Set<Issue> subtasks = new LinkedHashSet<>();

    private transient JiraProperties jiraProperties;

    private transient MetadataService metaDataService;

    private transient IssueTeamService issueTeamService;

    private transient LaneService laneService;

    private transient CardVisibilityEvalService cardVisibilityEvalService;

    private transient ProjectService projectService;

    private transient IssueColorService issueColorService;

    private transient CycleTime cycleTime;

    private transient IssuePriorityService issuePriorityService;

    public Issue(IssueScratch scratch,
            JiraProperties properties, 
            MetadataService metadataService, 
            IssueTeamService issueTeamService, 
            LaneService laneService,
            CycleTime cycleTime,
            CardVisibilityEvalService cardVisibilityEvalService,
            ProjectService projectService,
            IssueColorService issueColorService,
            IssuePriorityService issuePriorityService) {
        this.id = scratch.id;
        this.issueKey = scratch.issueKey;
        this.projectKey = scratch.projectKey;
        this.project = scratch.project;
        this.type = scratch.type;
        this.summary = scratch.summary;
        this.status = scratch.status;
        this.startDateStepMillis = scratch.startDateStepMillis;
        this.parent = scratch.parent;
        this.dependencies = scratch.dependencies;
        this.bugs = scratch.bugs;
        this.assignee = scratch.assignee;
        this.priority = scratch.priority;
        this.dueDate = scratch.dueDate;
        this.created = scratch.created;
        this.description = scratch.description;
        this.comments = scratch.comments;
        this.labels = scratch.labels;
        this.components = scratch.components;
        this.blocked = scratch.blocked;
        this.shouldBlockAllSubtasks = scratch.shouldBlockAllSubtasks;
        this.lastBlockReason = scratch.lastBlockReason;
        this.tshirtSizes = scratch.tshirtSizes;
        this.additionalEstimatedHours = scratch.additionalEstimatedHours;
        this.timeTracking = scratch.timeTracking;
        this.reporter = scratch.reporter;
        this.classOfService = scratch.classOfService;
        this.releaseId = scratch.releaseId;
        this.changelog = scratch.changelog;
        this.remoteIssueUpdatedDate = scratch.remoteIssueUpdatedDate;
        this.coAssignees = scratch.coAssignees;
        this.fixVersions = scratch.fixVersions;
        this.metaDataService = metadataService;
        this.jiraProperties = properties;
        this.issueTeamService = issueTeamService;
        this.laneService = laneService;
        this.cycleTime = cycleTime;
        this.cardVisibilityEvalService = cardVisibilityEvalService;
        this.projectService = projectService;
        this.issueColorService = issueColorService;
        this.issuePriorityService = issuePriorityService;
        this.worklogs = scratch.worklogs;
        this.assignedTeamsIds = scratch.assignedTeamsIds;

        this.extraFields = scratch.extraFields;
    }
    
    public Issue() {
        jiraProperties = SpringContextBridge.getBean(JiraProperties.class);
        metaDataService = SpringContextBridge.getBean(MetadataService.class);
        issueTeamService = SpringContextBridge.getBean(IssueTeamService.class);
        laneService = SpringContextBridge.getBean(LaneService.class);
        cycleTime = SpringContextBridge.getBean(CycleTime.class);
        cardVisibilityEvalService = SpringContextBridge.getBean(CardVisibilityEvalService.class);
        projectService = SpringContextBridge.getBean(ProjectService.class);
        issueColorService = SpringContextBridge.getBean(IssueColorService.class);
        issuePriorityService = SpringContextBridge.getBean(IssuePriorityService.class);
    }

    public boolean isDeferred() {
        boolean isDeferredStatus = jiraProperties.getStatusesDeferredIds().contains(this.getStatus());
        if (!isDeferredStatus && parentCard != null)
            return parentCard.isDeferred();
        return isDeferredStatus;
    }

    public String getTShirtSize() {
        String mainTShirtSizeFieldId = jiraProperties.getCustomfield().getTShirtSize().getMainTShirtSizeFieldId();
        return Optional.ofNullable(tshirtSizes.get(mainTShirtSizeFieldId))
                .flatMap(customField -> Optional.ofNullable((String) customField.getValue()))
                .orElse(null);
    }

    public String getTshirtSizeOfSubtaskForBallpark(BallparkMapping mapping) {
        return Optional.ofNullable(tshirtSizes.get(mapping.getTshirtCustomFieldId()))
                .flatMap(customField -> Optional.ofNullable((String) customField.getValue()))
                .orElse(null);
    }

    public List<BallparkMapping> getActiveBallparkMappings() {
        List<BallparkMapping> list = jiraProperties.getFollowup().getBallparkMappings().get(getType());
        if (list == null)
            return null;

        return list.stream().filter(bm -> getTshirtSizeOfSubtaskForBallpark(bm)!=null).collect(Collectors.toList());
    }
    
    public String getParentSummary() {
        if (this.parentCard != null)
            return this.parentCard.getSummary();
        return "";
    }

    public String getColor() {
        return issueColorService.getColor(getClassOfServiceId());
    }

    public Set<String> getMismatchingUsers() {
        return issueTeamService.getMismatchingUsers(this);
    }

    public Set<CardTeam> getTeams() {
        return issueTeamService.resolveTeams(this);
    }

    /**
     * Returns the value of assigned teams id in jira field. The default team will never be returned here.
     * 
     * @return the list of team id values in the jira issue. Prefer using getTeams to find the *actual* teams. 
     */
    public List<Long> getRawAssignedTeamsIds() {
        return assignedTeamsIds;
    }

    public boolean isUsingDefaultTeam() {
        return issueTeamService.resolveTeamsOrigin(this) == TeamOrigin.DEFAULT_BY_PROJECT;
    }

    public boolean isUsingTeamByIssueType() {
        return issueTeamService.resolveTeamsOrigin(this) == TeamOrigin.DEFAULT_BY_ISSUE_TYPE;
    }

    public boolean isUsingParentTeam() {
        return issueTeamService.resolveTeamsOrigin(this) == TeamOrigin.INHERITED;
    }

    public void setParentCard(Issue parentCard) {
        this.parentCard = parentCard;
        if (parentCard != null)
            parentCard.addsubtask(this);
    }

    private void addsubtask(Issue issue) {
        this.subtasks.remove(issue);
        this.subtasks.add(issue);
    }

    public void unlinkParent() {
        if(parentCard != null)
            parentCard.subtasks.remove(this);
        parentCard = null;
    }

    public Optional<Issue> getParentCard() {
        return Optional.ofNullable(parentCard);
    }

    public String getClassOfServiceValue() {
        String defaultClassOfService = jiraProperties.getCustomfield().getClassOfService().getDefaultValue();
        CustomField classOfService = getClassOfServiceCustomField();
        return classOfService == null ? defaultClassOfService : (String)classOfService.getValue();
    }

    public String getClassOfServiceFieldId() {
        return jiraProperties.getCustomfield().getClassOfService().getId();
    }

    private Long getClassOfServiceId() {
        CustomField classOfService = getClassOfServiceCustomField();
        return classOfService == null ? 0L : classOfService.getOptionId();
    }

    private CustomField getClassOfServiceCustomField() {
        String defaultClassOfService = jiraProperties.getCustomfield().getClassOfService().getDefaultValue();
        CustomField classOfService = getLocalClassOfServiceCustomField();

        boolean isNotDefaultClassOfService = classOfService != null
                                             && classOfService.getValue() != null
                                             && !classOfService.getValue().toString().equals(defaultClassOfService);
        if (isNotDefaultClassOfService)
            return classOfService;

        Optional<Issue> pc = getParentCard();
        if (pc.isPresent())
            return pc.get().getClassOfServiceCustomField();

        return classOfService;
    }

    public void setClassOfServiceValue(final String classOfService) {
        this.classOfService.setValue(classOfService);
    }

    public List<Changelog> getChangelog() {
        return changelog;
    }

    public boolean isDemand() {
        return jiraProperties.getIssuetype().getDemand().getId() == this.getType();
    }

    public boolean isFeature() {
        return jiraProperties.getIssuetype().getFeatures().stream().anyMatch(ft -> ft.getId() == this.getType());
    }

    public boolean isSubTask() {
        return jiraProperties.getIssuetype().getSubtasks().stream().anyMatch(ft -> ft.getId() == this.getType());
    }

    public Integer getIssueKeyNum() {
        return Integer.parseInt(issueKey.replace(projectKey+"-", ""));
    }

    public String getIssueTypeName() {
        return metaDataService.getIssueTypeById(type).getName();
    }

    public String getIssueTypeNameAsLoggedInUser() {
        return metaDataService.getIssueTypeByIdAsLoggedInUser(type).getName();
    }

    public String getStatusName() {
        return metaDataService.getStatusById(status).name;
    }

    public String getStatusNameAsLoggedInUser() {
        return metaDataService.getStatusByIdAsLoggedInUser(status).name;
    }

    public Integer getStatusPriority() {
        Integer r;
        if (this.isDemand())
            r = jiraProperties.getStatusPriorityOrder().getDemandPriorityByStatus(this.getStatusName());
        else
        if (this.isFeature())
            r = jiraProperties.getStatusPriorityOrder().getTaskPriorityByStatus(this.getStatusName());
        else
            r = jiraProperties.getStatusPriorityOrder().getSubtaskPriorityByStatus(this.getStatusName());
        
        if (r == null) r = 0;
        return r;
    }

    public Optional<Double> getCycleTime(ZoneId timezone) {
        return cycleTime.getCycleTime(Instant.ofEpochMilli(startDateStepMillis), timezone, status);
    }
    
    public CustomField getAdditionalEstimatedHoursField() {
        return additionalEstimatedHours;
    }

    public Double getAdditionalEstimatedHours() {
        CustomField additionalEstimatedHours = getAdditionalEstimatedHoursField();
        if (additionalEstimatedHours != null)
            return (Double) additionalEstimatedHours.getValue();
        return null;
    }
    
    public boolean isCancelled() {
        return jiraProperties.getStatusesCanceledIds().stream().anyMatch(s -> s.equals(status));
    }
    
    public boolean isCompleted() {
        return jiraProperties.getStatusesCompletedIds().stream().anyMatch(s -> s.equals(status));
    }    

    public boolean isBlocked() {
        return blocked;
    }

    public boolean shouldBlockAllSubtasks() {

        if (shouldBlockAllSubtasks || parentCard == null )
            return shouldBlockAllSubtasks;
        return parentCard.shouldBlockAllSubtasks();
    }

    public boolean isBlockedByParent() {

        if (parentCard == null)
            return false;

        return (parentCard.isBlocked() || parentCard.isBlockedByParent())
                    && shouldBlockAllSubtasks();
    }

    public String getLastBlockReason() {
        return lastBlockReason;
    }
    
    public List<CustomField> getBallparks() {
        List<BallparkMapping> list = jiraProperties.getFollowup().getBallparkMappings().get(getType());

        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        } else {
            return list.stream()
                .map(obj -> makeTshirtField(obj.getTshirtCustomFieldId()))
                .collect(Collectors.toList());
        }
    }

    private CustomField makeTshirtField(String fieldId) {
        if (tshirtSizes.containsKey(fieldId)) {
            return tshirtSizes.get(fieldId);
        } else {
            return new CustomField(fieldId, null);
        }
    }

    public String getCardTshirtSize() {
        if (tshirtSizes.containsKey(jiraProperties.getCustomfield().getTShirtSize().getMainTShirtSizeFieldId()))
            return ""+tshirtSizes.get(jiraProperties.getCustomfield().getTShirtSize().getMainTShirtSizeFieldId()).getValue();

        return "";
    }

    public String getCardTshirtSizeFieldId() {
        return jiraProperties.getCustomfield().getTShirtSize().getMainTShirtSizeFieldId();
    }

    public Long getId() {
        return this.id;
    }

    public String getProjectKey() {
        return this.projectKey;
    }

    public String getProject() {
        return this.project;
    }

    public long getType() {
        return this.type;
    }

    public String getTypeIconUri() {
        return metaDataService.getIssueTypeById(type).getIconUri().toASCIIString();
    }

    public String getSummary() {
        return this.summary;
    }

    public long getStatus() {
        return this.status;
    }

    public long getStartDateStepMillis() {
        return this.startDateStepMillis;
    }

    public String getParent() {
        return this.parent;
    }

    public long getParentType() {
        return getParentCard()
                .map(parent -> parent.type)
                .orElse(0L);
    }

    public String getParentTypeIconUri() {
        return getParentCard()
                .map(Issue::getTypeIconUri)
                .orElse("");
    }

    public List<String> getDependencies() {
        return this.dependencies;
    }

    public List<String> getBugs() {
        return bugs;
    }

    public List<User> getCoAssignees() {
        return this.coAssignees;
    }

    public List<Version> getFixVersions() {
        return fixVersions;
    }
    
    public List<User> getAssignees() {
        LinkedList<User> assigneeSet = new LinkedList<>();
        if (getAssignee().isAssigned())
            assigneeSet.add(getAssignee());
        assigneeSet.addAll(getCoAssignees());
        return assigneeSet;
    }

    public User getAssignee() {
        return this.assignee;
    }
    
    public long getPriority() {
        return this.priority;
    }

    public Date getDueDate() {
        return this.dueDate;
    }

    public ZonedDateTime getDueDateByTimezoneId(ZoneId timezone) {
        return DateTimeUtils.get(dueDate, timezone);
    }

    public Date getUpdatedDate() {
        Date priorityUpdatedDate = getPriorityUpdatedDate();

        if (remoteIssueUpdatedDate == null
                || priorityUpdatedDate.after(remoteIssueUpdatedDate))
            return priorityUpdatedDate;
        
        return remoteIssueUpdatedDate;
    }

    public ZonedDateTime getUpdatedDateByTimezoneId(ZoneId timezone) {
        return DateTimeUtils.get(getUpdatedDate(), timezone);
    }

    public long getCreated() {
        return this.created;
    }

    public ZonedDateTime getCreatedDateByTimezoneId(ZoneId timezone) {
        return DateTimeUtils.get(this.created, timezone);
    }

    public String getDescription() {
        return this.description;
    }

    public List<Comment> getComments() {
        return this.comments;
    }

    public List<String> getLabels() {
        return this.labels;
    }

    public List<String> getComponents() {
        return this.components;
    }

    public long getPriorityOrder() {
        return issuePriorityService.determinePriority(this);
    }

    public TaskboardTimeTracking getTimeTracking() {
        return this.timeTracking;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setIssueKey(final String issueKey) {
        this.issueKey = issueKey;
    }

    public void setProjectKey(final String projectKey) {
        this.projectKey = projectKey;
    }

    public void setProject(final String project) {
        this.project = project;
    }

    public void setType(final long type) {
        this.type = type;
    }

    public void setSummary(final String summary) {
        this.summary = summary;
    }

    public void setStatus(final long status) {
        this.status = status;
    }

    public void setParent(final String parent) {
        this.parent = parent;
    }

    public void setDependencies(final List<String> dependencies) {
        this.dependencies = dependencies;
    }

    public void setBugs(final List<String> bugs) {
        this.bugs = bugs;
    }

    public void setAssignee(final User assignee) {
        this.assignee = assignee;
    }

    public void setPriority(final long priority) {
        this.priority = priority;
    }

    public void setRemoteIssueUpdatedDate(final Date remoteIssueUpdatedDate) {
        this.remoteIssueUpdatedDate = remoteIssueUpdatedDate;
    }
    
    public Date getRemoteIssueUpdatedDate() {
        return remoteIssueUpdatedDate;
    }
    
    public Date getPriorityUpdatedDate() {
        return issuePriorityService.priorityUpdateDate(this);
    }
    
    public void setCreated(final long created) {
        this.created = created;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setComments(final List<Comment> comments) {
        this.comments = comments;
    }

    public void setLabels(final LinkedList<String> labels) {
        this.labels = labels;
    }

    public void setComponents(final List<String> components) {
        this.components = components;
    }

    public String getReporter() {
        return reporter;
    }

    public void setReporter(String nameReporter) {
        this.reporter = nameReporter;
    }
    
    public CustomField getLocalClassOfServiceCustomField() {
        return classOfService;
    }
    
    public boolean isVisible() {
        if (isDeferred())
            return false;
        
        boolean isVisible = laneService.getFilters().stream().anyMatch(f -> f.matches(this));
        if (!isVisible)
            return false;
        
        return cardVisibilityEvalService.isStillInVisibleRange(
                status, 
                getUpdatedDate().toInstant(), 
                changelog);
    }

    public Set<Issue> getSubtaskCards() {
        return subtasks;
    }
   
    public List<Subtask> getSubtasks() {
        return subtasks.stream().
                sorted((s1,s2)->compareIssueKey(s1.issueKey, s2.issueKey)).
                map(s->
                    new Subtask(s.issueKey,
                                s.summary,
                                s.getStatusNameAsLoggedInUser(),
                                s.getIssueTypeNameAsLoggedInUser(),
                                s.getTypeIconUri(),
                                issueColorService.getStatusColor(s.type, s.status)))
                .collect(Collectors.toList());
    }

    static int compareIssueKey(String issueKey1, String issueKey2) {
        try {
            Pattern pattern = Pattern.compile("^(.*?)-([0-9]*)$");
            Matcher mk1 = pattern.matcher(issueKey1);
            Matcher mk2 = pattern.matcher(issueKey2);
            if (!mk1.matches() || !mk2.matches()) return 0;

            int projComp = mk1.group(1).compareTo(mk2.group(1));
            if (projComp != 0)
                return projComp;

            return Integer.parseInt(mk1.group(2))-
                   Integer.parseInt(mk2.group(2));
        }catch(NumberFormatException e) {
            return 0;
        }
    }

    public String getReleaseId() {
        if(releaseId != null)
            return releaseId;

        Optional<Issue> pc = getParentCard();
        return pc.map(issue -> issue.getReleaseId()).orElse(null);
    }

    public Version getRelease() {
        return projectService.getVersion(getReleaseId());
    }

    public Map<String, String> getExtraFields() {
        return extraFields;
    }

    public List<Worklog> getWorklogs() {
        return worklogs;
    }

    private Object readResolve() {
        this.subtasks = new LinkedHashSet<>();
        return this;
    }

    public void restoreServices(
            JiraProperties jiraProperties, 
            MetadataService metaDataService,
            IssueTeamService issueTeamService,
            LaneService laneService,
            CycleTime cycleTime,
            CardVisibilityEvalService cardVisibilityEvalService,
            ProjectService projectService,
            IssueColorService issueColorService,
            IssuePriorityService issuePriorityService) {
        this.jiraProperties = jiraProperties;
        this.metaDataService = metaDataService;
        this.issueTeamService = issueTeamService;
        this.laneService = laneService;
        this.cycleTime = cycleTime;
        this.cardVisibilityEvalService = cardVisibilityEvalService;
        this.projectService = projectService;
        this.issueColorService = issueColorService;
        this.issuePriorityService = issuePriorityService;
    }
    
    @Override
    public int hashCode() {
        if (this.issueKey == null)
            return 0;
        return this.issueKey.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Issue) {
            Issue other = (Issue)obj;
            
            return (issueKey+"").equals(other.issueKey+"");
        }
        return false;
    }

    public void addTeam(Team teamToAdd) {
        if (isUsingDefaultTeam())
            assignedTeamsIds.add(issueTeamService.getDefaultTeamId(this));

        else if (isUsingTeamByIssueType()) {
            Optional<CardTeam> teamByIssueType = issueTeamService.getCardTeamByIssueType(this);
            if (teamByIssueType.isPresent())
                assignedTeamsIds.add(teamByIssueType.get().id);
        }

        else if (isUsingParentTeam())
            assignedTeamsIds.addAll(parentCard.getRawAssignedTeamsIds());

        if (!assignedTeamsIds.contains(teamToAdd.getId()))
            assignedTeamsIds.add(teamToAdd.getId());
    }

    public void removeTeam(Team teamToRemove) {
        if (isUsingParentTeam()) {
            this.assignedTeamsIds.addAll(parentCard.getRawAssignedTeamsIds());
        }
        assignedTeamsIds.remove(teamToRemove.getId());
    }
    
    public void replaceTeam(Optional<Team> teamToReplace, Team replacementTeam) {
        if (teamToReplace.isPresent()) {
            int previousPos = assignedTeamsIds.indexOf(teamToReplace.get().getId());
            if (previousPos > -1) {
                assignedTeamsIds.set(previousPos, replacementTeam.getId());
                return;
            }
        }
        assignedTeamsIds.add(replacementTeam.getId());
    }

    public static class CardTeam {
        public String name;
        public Long id;
        
        public CardTeam() {}

        public CardTeam(Long i) {
            id = i;
        }

        public CardTeam(String name, Long i) {
            this.name = name;
            id = i;
        }

        public static CardTeam from(Team team) {
            CardTeam card = new CardTeam();
            card.name = team.getName();
            card.id = team.getId();
            return card;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            CardTeam other = (CardTeam) obj;
            if (id == null) {
                if (other.id != null)
                    return false;
            } else if (!id.equals(other.id))
                return false;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            return true;
        }
    }

    public Issue copy() {
        Issue copy = SerializationUtils.clone(this);
        restoreServicesToIssue(copy);

        if (this.parentCard != null) {
            Issue parentCopy = this.parentCard.copy();
            copy.setParentCard(parentCopy);
        }

        return copy;
    }

    private void restoreServicesToIssue(Issue issue) {
        issue.restoreServices(
                jiraProperties,
                metaDataService,
                issueTeamService,
                laneService,
                cycleTime,
                cardVisibilityEvalService,
                projectService,
                issueColorService,
                issuePriorityService);
    }

    public void restoreDefaultTeams() {
        this.assignedTeamsIds.clear();
    }
}