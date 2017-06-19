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
import objective.taskboard.issueBuffer.IssueEvent;
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

    @RequestMapping(value = "{projectKey}/{issueKey}", method = RequestMethod.POST)
    public void webhook(@RequestBody Map<String, Object> body, @PathVariable("projectKey") String projectKey, @PathVariable("issueKey") String issueKey) throws JsonProcessingException {
        String webhookEvent = body.get("webhookEvent").toString().replace("jira:", "");
        Long issueTypeId = getIssueTypeId(body);

        addItemInTheQueue(webhookEvent, projectKey, issueTypeId, issueKey);
        log.debug("WEBHOOK REQUEST BODY: " + mapper.writeValueAsString(body));

//        webhookSchedule.processItems();
    }

    @RequestMapping(path = "webhook/{webhookEvent}/{projectKey}/{issueTypeId}/{issueKey}", method = RequestMethod.POST)
    public void webhook(@PathVariable("webhookEvent") String webhookEvent,
            @PathVariable("projectKey") String projectKey, @PathVariable("issueTypeId") String issueTypeId,
            @PathVariable("issueKey") String issueKey) throws JsonProcessingException {

        log.info("WEBHOOK PUT IN QUEUE: (" + webhookEvent + ") project=" + projectKey + " issue=" + issueKey);

        Long issueTypeIdLong = Long.parseLong(issueTypeId);
        addItemInTheQueue(webhookEvent, projectKey, issueTypeIdLong, issueKey);
    }

    private void addItemInTheQueue(String webhookEvent, String projectKey, Long issueTypeId, String issueKey) {
        if (!belongsToAnyProjectFilter(projectKey))
            return;

        if (!belongsToAnyIssueTypeFilter(issueTypeId))
            return;

        IssueEvent event = IssueEvent.valueOf(webhookEvent.toUpperCase());

        webhookSchedule.add(event, issueKey);
        log.info("WEBHOOK PUT IN QUEUE: (" + webhookEvent +  ") project=" + projectKey + " issue=" + issueKey);
    }

    private boolean belongsToAnyProjectFilter(String projectKey) {
        List<ProjectFilterConfiguration> projects = projectRepository.getProjects();
        return projects.stream().anyMatch(p -> p.getProjectKey().equals(projectKey));
    }

    private boolean belongsToAnyIssueTypeFilter(Long issueTypeId) {
        List<Filter> filters = filterCachedRepository.getCache();
        return filters.stream().anyMatch(f -> {
            return issueTypeId.equals(f.getIssueTypeId());
        });
    }

    private Long getIssueTypeId(Map<String, Object> requestBody) {
        Map<String, Object> issue = toMap(requestBody.get("issue"));
        Map<String, Object> fields = toMap(issue.get("fields"));
        Map<String, Object> type = toMap(fields.get("issuetype"));
        return Long.parseLong(type.get("id").toString());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(Object obj) {
        return (Map<String, Object>) obj;
    }

}
