package objective.taskboard.task;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import objective.taskboard.issueBuffer.IssueBufferService;
import objective.taskboard.issueBuffer.IssueEvent;

@Slf4j
@Component
public class WebhookSchedule {

    private static final long RATE_MILISECONDS = 20 * 1000;

    List<Item> list = Collections.synchronizedList(new ArrayList<Item>());

    @Autowired
    private IssueBufferService issueBufferService;

    private class Item {
        private IssueEvent event;
        private String issueKey;

        public Item(IssueEvent event, String issueKey) {
            this.event = event;
            this.issueKey = issueKey;
        }
    }

    private synchronized List<Item> getItens() {
        return new ArrayList<Item>(list);
    }

    private synchronized void removeItens(List<Item> list) {
        this.list.removeAll(list);
    }

    public void add(IssueEvent event, String issueKey) {
        Item item = new Item(event, issueKey);
        list.add(item);
    }

    @Scheduled(fixedRate = RATE_MILISECONDS)
    public void processItems() {
        List<Item> toRemove = new ArrayList<Item>();

        for (Item item : getItens()) {
            try {
                issueBufferService.updateIssueBuffer(item.event, item.issueKey);
                log.warn("WEBHOOK PROCESSED: (" + item.event.toString() +  ") issue=" + item.issueKey);
                toRemove.add(item);
            } catch (Exception ex) {
                log.error("WebhookScheduleError", ex);
            }
        }

        removeItens(toRemove);
    }

}
