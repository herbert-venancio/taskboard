package objective.taskboard.controller;

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

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import objective.taskboard.domain.Filter;
import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.issueBuffer.WebhookEvent;
import objective.taskboard.jira.JiraProperties;
import objective.taskboard.repository.FilterCachedRepository;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;
import objective.taskboard.task.WebhookSchedule;

@Slf4j
@RestController
@RequestMapping("webhook")
public class WebhookController {

    @Autowired
    private WebhookSchedule webhookSchedule;

    @Autowired
    private FilterCachedRepository filterCachedRepository;

    @Autowired
    ProjectFilterConfigurationCachedRepository projectRepository;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private JiraProperties jiraProperties;

    @RequestMapping(value = "{projectKey}", method = RequestMethod.POST)
    public void webhook(@RequestBody Map<String, Object> body, @PathVariable("projectKey") String projectKey) throws JsonProcessingException {
        log.debug("WEBHOOK REQUEST BODY: " + mapper.writeValueAsString(body));

        String webhookEvent = body.get("webhookEvent").toString().replace("jira:", "");
        String issueKey = getIssueKeyOrNull(body);
        Long issueTypeId = getIssueTypeIdOrNull(body);

        addItemInTheQueue(webhookEvent, projectKey, issueTypeId, issueKey);
    }

    @RequestMapping(path = "webhook/{webhookEvent}/{projectKey}/{issueTypeId}/{issueKey}", method = RequestMethod.POST)
    public void webhook(@PathVariable("webhookEvent") String webhookEvent,
            @PathVariable("projectKey") String projectKey, @PathVariable("issueTypeId") String issueTypeId,
            @PathVariable("issueKey") String issueKey) throws JsonProcessingException {
        log.debug("WEBHOOK PATH: (" + webhookEvent + ") project=" + projectKey + " issueTypeId=" + issueTypeId + " issue=" + issueKey);

        Long issueTypeIdLong = Long.parseLong(issueTypeId);
        addItemInTheQueue(webhookEvent, projectKey, issueTypeIdLong, issueKey);
    }

    private void addItemInTheQueue(String webhookEvent, String projectKey, Long issueTypeId, String issueKey) {
        if (!belongsToAnyProjectFilter(projectKey)) {
            log.debug("WEBHOOK PATH: project=" + projectKey + " issueTypeId=" + issueTypeId + " issue=" + issueKey + " doesn't belog to our projects.");
            return;
        }

        if (issueTypeId != null && !belongsToAnyIssueTypeFilter(issueTypeId)) {
            log.debug("WEBHOOK PATH: project=" + projectKey + " issueTypeId=" + issueTypeId + " issue=" + issueKey + " issue type not allowed.");
            return;
        }

        WebhookEvent event = WebhookEvent.valueOf(webhookEvent.toUpperCase());

        if (event.isTypeVersion() && !isReleaseConfigured())
            return;

        webhookSchedule.add(event, issueKey);
        log.info("WEBHOOK PUT IN QUEUE: (" + webhookEvent +  ") project=" + projectKey + " issue=" + issueKey);
    }

    private boolean belongsToAnyProjectFilter(String projectKey) {
        List<ProjectFilterConfiguration> projects = projectRepository.getProjects();
        return projects.stream().anyMatch(p -> p.getProjectKey().equals(projectKey));
    }

    private boolean belongsToAnyIssueTypeFilter(Long issueTypeId) {
        List<Filter> filters = filterCachedRepository.getCache();
        return filters.stream().anyMatch(f -> issueTypeId.equals(f.getIssueTypeId()));
    }

    private boolean isReleaseConfigured() {
        return !jiraProperties.getCustomfield().getRelease().getId().isEmpty();
    }

    private String getIssueKeyOrNull(Map<String, Object> body) {
        Map<String, Object> issue = toMap(body.get("issue"));
        return issue == null ? null : issue.get("key").toString();
    }

    private Long getIssueTypeIdOrNull(Map<String, Object> body) {
        Map<String, Object> issueType = toMap(getIssueFieldOrNull(body, "issuetype"));
        return issueType == null ? null : Long.parseLong(issueType.get("id").toString());
    }

    private Object getIssueFieldOrNull(Map<String, Object> requestBody, String field) {
        Map<String, Object> issue = toMap(requestBody.get("issue"));
        if (issue == null)
            return null;

        Map<String, Object> fields = toMap(issue.get("fields"));
        return fields.get(field);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(Object obj) {
        return (Map<String, Object>) obj;
    }

}
