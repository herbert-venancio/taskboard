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
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.size;
import static java.util.Arrays.asList;
import static objective.taskboard.followup.FollowUpHelper.getDefaultFollowupData;
import static objective.taskboard.followup.FollowUpHelper.getEmptyFollowupData;
import static objective.taskboard.followup.impl.FollowUpDataHistoryGeneratorJSONFiles.EXTENSION_JSON;
import static objective.taskboard.followup.impl.FollowUpDataHistoryGeneratorJSONFiles.EXTENSION_ZIP;
import static objective.taskboard.followup.impl.FollowUpDataHistoryGeneratorJSONFiles.FILE_NAME_FORMAT;
import static objective.taskboard.followup.impl.FollowUpDataHistoryGeneratorJSONFiles.TODAY;
import static objective.taskboard.issueBuffer.IssueBufferState.ready;
import static objective.taskboard.utils.IOUtilities.ENCODE_UTF_8;
import static objective.taskboard.utils.IOUtilities.asResource;
import static objective.taskboard.utils.IOUtilities.resourceToString;
import static objective.taskboard.utils.ZipUtils.unzip;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.database.directory.DataBaseDirectory;
import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;

@RunWith(MockitoJUnitRunner.class)
public class FollowUpDataHistoryGeneratorJSONFilesTest {

    private static final String PROJECT_TEST = "PROJECT TEST";
    private static final String PROJECT_TEST_2 = "PROJECT TEST 2";
    private static final String YESTERDAY = DateTime.now().minusDays(1).toString(FILE_NAME_FORMAT);

    @InjectMocks
    private FollowUpDataHistoryGeneratorJSONFiles subject;

    @Mock
    private FollowUpDataProviderFromCurrentState providerFromCurrentState;

    @Mock
    private ProjectFilterConfigurationCachedRepository projectFilterCacheRepo;

    @Mock
    private ProjectFilterConfiguration projectFilter;

    @Mock
    private ProjectFilterConfiguration projectFilter2;

    @Mock
    private DataBaseDirectory dataBaseDirectory;

    @Before
    public void before() throws IOException {
        when(providerFromCurrentState.getFollowupState()).thenReturn(ready);
        when(projectFilter.getProjectKey()).thenReturn(PROJECT_TEST);
        when(projectFilter2.getProjectKey()).thenReturn(PROJECT_TEST_2);
        Path pathHistoryTemp = createTempDirectory(getClass().getSimpleName());
        when(dataBaseDirectory.path(anyString())).thenReturn(pathHistoryTemp);
    }

    @Test
    public void whenHasOneProject_thenOneFileShouldBeGenerated() throws IOException, InterruptedException {
        when(projectFilterCacheRepo.getProjects()).thenReturn(asList(projectFilter));
        when(providerFromCurrentState.getJiraData(any())).thenReturn(getDefaultFollowupData());

        subject.generate();

        String dataHistoryExpected = resourceToString(getClass(), "followUpDataHistoryExpected.json");
        assertGeneratedFile(PROJECT_TEST, dataHistoryExpected);
    }

    @Test
    public void whenProjectDoesNotHaveData_thenNoDataShouldBeGenerated() throws IOException, InterruptedException {
        when(projectFilterCacheRepo.getProjects()).thenReturn(asList(projectFilter));
        when(providerFromCurrentState.getJiraData(any())).thenReturn(getEmptyFollowupData());

        subject.generate();

        assertGeneratedFile(PROJECT_TEST, "[]");
    }

    @Test
    public void whenHasTwoProjects_thenTwoFilesShouldBeGenerated() throws IOException, InterruptedException {
        when(projectFilterCacheRepo.getProjects()).thenReturn(asList(projectFilter, projectFilter2));
        when(providerFromCurrentState.getJiraData(any())).thenReturn(getDefaultFollowupData());

        subject.generate();

        String dataHistoryExpected = resourceToString(getClass(), "followUpDataHistoryExpected.json");
        assertGeneratedFile(PROJECT_TEST, dataHistoryExpected);
        assertGeneratedFile(PROJECT_TEST_2, dataHistoryExpected);
    }

    @Test
    public void givenProjectWithNoHistory_whenGetHistoryByProject_thenReturnNoData() {
        List<String> history = subject.getHistoryByProject(PROJECT_TEST);
        assertTrue("History should be empty", history.isEmpty());
    }

    @Test
    public void givenProjectWithTodaysHistory_whenGetHistoryByProject_thenReturnNoData() throws IOException {
        Path pathProject = dataBaseDirectory.path(anyString()).resolve(PROJECT_TEST);
        createDirectories(pathProject);
        Path pathZip = pathProject.resolve(TODAY + EXTENSION_JSON + EXTENSION_ZIP);
        createFile(pathZip);
        assertTrue("File should be exist", exists(pathZip));

        List<String> history = subject.getHistoryByProject(PROJECT_TEST);
        assertTrue("History should be empty", history.isEmpty());
    }

    @Test
    public void givenProjectWithYesterdaysHistory_whenGetHistoryByProject_thenReturnData() throws IOException {
        Path pathProject = dataBaseDirectory.path(anyString()).resolve(PROJECT_TEST);
        createDirectories(pathProject);
        Path pathZip = pathProject.resolve(YESTERDAY + EXTENSION_JSON + EXTENSION_ZIP);
        createFile(pathZip);
        assertTrue("File should be exist", exists(pathZip));

        List<String> history = subject.getHistoryByProject(PROJECT_TEST);
        assertEquals("History size", 1, history.size());
        assertEquals("First history", YESTERDAY, history.get(0));
    }

    @After
    public void after() {
        deleteQuietly(dataBaseDirectory.path(anyString()).toFile());
    }

    private void assertGeneratedFile(String project, String dataHistoryExpected) throws IOException {
        Path source = dataBaseDirectory.path(anyString()).resolve(project).resolve(TODAY + EXTENSION_JSON + EXTENSION_ZIP);

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
