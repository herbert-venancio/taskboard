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

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.walk;
import static objective.taskboard.issueBuffer.IssueBufferState.ready;
import static objective.taskboard.utils.IOUtilities.write;
import static objective.taskboard.utils.ZipUtils.zip;
import static org.apache.commons.io.FileUtils.deleteQuietly;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.extern.slf4j.Slf4j;
import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.FollowUpData;
import objective.taskboard.followup.FollowUpDataHistoryGenerator;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;

@Slf4j
@Component
public class FollowUpDataHistoryGeneratorJSONFiles implements FollowUpDataHistoryGenerator {

    public static final String PATH_FOLLOWUP_HISTORY = "data/followup-history/";
    public static final String FILE_NAME_FORMAT = "yyyyMMdd";
    public static final String EXTENSION_JSON = ".json";
    public static final String EXTENSION_ZIP = ".zip";

    private static final long MINUTE = 60 * 1000l;
    private static final long SLEEP_TIME_IN_MINUTES = 5l;

    @Autowired
    private ProjectFilterConfigurationCachedRepository projectFilterCacheRepo;
    
    @Autowired
    private FollowUpDataProviderFromCurrentState providerFromCurrentState;

    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private boolean isExecutingDataHistoryGenerate = false;

    @PostConstruct
    public void initialize() {
        scheduledGenerate();
    }

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
        thread.setName("FollowUpDataHistoryGeneratorJSONFiles.generate()");
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void generate() throws IOException, InterruptedException {
        while (!ready.equals(providerFromCurrentState.getFollowupState())) {
            log.debug("Waiting for updateAllIssuesBuffer...");
            Thread.sleep(SLEEP_TIME_IN_MINUTES * MINUTE);
        }

        log.info("FollowUpDataHistoryGeneratorJSONFiles start");
        for (ProjectFilterConfiguration pf : projectFilterCacheRepo.getProjects()) {
            String projectKey = pf.getProjectKey();

            Path pathProject = Paths.get(PATH_FOLLOWUP_HISTORY, projectKey);
            if (!pathProject.toFile().exists())
                createDirectories(pathProject);

            String todayString = DateTime.now().toString(FILE_NAME_FORMAT);
            Path pathJSON = pathProject.resolve(todayString + EXTENSION_JSON);

            try {
                List<FollowUpData> jiraData = providerFromCurrentState.getJiraData(projectKey.split(","));
                write(pathJSON.toFile(), gson.toJson(jiraData));

                Path pathZIP = Paths.get(pathJSON.toString() + EXTENSION_ZIP);
                deleteQuietly(pathZIP.toFile());
                zip(pathJSON, pathZIP);
            } finally {
                deleteQuietly(pathJSON.toFile());
            }
        }
        log.info("FollowUpDataHistoryGeneratorJSONFiles complete");
    }

    @Override
    public List<String> getHistoryByProject(String project) {
        List<String> history = new ArrayList<String>();

        Path pathProject = Paths.get(PATH_FOLLOWUP_HISTORY, project);
        if (!pathProject.toFile().exists())
            return history;

        try {
            Iterable<Path> paths = walk(pathProject)::iterator;
            for (Path path : paths) {
                if (path.toFile().isDirectory())
                    continue;

                String fileName = path.toFile().getName().replace(EXTENSION_JSON + EXTENSION_ZIP, "");
                String todayString = DateTime.now().toString(FILE_NAME_FORMAT);
                if (todayString.equals(fileName))
                    continue;

                history.add(fileName);
            }
            Collections.sort(history);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return history;
    }

}
