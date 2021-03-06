package objective.taskboard.data;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import objective.taskboard.jira.data.Version;

public class IssueScratch implements Serializable {
    private static final long serialVersionUID = -8643342601909365442L;

    protected Long id = 0L;
    protected String issueKey;
    protected String projectKey;
    protected String project;
    protected long type;
    protected String summary;
    protected long status;
    protected long startDateStepMillis;
    protected String parent;
    protected List<String> dependencies; //NOSONAR
    protected List<String> bugs; // NOSONAR
    protected User assignee;
    protected List<User> coAssignees; //NOSONAR
    protected long priority;
    protected Date dueDate;
    protected Date remoteIssueUpdatedDate;

    protected long created;
    protected String description;

    protected List<String> labels;       //NOSONAR
    protected List<String> components;   //NOSONAR
    protected boolean blocked;
    protected boolean shouldBlockAllSubtasks;
    protected String lastBlockReason;
    protected Map<String, CustomField> tshirtSizes; //NOSONAR
    protected CustomField additionalEstimatedHours;
    protected TaskboardTimeTracking timeTracking;
    protected String releaseId;
    protected List<Changelog> changelog; //NOSONAR
    protected List<Version> fixVersions; //NOSONAR

    @JsonIgnore
    protected String reporter;
    @JsonIgnore
    protected CustomField classOfService;
    @JsonIgnore
    protected List<Worklog> worklogs; //NOSONAR

    protected List<Comment> comments; //NOSONAR
    @JsonIgnore
    protected List<Long> assignedTeamsIds = new LinkedList<>();//NOSONAR

    protected Map<String, String> extraFields; //NOSONAR
    
    public IssueScratch() {
        super();
    }

    public IssueScratch(
            Long id, 
            String issueKey, 
            String projectKey, 
            String project, 
            long issueType, 
            String summary, 
            long status, 
            long startDateStepMillis, 
            String parent, 
            List<String> dependencies,
            List<String> bugs,
            List<User> subResponsaveis, 
            User assignee, 
            long priority, 
            Date dueDate, 
            long created,
            Date remoteIssueUpdatedDate,
            String description,
            List<Comment> comments,
            List<String> labels,
            List<String> components,
            boolean blocked,
            boolean shouldBlockAllSubtasks,
            String lastBlockReason,
            Map<String, CustomField> tshirtSizes,
            CustomField additionalEstimatedHours,
            TaskboardTimeTracking timeTracking,
            String reporter, 
            CustomField classOfService, 
            String releaseId,
            List<Changelog> changelog,
            List<Worklog> worklogs,
            List<Long> assignedTeamsIds,
            Map<String, String> extraFields,
            List<Version> fixVersions) {
        this.id = id;
        this.issueKey = issueKey;
        this.projectKey = projectKey;
        this.project = project;
        this.type = issueType;
        this.summary = summary;
        this.status = status;
        this.startDateStepMillis = startDateStepMillis;
        this.parent = parent;
        this.dependencies = dependencies;
        this.bugs = bugs;
        this.coAssignees = subResponsaveis == null? new LinkedList<>(): subResponsaveis;
        this.assignee = assignee;
        this.priority = priority;
        this.dueDate = dueDate;
        this.created = created;
        this.remoteIssueUpdatedDate = remoteIssueUpdatedDate;
        this.description = description;
        this.comments = comments;
        this.labels = labels;
        this.components = components;
        this.blocked = blocked;
        this.shouldBlockAllSubtasks = shouldBlockAllSubtasks;
        this.lastBlockReason = lastBlockReason;
        this.tshirtSizes = tshirtSizes;
        this.additionalEstimatedHours = additionalEstimatedHours;
        this.timeTracking = timeTracking;
        this.reporter = reporter;
        this.classOfService = classOfService;
        this.releaseId = releaseId;
        this.changelog = changelog;

        this.assignedTeamsIds.addAll(assignedTeamsIds);
        
        this.worklogs = worklogs;

        this.extraFields = extraFields;
        this.fixVersions = fixVersions;
    }

    public String getIssueKey() {
        return issueKey;
    }

}