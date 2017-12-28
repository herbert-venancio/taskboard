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
package objective.taskboard.followup.impl;

import static java.util.Arrays.asList;
import static objective.taskboard.issueBuffer.IssueBufferState.ready;
import static objective.taskboard.issueBuffer.IssueBufferState.updateError;
import static objective.taskboard.issueBuffer.IssueBufferState.updating;

import java.io.IOException;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.FollowUpDataHistoryGenerator;
import objective.taskboard.followup.FollowUpDataHistoryRepository;
import objective.taskboard.followup.FollowupData;
import objective.taskboard.issueBuffer.IssueBufferState;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;

@Component
public class FollowUpDataHistoryGeneratorJSONFiles implements FollowUpDataHistoryGenerator {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FollowUpDataHistoryGeneratorJSONFiles.class);
    private static final List<IssueBufferState> ISSUE_BUFFER_STATES_READY = asList(ready, updating, updateError);
    private static final long MINUTE = 60 * 1000L;
    private static final long SLEEP_TIME_IN_MINUTES = 5L;

    private final ProjectFilterConfigurationCachedRepository projectFilterCacheRepo;
    private final FollowUpDataProviderFromCurrentState providerFromCurrentState;
    private final FollowUpDataHistoryRepository historyRepository;

    private boolean isExecutingDataHistoryGenerate = false;
    
    @Autowired
    public FollowUpDataHistoryGeneratorJSONFiles(
            ProjectFilterConfigurationCachedRepository projectFilterCacheRepo,
            FollowUpDataProviderFromCurrentState providerFromCurrentState,
            FollowUpDataHistoryRepository historyRepository) {
        this.projectFilterCacheRepo = projectFilterCacheRepo;
        this.providerFromCurrentState = providerFromCurrentState;
        this.historyRepository = historyRepository;
    }

    @PostConstruct
    public void initialize() {
        scheduledGenerate();
    }

    @Override
    @Scheduled(cron = "${jira.followup.executionDataHistoryGenerator.cron}", zone = "${jira.followup.executionDataHistoryGenerator.timezone}")
    public void scheduledGenerate() {
        if (isExecutingDataHistoryGenerate)
            return;

        isExecutingDataHistoryGenerate = true;
        Thread thread = new Thread(() -> {
            try {
                generate();
            } catch (IOException|InterruptedException e ) {
                throw new RuntimeException(e);
            } finally {
                isExecutingDataHistoryGenerate = false;
            }
        });
        thread.setName(getClass().getSimpleName());
        thread.setDaemon(true);
        thread.start();
    }

    protected void generate() throws IOException, InterruptedException {
        while (!ISSUE_BUFFER_STATES_READY.contains(providerFromCurrentState.getFollowupState())) {
            log.debug("Waiting for updateAllIssuesBuffer...");
            Thread.sleep(SLEEP_TIME_IN_MINUTES * MINUTE);
        }

        log.info(getClass().getSimpleName() + " start");
        for (ProjectFilterConfiguration pf : projectFilterCacheRepo.getProjects()) {
            String projectKey = pf.getProjectKey();

            log.info("Generating history of project " + projectKey);
            FollowupData jiraData = providerFromCurrentState.getJiraData(projectKey);

            historyRepository.save(projectKey, jiraData);
            
            log.info("History of project " + projectKey + " generated");

        }
        log.info(getClass().getSimpleName() + " complete");
    }
}
