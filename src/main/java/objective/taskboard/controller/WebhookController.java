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
package objective.taskboard.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import objective.taskboard.jira.data.WebHookBody;
import objective.taskboard.task.IssueEventProcessScheduler;
import objective.taskboard.task.JiraEventProcessor;
import objective.taskboard.task.JiraEventProcessorFactory;

@RestController
@RequestMapping("webhook")
public class WebhookController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WebhookController.class);

    @Autowired
    private IssueEventProcessScheduler webhookSchedule;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private List<JiraEventProcessorFactory> jiraEventProcessorFactories;

    @RequestMapping(value = "{projectKey}", method = RequestMethod.POST)
    public void webhook(@RequestBody WebHookBody body, @PathVariable("projectKey") String projectKey) throws JsonProcessingException {
        log.debug("WEBHOOK REQUEST BODY: " + mapper.writeValueAsString(body));

        if(body.webhookEvent == null)
            return;

        for(JiraEventProcessorFactory factory : jiraEventProcessorFactories) {
            enqueue(factory.create(body, projectKey));
        }
    }

    private void enqueue(JiraEventProcessor eventProcessor) {
        if(eventProcessor != null)
            webhookSchedule.add(eventProcessor);
    }
}
