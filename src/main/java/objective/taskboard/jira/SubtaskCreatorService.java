/*- 
 * [LICENSE] 
 * Taskboard 
 * - - - 
 * Copyright (C) 2015 - 2017 Objective Solutions 
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

package objective.taskboard.jira;

import static objective.taskboard.domain.converter.IssueFieldsExtractor.extractSingleValueCheckbox;
import static objective.taskboard.jira.data.JiraIssue.FieldBuilder.byId;
import static objective.taskboard.jira.data.JiraIssue.FieldBuilder.byKey;
import static objective.taskboard.jira.data.JiraIssue.FieldBuilder.byName;
import static objective.taskboard.jira.data.JiraIssue.FieldBuilder.byValue;

import java.util.List;
import java.util.Optional;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import objective.taskboard.jira.JiraProperties.SubtaskCreation;
import objective.taskboard.jira.client.JiraIssueDto;
import objective.taskboard.jira.client.JiraSubtaskDto;
import objective.taskboard.jira.data.JiraIssue;
import objective.taskboard.jira.data.Transition;

@Service
public class SubtaskCreatorService {

    private static final Logger log = LoggerFactory.getLogger(SubtaskCreatorService.class);

    private final JiraService jiraService;
    private final JiraProperties jiraProperties;

    public SubtaskCreatorService(JiraService jiraService, JiraProperties jiraProperties) {
        this.jiraService = jiraService;
        this.jiraProperties = jiraProperties;
    }

    public void create(JiraIssueDto parent, SubtaskCreation creationProperties) {
        if (!hasRequiredValueOrHasNoRequirement(parent, creationProperties))
            return;
        
        if(skipCreationWhenTShirtSizeParentIsAbsent(parent, creationProperties))
            return;

        Long typeId = creationProperties.getIssueTypeId();

        String subtaskKey = subtaskOfType(parent, typeId);        
        if (subtaskKey == null) {
            subtaskKey = create(parent, creationProperties, typeId);
        } else {
            log.debug("Subtask already exists. Creation will be skipped");
        }

        Optional<Long> transitionId = creationProperties.getTransitionId();
        if (transitionId.isPresent())
            executeTransitionIfAvailable(transitionId.get(), subtaskKey);
    }

    private boolean hasRequiredValueOrHasNoRequirement(JiraIssueDto parent, SubtaskCreation creationProperties) {
        return creationProperties.getCustomFieldCondition()
                .map(field -> extractSingleValueCheckbox(field.getId(), parent))
                .orElse(true);
    }
    
    private boolean skipCreationWhenTShirtSizeParentIsAbsent(JiraIssueDto parent, SubtaskCreation creationProperties) {
        JSONObject parentTShirtSize = parent.getField(creationProperties.getTShirtSizeParentId());
        Boolean skipCreation = creationProperties.getSkipCreationWhenTShirtParentIsAbsent();
        return parentTShirtSize == null && skipCreation;
    }

    private String subtaskOfType(JiraIssueDto parent, Long typeId) {
        for (JiraSubtaskDto sub : parent.getSubtasks())
            if (sub.getIssueType().getId().equals(typeId))
                return sub.getIssueKey();
        return null;
    }

    private String create(JiraIssueDto parent, SubtaskCreation creationProperties, Long typeId) {
        String summaryPrefix = creationProperties.getSummaryPrefix();
        String tShirtSizeParentId = creationProperties.getTShirtSizeParentId();
        String tShirtSizeSubtaskId = creationProperties.getTShirtSizeSubtaskId();
        String tShirtSizeDefaultValue = creationProperties.getTShirtSizeDefaultValue();

        JiraIssue.CustomInputBuilder issueBuilder = JiraIssue.Input.builder(jiraProperties, parent.getProject().getKey(), typeId)
                .parent(byKey(parent.getKey()))
                .priority(byId(parent.getPriority().getId()))
                .summary(summaryPrefix + parent.getSummary())
                .reporter(byName(parent.getReporter().getName()));
        setTShirtSize(issueBuilder, parent, tShirtSizeParentId, tShirtSizeSubtaskId, tShirtSizeDefaultValue);
        setClassOfService(issueBuilder, parent);
        
        JiraIssue.Input issueInput = issueBuilder.build();
        
        log.debug("Creating subtask of issue " + parent.getKey() + ". Type id: " + typeId);

        return jiraService.createIssueAsMaster(issueInput);
    }

    private void setTShirtSize(JiraIssue.InputBuilder<?> issueBuilder, JiraIssueDto parent, String tShirtParentId, String tShirtSubtaskId, String defaultValue) {
        String tShirtValue = getTShirtSizeValue(parent, tShirtParentId, defaultValue);
        issueBuilder.field(tShirtSubtaskId, byValue(tShirtValue));
    }

    private String getTShirtSizeValue(JiraIssueDto parent, String tShirtSizeParentId, String tShirtSizeDefaultValue) {
        JSONObject parentTShirtSize = parent.getField(tShirtSizeParentId);
        if (parentTShirtSize == null)
            return tShirtSizeDefaultValue;

        try {
            return parentTShirtSize.getString("value");
        } catch (JSONException e) {
            log.error("Error extracting t-shirt-size value (customfield id = '" + tShirtSizeParentId + "') from parent issue '" + parent.getKey() + "'", e);
            return  tShirtSizeDefaultValue;
        }
    }

    private void setClassOfService(JiraIssue.CustomInputBuilder issueBuilder, JiraIssueDto parent) {
        String classOfServiceId = jiraProperties.getCustomfield().getClassOfService().getId();
        JSONObject parentClassOfService = parent.getField(classOfServiceId);
        
        if (parentClassOfService == null)
            return;

        try {
            String valueId = parentClassOfService.getString("id");
            issueBuilder.classOfService(byId(valueId));
        } catch (JSONException e) {
            // just don't set if can't extract parent value
            log.error("Error extracting class-of-service value (customfield id = '" + classOfServiceId + "') from parent issue '" + parent.getKey() + "'", e);
        }
    }

    private void executeTransitionIfAvailable(Long transitionId, String issueKey) {
        Optional<Transition> transition = findTransition(issueKey, transitionId);
        if (!transition.isPresent()) {
            log.debug("Transition " + transitionId + " is not available for issue " + issueKey);
            return;
        }

        log.debug("Executing transition of subtask " + issueKey + ". Transition id: " + transitionId);

        jiraService.doTransitionAsMaster(issueKey, transitionId);
    }

    private Optional<Transition> findTransition(String issueKey, Long transitionId) {
        List<Transition> transitions = jiraService.getTransitionsAsMaster(issueKey);
        return transitions.stream()
                .filter(t -> transitionId.equals(t.id))
                .findFirst();
    }

}
