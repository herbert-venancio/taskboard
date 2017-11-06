/*-
 * [LICENSE]
 * Taskboard
 * ---
 * Copyright (C) 2015 - 2017 Objective Solutions
 * ---
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
package objective.taskboard.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import objective.taskboard.controller.WebhookController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import objective.taskboard.issueBuffer.IssueBufferService;
import objective.taskboard.issueBuffer.WebhookEvent;
import objective.taskboard.jira.JiraServiceUnavailable;

@Component
public class IssueEventProcessScheduler {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IssueEventProcessScheduler.class);
    @Autowired
    private IssueBufferService issueBufferService;

    @Autowired
    private Set<IssueEventProcessor> issueEventProcessors;

    private static final long RATE_MILISECONDS = 1000l;
    
    List<IssueEventProcessor.IssueEvent> list = Collections.synchronizedList(new ArrayList<>());

    private synchronized List<IssueEventProcessor.IssueEvent> getItens() {
        return new ArrayList<>(list);
    }

    private synchronized void removeItens(List<IssueEventProcessor.IssueEvent> list) {
        this.list.removeAll(list);
    }

    public synchronized void add(WebhookEvent event, String projectKey, String issueKey, WebhookController.WebhookBody eventData) {
        IssueEventProcessor.IssueEvent item = new IssueEventProcessor.IssueEvent(event, projectKey, issueKey, eventData);
        list.add(item);
    }

    @Scheduled(fixedRate = RATE_MILISECONDS)
    public void processItems() {
        List<IssueEventProcessor.IssueEvent> toRemove = new ArrayList<>();

        issueBufferService.startBatchUpdate();
        for (IssueEventProcessor.IssueEvent item : getItens())
            try {
                processEvent(item);

                toRemove.add(item);
            }
            catch(JiraServiceUnavailable ex) {
                log.warn("WEBHOOK PROCESS FAILED FOR ITEM: (" + item.event.toString() +  ") issue=" + item.issueKey);
                log.warn("Jira was not available. Will retry later", ex);
            }
            catch (Exception ex) {
                log.warn("WEBHOOK PROCESS FAILED: (" + item.event.toString() +  ") issue=" + item.issueKey);
                log.error("WebhookScheduleError, unrecoverable error. Event will be discarded:", ex);
                toRemove.add(item);
            }

        issueBufferService.finishBatchUpdate();
        removeItens(toRemove);
    }

    private void processEvent(IssueEventProcessor.IssueEvent item) {
        for(IssueEventProcessor eventProcessor : issueEventProcessors) {
            if(eventProcessor.processEvent(item))
                break;
        }
    }

}
