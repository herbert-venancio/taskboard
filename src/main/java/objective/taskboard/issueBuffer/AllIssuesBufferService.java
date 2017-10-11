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
package objective.taskboard.issueBuffer;

import static java.util.stream.Collectors.toList;

import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.data.Issue;
import objective.taskboard.domain.converter.JiraIssueToIssueConverter;
import objective.taskboard.jira.JiraIssueService;

@Service
public class AllIssuesBufferService {
    private static final String ALL_ISSUES_CACHE = "all-issues.dat";

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AllIssuesBufferService.class);

    @Autowired
    private JiraIssueToIssueConverter issueConverter;

    @Autowired
    private JiraIssueService jiraIssueService;
    
    @Autowired
    private CardRepoService cardsRepoService;

    private CardRepo allCardsRepo;

    private boolean isUpdatingAllIssuesBuffer = false;

    private IssueBufferState state = IssueBufferState.uninitialised;
    
    public IssueBufferState getState() {
        return state;
    }
    
    @PostConstruct
    private synchronized void loadCache() {
        allCardsRepo = cardsRepoService.from(ALL_ISSUES_CACHE);
        if (allCardsRepo.size() > 0) 
            state = IssueBufferState.ready;
    }
    
    private synchronized void saveCache() {
        allCardsRepo.commit();
    }

    public synchronized void updateAllIssuesBuffer() {
        if (isUpdatingAllIssuesBuffer)
            return;
        
        isUpdatingAllIssuesBuffer = true;
        Thread thread = new Thread(() -> {
            try {
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();
                state = state.start();
                log.debug("updateAllIssuesBuffer start");
               
                IssueBufferServiceSearchVisitor searchVisitor = new IssueBufferServiceSearchVisitor(issueConverter, allCardsRepo);
                jiraIssueService.searchAllProjectIssues(searchVisitor, allCardsRepo);
                
                log.debug("All Buffer - Issues processed: " + searchVisitor.getProcessedCount());
                log.debug("updateAllIssuesBuffer complete");
                log.debug("updateAllIssuesBuffer time spent " +stopWatch.getTime());
                state = state.done();
                saveCache();
            }
            catch (Exception e) {
                state = state.error();
                throw e;
            }
            finally {
                isUpdatingAllIssuesBuffer = false;
            }
        });
        thread.setName("AllIssues.update");
        thread.setDaemon(true);
        thread.start();
    }

    public synchronized List<Issue> getAllIssues() {
        return allCardsRepo.values().stream()
                .collect(toList());
    }
}
