package objective.taskboard.data;

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

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.atlassian.jira.rest.client.api.domain.TimeTracking;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers.DateDeserializer;

import lombok.Data;
import objective.taskboard.jira.JiraProperties;
import objective.taskboard.jira.JiraProperties.BallparkMapping;
import objective.taskboard.jira.MetadataService;

@Data
@JsonIgnoreProperties({"jiraProperties","metaDataService"})
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
                metaDataService);
    }
    
    public static class TaskboardTimeTracking {
        private Integer originalEstimateMinutes;
        private Integer timeSpentMinutes;
        
        public TaskboardTimeTracking(){
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
     */
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

    public Issue(){}

    private Issue(Long id, String issueKey, String projectKey, String project, long type, String typeIconUri,
            String summary, long status, long startDateStepMillis, String subresponsavel1, String subresponsavel2,
            String parent, long parentType, String parentTypeIconUri, List<String> dependencies, boolean render,
            boolean favorite, boolean hidden, String color, String subResponsaveis, String assignee, String usersTeam,
            long priority, Date dueDate, long created, String description, List<String> teams, String comments,
            List<String> labels, List<String> components, Map<String, Object> customFields, Long priorityOrder, 
            TaskboardTimeTracking timeTracking,
            JiraProperties properties, MetadataService metaDataService) {
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
    
    public Integer getIssueKeyNum() {
        return Integer.parseInt(issueKey.replace(projectKey+"-", ""));
    }
    
    public String getTShirtSize() {
        String mainTShirtSizeFieldId = jiraProperties.getCustomfield().getTShirtSize().getMainTShirtSizeFieldId();
        CustomField customField = (CustomField)customFields.get(mainTShirtSizeFieldId);
        if (customField == null || customField.getValue() == null)
            return null;
        return customField.getValue().toString();
    }
    
    public void setTShirtSize(String value) {
        String mainTShirtSizeFieldId = jiraProperties.getCustomfield().getTShirtSize().getMainTShirtSizeFieldId();
        CustomField customField = (CustomField)customFields.get(mainTShirtSizeFieldId);
        if (customField == null) {
            customField = new CustomField(mainTShirtSizeFieldId, value);
            customFields.put(mainTShirtSizeFieldId, customField);
        }
        customField.setValue(value);        
    }
    
    public String getTshirtSizeOfSubtaskForBallpark(BallparkMapping mapping) {
        CustomField customField = (CustomField)customFields.get(mapping.getTshirtCustomFieldId());
        if (customField == null || customField.getValue() == null) return null;
        return customField.getValue().toString();
    }

    public List<BallparkMapping> getBallparkMappings() {
        return jiraProperties.getFollowup().getBallparkMappings().get(getType());
    }
    
    public List<BallparkMapping> getActiveBallparkMappings() {
        List<BallparkMapping> list = jiraProperties.getFollowup().getBallparkMappings().get(getType());
        if (list == null)
            return null;
        
        return list.stream().filter(bm -> getTshirtSizeOfSubtaskForBallpark(bm)!=null).collect(Collectors.toList());
    }
    
    public String getIssueTypeName() {
        try {
            return metaDataService.getIssueTypeMetadata().get(type).getName();
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }
    
    public String getStatusName() {
        try {
            return metaDataService.getStatusesMetadata().get(status).getName();
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }
}
