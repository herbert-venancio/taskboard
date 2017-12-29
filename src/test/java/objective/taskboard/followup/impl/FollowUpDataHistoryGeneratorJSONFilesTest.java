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

import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.size;
import static java.util.Arrays.asList;
import static objective.taskboard.followup.FollowUpDataHistoryRepository.EXTENSION_JSON;
import static objective.taskboard.followup.FollowUpDataHistoryRepository.EXTENSION_ZIP;
import static objective.taskboard.followup.FollowUpDataHistoryRepository.FILE_NAME_FORMATTER;
import static objective.taskboard.followup.FollowUpHelper.followupEmptyV2;
import static objective.taskboard.followup.FollowUpHelper.followupExpectedV2;
import static objective.taskboard.followup.FollowUpHelper.getDefaultFollowupData;
import static objective.taskboard.followup.FollowUpHelper.getEmptyFollowupData;
import static objective.taskboard.issueBuffer.IssueBufferState.ready;
import static objective.taskboard.utils.IOUtilities.ENCODE_UTF_8;
import static objective.taskboard.utils.IOUtilities.asResource;
import static objective.taskboard.utils.ZipUtils.unzip;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import objective.taskboard.database.directory.DataBaseDirectory;
import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.FollowUpDataEntry;
import objective.taskboard.followup.FollowUpDataHistoryRepository;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;
import objective.taskboard.rules.TimeZoneRule;

public class FollowUpDataHistoryGeneratorJSONFilesTest {

    private static final String PROJECT_TEST = "PROJECT TEST";
    private static final String PROJECT_TEST_2 = "PROJECT TEST 2";
    private static final LocalDate TODAY_DATE = LocalDate.now();
    private static final String TODAY = TODAY_DATE.format(FILE_NAME_FORMATTER);

    @Rule
    public TimeZoneRule timeZoneRule = new TimeZoneRule("America/Sao_Paulo");

    private FollowUpDataProviderFromCurrentState providerFromCurrentState = mock(FollowUpDataProviderFromCurrentState.class);
    private ProjectFilterConfigurationCachedRepository projectFilterCacheRepo = mock(ProjectFilterConfigurationCachedRepository.class);
    private ProjectFilterConfiguration projectFilter = mock(ProjectFilterConfiguration.class);
    private ProjectFilterConfiguration projectFilter2 = mock(ProjectFilterConfiguration.class);
    private DataBaseDirectory dataBaseDirectory = mock(DataBaseDirectory.class);
    private FollowUpDataHistoryRepository historyRepository = new FollowUpDataHistoryRepository(dataBaseDirectory); //TODO mockar o repo
    private Path pathHistory;
    
    private FollowUpDataHistoryGeneratorJSONFiles subject = new FollowUpDataHistoryGeneratorJSONFiles(
            projectFilterCacheRepo, 
            providerFromCurrentState, 
            historyRepository);

    @Before
    public void before() throws IOException {
        when(providerFromCurrentState.getFollowupState()).thenReturn(ready);
        when(projectFilter.getProjectKey()).thenReturn(PROJECT_TEST);
        when(projectFilter2.getProjectKey()).thenReturn(PROJECT_TEST_2);
        pathHistory = createTempDirectory(getClass().getSimpleName());
        when(dataBaseDirectory.path(anyString())).thenReturn(pathHistory);
    }

    @Test
    public void whenHasOneProject_thenOneFileShouldBeGenerated() throws IOException, InterruptedException {
        when(projectFilterCacheRepo.getProjects()).thenReturn(asList(projectFilter));
        when(providerFromCurrentState.getJiraData(any())).thenReturn(new FollowUpDataEntry(TODAY_DATE, getDefaultFollowupData()));

        subject.generate();

        assertGeneratedFile(PROJECT_TEST, followupExpectedV2());
    }

    @Test
    public void whenProjectDoesNotHaveData_thenNoDataShouldBeGenerated() throws IOException, InterruptedException {
        when(projectFilterCacheRepo.getProjects()).thenReturn(asList(projectFilter));
        when(providerFromCurrentState.getJiraData(any())).thenReturn(new FollowUpDataEntry(TODAY_DATE, getEmptyFollowupData()));

        subject.generate();

        assertGeneratedFile(PROJECT_TEST, followupEmptyV2());
    }

    @Test
    public void whenHasTwoProjects_thenTwoFilesShouldBeGenerated() throws IOException, InterruptedException {
        when(projectFilterCacheRepo.getProjects()).thenReturn(asList(projectFilter, projectFilter2));
        when(providerFromCurrentState.getJiraData(any())).thenReturn(new FollowUpDataEntry(TODAY_DATE, getDefaultFollowupData()));

        subject.generate();

        assertGeneratedFile(PROJECT_TEST, followupExpectedV2());
        assertGeneratedFile(PROJECT_TEST_2, followupExpectedV2());
    }

    @After
    public void after() {
        deleteQuietly(pathHistory.toFile());
    }

    private void assertGeneratedFile(String project, String dataHistoryExpected) throws IOException {
        Path source = pathHistory.resolve(project).resolve(TODAY + EXTENSION_JSON + EXTENSION_ZIP);

        assertTrue("File should be exist", exists(source));
        assertThat(size(source), greaterThan(0L));

        Path destiny = createTempDirectory("FollowUpDataHistoryAssertTest");
        try {
            unzip(source.toFile(), destiny);
            Path pathJSON = destiny.resolve(TODAY + EXTENSION_JSON);
            String actual = IOUtils.toString(asResource(pathJSON).getInputStream(), ENCODE_UTF_8);

            assertEquals("Follow Up data history", dataHistoryExpected, actual);
        } finally {
            deleteQuietly(destiny.toFile());
        }
    }

}
