package objective.taskboard.data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import objective.taskboard.domain.converter.IssueCoAssignee;

public class IssueScratch implements Serializable {
    private static final long serialVersionUID = -8643342601909365442L;
    
    protected Long id;
    protected String issueKey;
    protected String projectKey;
    protected String project;
    protected long type;
    protected String typeIconUri;
    protected String summary;
    protected long status;
    protected long startDateStepMillis;
    protected String subresponsavel1;
    protected String subresponsavel2;
    protected String parent;
    protected long parentType;
    protected String parentTypeIconUri;
    protected List<String> dependencies;
    protected boolean render;
    protected boolean favorite;
    protected boolean hidden;
    protected String subResponsaveis;
    protected String assignee;
    protected long priority;
    protected Date dueDate;
    protected Date updatedDate;
    protected Date remoteIssueUpdatedDate;

    protected long created;
    protected String description;
    protected String comments;
    protected List<String> labels;
    protected List<String> components;
    protected Long priorityOrder;
    protected TaskboardTimeTracking timeTracking;
    protected List<Changelog> changelog;
    protected boolean isVisible;
    protected LocalDateTime visibleUntil;
    
    @JsonIgnore
    protected String reporter;
    @JsonIgnore
    protected List<IssueCoAssignee> coAssignees = new LinkedList<>();
    @JsonIgnore
    protected CustomField classOfService;
    @JsonIgnore
    protected Map<String, CustomField> release;
    @JsonProperty(access = Access.WRITE_ONLY)
    protected Map<String, Serializable> customFields;

    public IssueScratch() {
        super();
    }

    public IssueScratch(
            Long id, 
            String issueKey, 
            String projectKey, 
            String project, 
            long issueType, 
            String typeIconUri,
            String summary, 
            long status, 
            long startDateStepMillis, 
            String subresponsavel1, 
            String subresponsavel2, 
            String parent, 
            long parentType, 
            String parentTypeIconUri,
            List<String> dependencies, 
            String subResponsaveis, 
            String assignee, 
            long priority, 
            Date dueDate, 
            long created,
            Date updatedDate, 
            Date remoteIssueUpdatedDate,
            String description, 
            String comments, 
            List<String> labels, 
            List<String> components, 
            Map<String, Serializable> customFields,
            Long priorityOrder,
            TaskboardTimeTracking timeTracking, 
            String reporter, 
            List<IssueCoAssignee> coAssignees, 
            CustomField classOfService, 
            Map<String, CustomField> release,
            List<Changelog> changelog,
            boolean isVisible,
            Optional<LocalDateTime> visibleUntil) {
        this.id = id;
        this.issueKey = issueKey;
        this.projectKey = projectKey;
        this.project = project;
        this.type = issueType;
        this.typeIconUri = typeIconUri;
        this.summary = summary;
        this.status = status;
        this.startDateStepMillis = startDateStepMillis;
        this.subresponsavel1 = subresponsavel1;
        this.subresponsavel2 = subresponsavel2;
        this.parent = parent;
        this.parentType = parentType;
        this.parentTypeIconUri = parentTypeIconUri;
        this.dependencies = dependencies;
        this.subResponsaveis = subResponsaveis;
        this.assignee = assignee;
        this.priority = priority;
        this.dueDate = dueDate;
        this.created = created;
        this.updatedDate = updatedDate;
        this.remoteIssueUpdatedDate = remoteIssueUpdatedDate;
        this.description = description;
        this.comments = comments;
        this.labels = labels;
        this.components = components;
        this.customFields = customFields;
        this.priorityOrder = priorityOrder;
        this.timeTracking = timeTracking;
        this.reporter = reporter;
        this.coAssignees = coAssignees;
        this.classOfService = classOfService;
        this.release = release;
        this.changelog = changelog;
        this.isVisible = isVisible;
        if (visibleUntil.isPresent())
            this.visibleUntil = visibleUntil.get();
        
        this.render = false;
        this.favorite = false;
        this.hidden = false;        
    }

    public String getIssueKey() {
        return issueKey;
    }

}