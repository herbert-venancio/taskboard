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
package objective.taskboard.data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.atlassian.jira.rest.client.api.domain.TimeTracking;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers.DateDeserializer;

import objective.taskboard.config.SpringContextBridge;
import objective.taskboard.jira.JiraProperties;
import objective.taskboard.jira.JiraProperties.BallparkMapping;
import objective.taskboard.jira.MetadataService;

@JsonIgnoreProperties({"jiraProperties", "metaDataService"})
public class Issue implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String issueKey;

    private String projectKey;

    private String project;

    private long type;

    private String typeIconUri;

    private String summary;

    private long status;

    private long startDateStepMillis;

    private String subresponsavel1;

    private String subresponsavel2;

    private String parent;

    private long parentType;

    private String parentTypeIconUri;

    private List<String> dependencies;

    private boolean render;

    private boolean favorite;

    private boolean hidden;

    private String color;

    private String subResponsaveis;

    private String assignee;

    private String usersTeam;

    private long priority;

    @JsonDeserialize(using = DateDeserializer.class)
    private Date dueDate;

    @JsonDeserialize(using = DateDeserializer.class)
    private Date updatedDate;

    private long created;

    private String description;

    private List<String> teams;

    private String comments;

    private List<String> labels;

    private List<String> components;

    @JsonProperty(access = Access.WRITE_ONLY)
    private Map<String, Object> customFields;

    private Long priorityOrder;

    private TaskboardTimeTracking timeTracking;

    private JiraProperties jiraProperties;

    private MetadataService metaDataService;

    public static Issue from(Long id,
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
            String color,
            String subResponsaveis,
            String assignee,
            String usersTeam,
            long priority,
            Date dueDate,
            long created,
            Date updatedDate,
            String description,
            List<String> teams,
            String comments,
            List<String> labels,
            List<String> components,
            Map<String, Object> customFields,
            Long priorityOrder,
            TaskboardTimeTracking timeTracking,
            JiraProperties jiraProperties,
            MetadataService metaDataService)
    {
        return new Issue(id,
                issueKey,
                projectKey,
                project,
                issueType,
                typeIconUri,
                summary,
                status,
                startDateStepMillis,
                subresponsavel1,
                subresponsavel2,
                parent,
                parentType,
                parentTypeIconUri,
                dependencies,
                false,
                false,
                false,
                color,
                subResponsaveis,
                assignee,
                usersTeam,
                priority,
                dueDate,
                created,
                description,
                teams,
                comments,
                labels,
                components,
                customFields,
                priorityOrder,
                timeTracking,
                jiraProperties,
                metaDataService,
                updatedDate);
    }

    public static class TaskboardTimeTracking {
        private Integer originalEstimateMinutes;
        private Integer timeSpentMinutes;

        public TaskboardTimeTracking() {
            originalEstimateMinutes = 0;
            timeSpentMinutes = 0;
        }

        public TaskboardTimeTracking(Integer originalEstimateMinutes, Integer timeSpentMinutes) {
            this.originalEstimateMinutes = originalEstimateMinutes;
            this.timeSpentMinutes = timeSpentMinutes;
        }

        public static TaskboardTimeTracking fromJira(TimeTracking tt) {
            if (tt == null)
                return null;
            return new TaskboardTimeTracking(tt.getOriginalEstimateMinutes(), tt.getTimeSpentMinutes());
        }

        public Integer getOriginalEstimateMinutes() {
            return originalEstimateMinutes;
        }
        public void setOriginalEstimateMinutes(Integer originalEstimateMinutes) {
            this.originalEstimateMinutes = originalEstimateMinutes;
        }
        public Integer getTimeSpentMinutes() {
            return timeSpentMinutes;
        }
        public void setTimeSpentMinutes(Integer timeSpentMinutes) {
            this.timeSpentMinutes = timeSpentMinutes;
        }
    }

    /**
     * Subtasks only need to show their key and summary.
     * 
     * DON'T USE. Will be terminated
     */
    @Deprecated
    public static Issue from(String issueKey, String summary) {
        Issue issue = new Issue();
        issue.setIssueKey(issueKey);
        issue.setSummary(summary);
        return issue;
    }

    @JsonAnyGetter
    public Map<String, Object> getCustomFields() {
        return customFields;
    }

    public Issue() {
        jiraProperties = SpringContextBridge.getBean(JiraProperties.class);
        metaDataService = SpringContextBridge.getBean(MetadataService.class);
    }

    private Issue(Long id, String issueKey, String projectKey, String project, long type, String typeIconUri,
            String summary, long status, long startDateStepMillis, String subresponsavel1, String subresponsavel2,
            String parent, long parentType, String parentTypeIconUri, List<String> dependencies, boolean render,
            boolean favorite, boolean hidden, String color, String subResponsaveis, String assignee, String usersTeam,
            long priority, Date dueDate, long created, String description, List<String> teams, String comments,
            List<String> labels, List<String> components, Map<String, Object> customFields, Long priorityOrder,
            TaskboardTimeTracking timeTracking,
            JiraProperties properties,
            MetadataService metaDataService,
            Date updatedDate) {
        this.id = id;
        this.issueKey = issueKey;
        this.projectKey = projectKey;
        this.project = project;
        this.type = type;
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
        this.render = render;
        this.favorite = favorite;
        this.hidden = hidden;
        this.color = color;
        this.subResponsaveis = subResponsaveis;
        this.assignee = assignee;
        this.usersTeam = usersTeam;
        this.priority = priority;
        this.dueDate = dueDate;
        this.created = created;
        this.updatedDate = updatedDate;
        this.description = description;
        this.teams = teams;
        this.comments = comments;
        this.labels = labels;
        this.components = components;
        this.customFields = customFields;
        this.priorityOrder = priorityOrder;
        this.timeTracking = timeTracking;
        this.jiraProperties = properties;
        this.metaDataService = metaDataService;
    }

    @JsonIgnore
    public boolean isDemand() {
        return jiraProperties.getIssuetype().getDemand().getId() == this.getType();
    }

    @JsonIgnore
    public boolean isFeature() {
        return jiraProperties.getIssuetype().getFeatures().stream().anyMatch(ft -> ft.getId() == this.getType());
    }

    @JsonIgnore
    public boolean isSubTask() {
        return !isDemand() && !isFeature();
    }

    @JsonIgnore
    public Integer getIssueKeyNum() {
        return Integer.parseInt(issueKey.replace(projectKey+"-", ""));
    }

    @JsonIgnore
    public String getTShirtSize() {
        String mainTShirtSizeFieldId = jiraProperties.getCustomfield().getTShirtSize().getMainTShirtSizeFieldId();
        if (customFields.get(mainTShirtSizeFieldId) == null)
            return null;

        CustomField customField = (CustomField)customFields.get(mainTShirtSizeFieldId);
        if (customField == null || customField.getValue() == null)
            return null;
        return customField.getValue().toString();
    }

    @JsonIgnore
    public void setTShirtSize(String value) {
        String mainTShirtSizeFieldId = jiraProperties.getCustomfield().getTShirtSize().getMainTShirtSizeFieldId();
        if (customFields.get(mainTShirtSizeFieldId) == null)
            return;
        CustomField customField = (CustomField)customFields.get(mainTShirtSizeFieldId);
        if (customField == null) {
            customField = new CustomField(mainTShirtSizeFieldId, value);
            customFields.put(mainTShirtSizeFieldId, customField);
        }
        customField.setValue(value);
    }

    @JsonIgnore
    public String getTshirtSizeOfSubtaskForBallpark(BallparkMapping mapping) {
        CustomField customField = (CustomField)customFields.get(mapping.getTshirtCustomFieldId());
        if (customField == null || customField.getValue() == null) return null;
        return customField.getValue().toString();
    }

    @JsonIgnore
    public List<BallparkMapping> getBallparkMappings() {
        return jiraProperties.getFollowup().getBallparkMappings().get(getType());
    }

    @JsonIgnore
    public List<BallparkMapping> getActiveBallparkMappings() {
        List<BallparkMapping> list = jiraProperties.getFollowup().getBallparkMappings().get(getType());
        if (list == null)
            return null;

        return list.stream().filter(bm -> getTshirtSizeOfSubtaskForBallpark(bm)!=null).collect(Collectors.toList());
    }

    @JsonIgnore
    public String getIssueTypeName() {
        return metaDataService.getIssueTypeById(type).getName();
    }

    @JsonIgnore
    public String getStatusName() {
        return metaDataService.getStatusById(status).getName();
    }

    @JsonIgnore
    public Integer getStatusPriority() {
        if (this.isDemand())
            return jiraProperties.getStatusPriorityOrder().getDemandPriorityByStatus(this.getStatusName());

        if (this.isFeature())
            return jiraProperties.getStatusPriorityOrder().getTaskPriorityByStatus(this.getStatusName());

        return jiraProperties.getStatusPriorityOrder().getSubtaskPriorityByStatus(this.getStatusName());
    }

    public Long getId() {
        return this.id;
    }

    public String getIssueKey() {
        return this.issueKey;
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
        return this.typeIconUri;
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

    public String getSubresponsavel1() {
        return this.subresponsavel1;
    }

    public String getSubresponsavel2() {
        return this.subresponsavel2;
    }

    public String getParent() {
        return this.parent;
    }

    public long getParentType() {
        return this.parentType;
    }

    public String getParentTypeIconUri() {
        return this.parentTypeIconUri;
    }

    public List<String> getDependencies() {
        return this.dependencies;
    }

    public boolean isRender() {
        return this.render;
    }

    public boolean isFavorite() {
        return this.favorite;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public String getColor() {
        return this.color;
    }

    public String getSubResponsaveis() {
        return this.subResponsaveis;
    }

    public String getAssignee() {
        return this.assignee;
    }

    public String getUsersTeam() {
        return this.usersTeam;
    }

    public long getPriority() {
        return this.priority;
    }

    public Date getDueDate() {
        return this.dueDate;
    }

    public Date getUpdatedDate() {
        return this.updatedDate;
    }

    public long getCreated() {
        return this.created;
    }

    public String getDescription() {
        return this.description;
    }

    public List<String> getTeams() {
        return this.teams;
    }

    public String getComments() {
        return this.comments;
    }

    public List<String> getLabels() {
        return this.labels;
    }

    public List<String> getComponents() {
        return this.components;
    }

    public Long getPriorityOrder() {
        return this.priorityOrder;
    }

    public TaskboardTimeTracking getTimeTracking() {
        return this.timeTracking;
    }

    public JiraProperties getJiraProperties() {
        return this.jiraProperties;
    }

    public MetadataService getMetaDataService() {
        return this.metaDataService;
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

    public void setTypeIconUri(final String typeIconUri) {
        this.typeIconUri = typeIconUri;
    }

    public void setSummary(final String summary) {
        this.summary = summary;
    }

    public void setStatus(final long status) {
        this.status = status;
    }

    public void setStartDateStepMillis(final long startDateStepMillis) {
        this.startDateStepMillis = startDateStepMillis;
    }

    public void setSubresponsavel1(final String subresponsavel1) {
        this.subresponsavel1 = subresponsavel1;
    }

    public void setSubresponsavel2(final String subresponsavel2) {
        this.subresponsavel2 = subresponsavel2;
    }

    public void setParent(final String parent) {
        this.parent = parent;
    }

    public void setParentType(final long parentType) {
        this.parentType = parentType;
    }

    public void setParentTypeIconUri(final String parentTypeIconUri) {
        this.parentTypeIconUri = parentTypeIconUri;
    }

    public void setDependencies(final List<String> dependencies) {
        this.dependencies = dependencies;
    }

    public void setRender(final boolean render) {
        this.render = render;
    }

    public void setFavorite(final boolean favorite) {
        this.favorite = favorite;
    }

    public void setHidden(final boolean hidden) {
        this.hidden = hidden;
    }

    public void setColor(final String color) {
        this.color = color;
    }

    public void setSubResponsaveis(final String subResponsaveis) {
        this.subResponsaveis = subResponsaveis;
    }

    public void setAssignee(final String assignee) {
        this.assignee = assignee;
    }

    public void setUsersTeam(final String usersTeam) {
        this.usersTeam = usersTeam;
    }

    public void setPriority(final long priority) {
        this.priority = priority;
    }

    public void setDueDate(final Date dueDate) {
        this.dueDate = dueDate;
    }

    public void setUpdatedDate(final Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public void setCreated(final long created) {
        this.created = created;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setTeams(final List<String> teams) {
        this.teams = teams;
    }

    public void setComments(final String comments) {
        this.comments = comments;
    }

    public void setLabels(final List<String> labels) {
        this.labels = labels;
    }

    public void setComponents(final List<String> components) {
        this.components = components;
    }

    public void setCustomFields(final Map<String, Object> customFields) {
        this.customFields = customFields;
    }

    public void setPriorityOrder(final Long priorityOrder) {
        this.priorityOrder = priorityOrder;
    }

    public void setTimeTracking(final TaskboardTimeTracking timeTracking) {
        this.timeTracking = timeTracking;
    }

    public void setJiraProperties(final JiraProperties jiraProperties) {
        this.jiraProperties = jiraProperties;
    }

    public void setMetaDataService(final MetadataService metaDataService) {
        this.metaDataService = metaDataService;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Issue issue = (Issue) o;

        if (type != issue.type) return false;
        if (status != issue.status) return false;
        if (startDateStepMillis != issue.startDateStepMillis) return false;
        if (parentType != issue.parentType) return false;
        if (render != issue.render) return false;
        if (favorite != issue.favorite) return false;
        if (hidden != issue.hidden) return false;
        if (priority != issue.priority) return false;
        if (created != issue.created) return false;
        if (id != null ? !id.equals(issue.id) : issue.id != null) return false;
        if (issueKey != null ? !issueKey.equals(issue.issueKey) : issue.issueKey != null) return false;
        if (projectKey != null ? !projectKey.equals(issue.projectKey) : issue.projectKey != null) return false;
        if (project != null ? !project.equals(issue.project) : issue.project != null) return false;
        if (typeIconUri != null ? !typeIconUri.equals(issue.typeIconUri) : issue.typeIconUri != null) return false;
        if (summary != null ? !summary.equals(issue.summary) : issue.summary != null) return false;
        if (subresponsavel1 != null ? !subresponsavel1.equals(issue.subresponsavel1) : issue.subresponsavel1 != null)
            return false;
        if (subresponsavel2 != null ? !subresponsavel2.equals(issue.subresponsavel2) : issue.subresponsavel2 != null)
            return false;
        if (parent != null ? !parent.equals(issue.parent) : issue.parent != null) return false;
        if (parentTypeIconUri != null ? !parentTypeIconUri.equals(issue.parentTypeIconUri) : issue.parentTypeIconUri != null)
            return false;
        if (dependencies != null ? !dependencies.equals(issue.dependencies) : issue.dependencies != null) return false;
        if (color != null ? !color.equals(issue.color) : issue.color != null) return false;
        if (subResponsaveis != null ? !subResponsaveis.equals(issue.subResponsaveis) : issue.subResponsaveis != null)
            return false;
        if (assignee != null ? !assignee.equals(issue.assignee) : issue.assignee != null) return false;
        if (usersTeam != null ? !usersTeam.equals(issue.usersTeam) : issue.usersTeam != null) return false;
        if (dueDate != null ? !dueDate.equals(issue.dueDate) : issue.dueDate != null) return false;
        if (updatedDate != null ? !updatedDate.equals(issue.updatedDate) : issue.updatedDate != null) return false;
        if (description != null ? !description.equals(issue.description) : issue.description != null) return false;
        if (teams != null ? !teams.equals(issue.teams) : issue.teams != null) return false;
        if (comments != null ? !comments.equals(issue.comments) : issue.comments != null) return false;
        if (labels != null ? !labels.equals(issue.labels) : issue.labels != null) return false;
        if (components != null ? !components.equals(issue.components) : issue.components != null) return false;
        if (customFields != null ? !customFields.equals(issue.customFields) : issue.customFields != null) return false;
        if (priorityOrder != null ? !priorityOrder.equals(issue.priorityOrder) : issue.priorityOrder != null)
            return false;
        if (timeTracking != null ? !timeTracking.equals(issue.timeTracking) : issue.timeTracking != null) return false;
        if (jiraProperties != null ? !jiraProperties.equals(issue.jiraProperties) : issue.jiraProperties != null)
            return false;
        return metaDataService != null ? metaDataService.equals(issue.metaDataService) : issue.metaDataService == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (issueKey != null ? issueKey.hashCode() : 0);
        result = 31 * result + (projectKey != null ? projectKey.hashCode() : 0);
        result = 31 * result + (project != null ? project.hashCode() : 0);
        result = 31 * result + (int) (type ^ (type >>> 32));
        result = 31 * result + (typeIconUri != null ? typeIconUri.hashCode() : 0);
        result = 31 * result + (summary != null ? summary.hashCode() : 0);
        result = 31 * result + (int) (status ^ (status >>> 32));
        result = 31 * result + (int) (startDateStepMillis ^ (startDateStepMillis >>> 32));
        result = 31 * result + (subresponsavel1 != null ? subresponsavel1.hashCode() : 0);
        result = 31 * result + (subresponsavel2 != null ? subresponsavel2.hashCode() : 0);
        result = 31 * result + (parent != null ? parent.hashCode() : 0);
        result = 31 * result + (int) (parentType ^ (parentType >>> 32));
        result = 31 * result + (parentTypeIconUri != null ? parentTypeIconUri.hashCode() : 0);
        result = 31 * result + (dependencies != null ? dependencies.hashCode() : 0);
        result = 31 * result + (render ? 1 : 0);
        result = 31 * result + (favorite ? 1 : 0);
        result = 31 * result + (hidden ? 1 : 0);
        result = 31 * result + (color != null ? color.hashCode() : 0);
        result = 31 * result + (subResponsaveis != null ? subResponsaveis.hashCode() : 0);
        result = 31 * result + (assignee != null ? assignee.hashCode() : 0);
        result = 31 * result + (usersTeam != null ? usersTeam.hashCode() : 0);
        result = 31 * result + (int) (priority ^ (priority >>> 32));
        result = 31 * result + (dueDate != null ? dueDate.hashCode() : 0);
        result = 31 * result + (updatedDate != null ? updatedDate.hashCode() : 0);
        result = 31 * result + (int) (created ^ (created >>> 32));
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (teams != null ? teams.hashCode() : 0);
        result = 31 * result + (comments != null ? comments.hashCode() : 0);
        result = 31 * result + (labels != null ? labels.hashCode() : 0);
        result = 31 * result + (components != null ? components.hashCode() : 0);
        result = 31 * result + (customFields != null ? customFields.hashCode() : 0);
        result = 31 * result + (priorityOrder != null ? priorityOrder.hashCode() : 0);
        result = 31 * result + (timeTracking != null ? timeTracking.hashCode() : 0);
        result = 31 * result + (jiraProperties != null ? jiraProperties.hashCode() : 0);
        result = 31 * result + (metaDataService != null ? metaDataService.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Issue{" +
                "id=" + id +
                ", issueKey='" + issueKey + '\'' +
                ", projectKey='" + projectKey + '\'' +
                ", project='" + project + '\'' +
                ", type=" + type +
                ", typeIconUri='" + typeIconUri + '\'' +
                ", summary='" + summary + '\'' +
                ", status=" + status +
                ", startDateStepMillis=" + startDateStepMillis +
                ", subresponsavel1='" + subresponsavel1 + '\'' +
                ", subresponsavel2='" + subresponsavel2 + '\'' +
                ", parent='" + parent + '\'' +
                ", parentType=" + parentType +
                ", parentTypeIconUri='" + parentTypeIconUri + '\'' +
                ", dependencies=" + dependencies +
                ", render=" + render +
                ", favorite=" + favorite +
                ", hidden=" + hidden +
                ", color='" + color + '\'' +
                ", subResponsaveis='" + subResponsaveis + '\'' +
                ", assignee='" + assignee + '\'' +
                ", usersTeam='" + usersTeam + '\'' +
                ", priority=" + priority +
                ", dueDate=" + dueDate +
                ", updatedDate=" + updatedDate +
                ", created=" + created +
                ", description='" + description + '\'' +
                ", teams=" + teams +
                ", comments='" + comments + '\'' +
                ", labels=" + labels +
                ", components=" + components +
                ", customFields=" + customFields +
                ", priorityOrder=" + priorityOrder +
                ", timeTracking=" + timeTracking +
                ", jiraProperties=" + jiraProperties +
                ", metaDataService=" + metaDataService +
                '}';
    }
}
