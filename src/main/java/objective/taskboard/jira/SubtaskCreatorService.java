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

import java.util.List;
import java.util.Optional;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.Subtask;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;

import objective.taskboard.jira.JiraProperties.SubtaskCreation;
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

    public void create(Issue parent, SubtaskCreation creationProperties) {
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

    private String subtaskOfType(Issue parent, Long typeId) {
        for (Subtask sub : parent.getSubtasks())
            if (sub.getIssueType().getId().equals(typeId))
                return sub.getIssueKey();
        return null;
    }

    private String create(Issue parent, SubtaskCreation creationProperties, Long typeId) {
        String summaryPrefix = creationProperties.getSummaryPrefix();
        String tShirtSizeParentId = creationProperties.getTShirtSizeParentId();
        String tShirtSizeSubtaskId = creationProperties.getTShirtSizeSubtaskId();
        String tShirtSizeDefaultValue = creationProperties.getTShirtSizeDefaultValue();

        IssueInputBuilder issueBuilder = new IssueInputBuilder(parent.getProject().getKey(), typeId);
        issueBuilder.setFieldValue("parent", ComplexIssueInputFieldValue.with("key", parent.getKey()));
        issueBuilder.setPriorityId(parent.getPriority().getId());
        issueBuilder.setSummary(summaryPrefix + parent.getSummary());
        issueBuilder.setReporter(parent.getReporter());
        setTShirtSize(issueBuilder, parent, tShirtSizeParentId, tShirtSizeSubtaskId, tShirtSizeDefaultValue);
        setClassOfService(issueBuilder, parent);
        
        IssueInput issueInput = issueBuilder.build();
        
        log.debug("Creating subtask of issue " + parent.getKey() + ". Type id: " + typeId);

        return jiraService.createIssueAsMaster(issueInput);
    }

    private void setTShirtSize(IssueInputBuilder issueBuilder, Issue parent, String tShirtParentId, String tShirtSubtaskId, String defaultValue) {
        String tShirtValue = getTShirtSizeValue(parent, tShirtParentId, defaultValue);
        issueBuilder.setFieldValue(tShirtSubtaskId, ComplexIssueInputFieldValue.with("value", tShirtValue));
    }

    private String getTShirtSizeValue(Issue parent, String tShirtSizeParentId, String tShirtSizeDefaultValue) {
        IssueField parentTShirtSize = parent.getField(tShirtSizeParentId);
        if (parentTShirtSize == null || parentTShirtSize.getValue() == null)
            return tShirtSizeDefaultValue;

        try {
            return ((JSONObject) parentTShirtSize.getValue()).getString("value");
        } catch (JSONException e) {
            log.error("Error extracting t-shirt-size value (customfield id = '" + tShirtSizeParentId + "') from parent issue '" + parent.getKey() + "'", e);
            return  tShirtSizeDefaultValue;
        }
    }

    private void setClassOfService(IssueInputBuilder issueBuilder, Issue parent) {
        String classOfServiceId = jiraProperties.getCustomfield().getClassOfService().getId();
        IssueField parentClassOfService = parent.getField(classOfServiceId);
        
        if (parentClassOfService == null || parentClassOfService.getValue() == null)
            return;

        try {
            String valueId = ((JSONObject) parentClassOfService.getValue()).getString("id");
            issueBuilder.setFieldValue(classOfServiceId, ComplexIssueInputFieldValue.with("id", valueId));
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

        try {
            jiraService.doTransitionAsMaster(issueKey, transitionId);
        } catch (JiraServiceException e) {
            log.error("Error executing transition '" + transitionId + "' on issue '" + issueKey + "'", e);
        }
    }

    private Optional<Transition> findTransition(String issueKey, Long transitionId) {
        List<Transition> transitions = jiraService.getTransitionsAsMaster(issueKey);
        return transitions.stream()
                .filter(t -> transitionId.equals(t.id))
                .findFirst();
    }

}
