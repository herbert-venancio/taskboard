package objective.taskboard.task;

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

import objective.taskboard.issueBuffer.IssueBufferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RefreshIssueBufferTask implements ApplicationListener<ContextRefreshedEvent> {

    private static final long RATE_MILISECONDS = 15 * 60 * 1000; 

    @Autowired
    private IssueBufferService issueBufferService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
    }

    @Scheduled(fixedRate = RATE_MILISECONDS, initialDelay = 0)
    public void updateIssueBuffer() {
        System.out.println("UPDATING ISSUE BUFFER");
        issueBufferService.updateIssueBuffer();
    }
}
