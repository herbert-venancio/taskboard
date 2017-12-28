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

package objective.taskboard.followup;

import static java.nio.file.Files.copy;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.size;
import static objective.taskboard.followup.FollowUpDataHistoryRepository.EXTENSION_JSON;
import static objective.taskboard.followup.FollowUpDataHistoryRepository.EXTENSION_ZIP;
import static objective.taskboard.followup.FollowUpDataHistoryRepository.FILE_NAME_FORMAT;
import static objective.taskboard.followup.FollowUpHelper.assertFollowUpDataV0;
import static objective.taskboard.followup.FollowUpHelper.followupEmptyV2;
import static objective.taskboard.followup.FollowUpHelper.followupExpectedV2;
import static objective.taskboard.followup.FollowUpHelper.getDefaultFollowupData;
import static objective.taskboard.followup.FollowUpHelper.getEmptyFollowupData;
import static objective.taskboard.utils.IOUtilities.ENCODE_UTF_8;
import static objective.taskboard.utils.IOUtilities.asResource;
import static objective.taskboard.utils.ZipUtils.unzip;
import static objective.taskboard.utils.ZipUtils.zip;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import objective.taskboard.database.directory.DataBaseDirectory;
import objective.taskboard.rules.TimeZoneRule;
import objective.taskboard.utils.DateTimeUtils;

public class FollowUpDataHistoryRepositoryTest {

    private static final ZoneId TIMEZONE = ZoneId.systemDefault();
    private static final String PROJECT_TEST = "PROJECT TEST";
    private static final String PROJECT_TEST_2 = "PROJECT TEST 2";
    private static final String TODAY = DateTime.now().toString(FILE_NAME_FORMAT);
    private static final String YESTERDAY = DateTime.now().minusDays(1).toString(FILE_NAME_FORMAT);
    private static final String BEFORE_YESTERDAY = DateTime.now().minusDays(2).toString(FILE_NAME_FORMAT);

    private final DataBaseDirectory dataBaseDirectory = mock(DataBaseDirectory.class);
    private final FollowUpDataHistoryRepository subject = new FollowUpDataHistoryRepository(dataBaseDirectory);
    
    @Rule
    public TimeZoneRule timeZoneRule = new TimeZoneRule("America/Sao_Paulo");
    
    @Before
    public void before() throws IOException {
        Path pathHistoryTemp = createTempDirectory(getClass().getSimpleName());
        when(dataBaseDirectory.path(anyString())).thenReturn(pathHistoryTemp);
    }
    
    @After
    public void after() {
        deleteQuietly(dataBaseDirectory.path(anyString()).toFile());
    }

    @Test
    public void whenSaveOneProject_thenOneFileShouldBeGenerated() throws IOException, InterruptedException {
        subject.save(PROJECT_TEST, getDefaultFollowupData());

        assertGeneratedFile(PROJECT_TEST, followupExpectedV2());
    }

    @Test
    public void whenSaveEmptyData_thenNoDataShouldBeGenerated() throws IOException, InterruptedException {
        subject.save(PROJECT_TEST, getEmptyFollowupData());

        assertGeneratedFile(PROJECT_TEST, followupEmptyV2());
    }

    @Test
    public void whenSaveTwoProjects_thenTwoFilesShouldBeGenerated() throws IOException, InterruptedException {
        subject.save(PROJECT_TEST, getDefaultFollowupData());
        subject.save(PROJECT_TEST_2, getDefaultFollowupData());

        assertGeneratedFile(PROJECT_TEST, followupExpectedV2());
        assertGeneratedFile(PROJECT_TEST_2, followupExpectedV2());
    }

    @Test
    public void givenProjectWithNoHistory_whenGetHistoryGivenProjects_thenReturnNoData() {
        List<String> history = subject.getHistoryGivenProjects(PROJECT_TEST);
        assertTrue("History should be empty", history.isEmpty());
    }

    @Test
    public void givenProjectWithTodaysHistory_whenGetHistoryGivenProjects_thenReturnNoData() throws IOException {
        Path pathProject = dataBaseDirectory.path(anyString()).resolve(PROJECT_TEST);
        createDirectories(pathProject);
        Path pathZip = pathProject.resolve(TODAY + EXTENSION_JSON + EXTENSION_ZIP);
        createFile(pathZip);
        assertTrue("File should be exist", exists(pathZip));

        List<String> history = subject.getHistoryGivenProjects(PROJECT_TEST);
        assertTrue("History should be empty", history.isEmpty());
    }

    @Test
    public void givenProjectWithYesterdaysHistory_whenGetHistoryGivenProjects_thenReturnData() throws IOException {
        Path pathProject = dataBaseDirectory.path(anyString()).resolve(PROJECT_TEST);
        createDirectories(pathProject);
        Path pathZip = pathProject.resolve(YESTERDAY + EXTENSION_JSON + EXTENSION_ZIP);
        createFile(pathZip);
        assertTrue("File should be exist", exists(pathZip));

        List<String> history = subject.getHistoryGivenProjects(PROJECT_TEST);
        assertEquals("History size", 1, history.size());
        assertEquals("First history", YESTERDAY, history.get(0));
    }

    @Test
    public void whenGetHistoryGivenNoProjects_thenReturnNoData() throws IOException {
        List<String> history = subject.getHistoryGivenProjects();
        assertEquals(0, history.size());
    }

    @Test
    public void givenProjectsWithVariedHistory_whenGetHistoryGivenProjects_thenReturnOnlyIntersection() throws IOException {
        Path pathProject = dataBaseDirectory.path(anyString()).resolve(PROJECT_TEST);
        createDirectories(pathProject);
        createFile(pathProject.resolve(YESTERDAY + EXTENSION_JSON + EXTENSION_ZIP));
        createFile(pathProject.resolve(BEFORE_YESTERDAY + EXTENSION_JSON + EXTENSION_ZIP));
        
        Path pathProject2 = dataBaseDirectory.path(anyString()).resolve(PROJECT_TEST_2);
        createDirectories(pathProject2);
        createFile(pathProject2.resolve(BEFORE_YESTERDAY + EXTENSION_JSON + EXTENSION_ZIP));

        List<String> history = subject.getHistoryGivenProjects(PROJECT_TEST, PROJECT_TEST_2);
        assertEquals("History size", 1, history.size());
        assertEquals("First history", BEFORE_YESTERDAY, history.get(0));
    }
    
    @Test
    public void givenProjectsWithDistinctHistory_whenGetHistoryGivenProjects_thenReturnNoData() throws IOException {
        Path pathProject = dataBaseDirectory.path(anyString()).resolve(PROJECT_TEST);
        createDirectories(pathProject);
        createFile(pathProject.resolve(YESTERDAY + EXTENSION_JSON + EXTENSION_ZIP));
        
        Path pathProject2 = dataBaseDirectory.path(anyString()).resolve(PROJECT_TEST_2);
        createDirectories(pathProject2);
        createFile(pathProject2.resolve(BEFORE_YESTERDAY + EXTENSION_JSON + EXTENSION_ZIP));

        List<String> history = subject.getHistoryGivenProjects(PROJECT_TEST, PROJECT_TEST_2);
        assertTrue("History should be empty", history.isEmpty());
    }


    @Test
    public void whenHasDataHistory_GetShouldReturnSomeData() throws IOException, URISyntaxException {
        createProjectZipV0(PROJECT_TEST);

        List<FromJiraDataRow> jiraData = subject.get(YESTERDAY, TIMEZONE, PROJECT_TEST).fromJiraDs.rows;

        assertEquals("Jira data size", jiraData.size(), 1);
        assertFollowUpDataV0(jiraData.get(0));
    }

    @Test
    public void whenDoesNotHaveDataHistory_GetShouldThrowAnIllegalStateException() {
        try {
            subject.get(YESTERDAY, TIMEZONE, PROJECT_TEST);
            fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            assertNotNull("IllegalStateException shouldn't be null", e);
        }
    }

    @Test
    public void whenHasInvalidDataHistory_GetShouldThrowAnIllegalStateException() throws URISyntaxException {
        try {
            Path pathJSON = Paths.get(getClass().getResource("impl/V1_followUpDataHistoryExpected.json").toURI());
            Path zipFile = dataBaseDirectory.path(anyString()).resolve(PROJECT_TEST).resolve(YESTERDAY + EXTENSION_JSON + EXTENSION_ZIP);
            zip(pathJSON, zipFile);

            subject.get(YESTERDAY, TIMEZONE, PROJECT_TEST);
            fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            assertNotNull("IllegalStateException shouldn't be null", e);
        }
    }

    @Test
    public void whenTwoProjectsHasDataHistory_GetShouldReturnSomeData() throws IOException,  URISyntaxException {
        createProjectZipV0(PROJECT_TEST);
        createProjectZipV0(PROJECT_TEST_2);

        List<FromJiraDataRow> jiraData = subject.get(YESTERDAY, TIMEZONE, PROJECT_TEST, PROJECT_TEST_2).fromJiraDs.rows;

        assertEquals("Jira data size", jiraData.size(), 2);
        assertFollowUpDataV0(jiraData.get(0));
        assertFollowUpDataV0(jiraData.get(1));
    }

    @Test
    public void whenHasDataHistoryWithCfd_GetShouldRestoreSyntheticsDataSources() throws IOException, URISyntaxException {
        // given
        createProjectZipV2(PROJECT_TEST);

        // when
        FollowupData data = subject.get(YESTERDAY, TIMEZONE, PROJECT_TEST);

        // then
        assertThat(data).isEqualToComparingFieldByFieldRecursively(getDefaultFollowupData());
    }

    @Test
    public void testVersion2() throws IOException, URISyntaxException {
        // given
        createProjectZipV2(PROJECT_TEST);

        // when
        FollowupData data = subject.get(YESTERDAY, TIMEZONE, PROJECT_TEST);

        // then
        assertThat(data).isEqualToComparingFieldByFieldRecursively(getDefaultFollowupData());
    }

    @Test
    public void whenHasDataHistoryWithCfdAndPassesZoneId_GetShouldRecalculateAnalyticsAndSynthetics() throws IOException, URISyntaxException {
        // given
        createProjectZipV1(PROJECT_TEST);
        ZoneId saoPauloTZ = ZoneId.of("America/Sao_Paulo"); // -03:00, no conversion
        ZoneId torontoTZ = ZoneId.of("America/Toronto"); // -04:00, changes date and hours
        ZoneId sydneyTZ = ZoneId.of("Australia/Sydney"); // +10:00, same day, different hours

        // when
        FollowupData dataSaoPaulo = subject.get(YESTERDAY, saoPauloTZ, PROJECT_TEST);
        FollowupData dataToronto = subject.get(YESTERDAY, torontoTZ, PROJECT_TEST);
        FollowupData dataSydney = subject.get(YESTERDAY, sydneyTZ, PROJECT_TEST);

        // then
        List<ZonedDateTime> saoPauloAnalyticsDates = dataSaoPaulo.analyticsTransitionsDsList.get(0).rows.get(0).transitionsDates;
        assertEquals(saoPauloAnalyticsDates.get(0), DateTimeUtils.parseDateTime("2017-09-27", "00:00:00", saoPauloTZ));
        assertEquals(saoPauloAnalyticsDates.get(1), DateTimeUtils.parseDateTime("2017-09-26", "00:00:00", saoPauloTZ));
        assertEquals(saoPauloAnalyticsDates.get(2), DateTimeUtils.parseDateTime("2017-09-25", "00:00:00", saoPauloTZ));

        List<ZonedDateTime> torontoAnalyticsDates = dataToronto.analyticsTransitionsDsList.get(0).rows.get(0).transitionsDates;
        assertEquals(torontoAnalyticsDates.get(0), DateTimeUtils.parseDateTime("2017-09-26", "23:00:00", torontoTZ));
        assertEquals(torontoAnalyticsDates.get(1), DateTimeUtils.parseDateTime("2017-09-25", "23:00:00", torontoTZ));
        assertEquals(torontoAnalyticsDates.get(2), DateTimeUtils.parseDateTime("2017-09-24", "23:00:00", torontoTZ));

        List<ZonedDateTime> sydneyAnalyticsDates = dataSydney.analyticsTransitionsDsList.get(0).rows.get(0).transitionsDates;
        assertEquals(sydneyAnalyticsDates.get(0), DateTimeUtils.parseDateTime("2017-09-27", "13:00:00", sydneyTZ));
        assertEquals(sydneyAnalyticsDates.get(1), DateTimeUtils.parseDateTime("2017-09-26", "13:00:00", sydneyTZ));
        assertEquals(sydneyAnalyticsDates.get(2), DateTimeUtils.parseDateTime("2017-09-25", "13:00:00", sydneyTZ));

        List<SyntheticTransitionsDataRow> saoPauloSyntheticRows = dataSaoPaulo.syntheticsTransitionsDsList.get(0).rows;
        assertEquals(saoPauloSyntheticRows.get(0).date, DateTimeUtils.parseDate("2017-09-25", saoPauloTZ));
        assertEquals(saoPauloSyntheticRows.get(1).date, DateTimeUtils.parseDate("2017-09-25", saoPauloTZ));
        assertEquals(saoPauloSyntheticRows.get(2).date, DateTimeUtils.parseDate("2017-09-26", saoPauloTZ));
        assertEquals(saoPauloSyntheticRows.get(3).date, DateTimeUtils.parseDate("2017-09-26", saoPauloTZ));
        assertEquals(saoPauloSyntheticRows.get(4).date, DateTimeUtils.parseDate("2017-09-27", saoPauloTZ));
        assertEquals(saoPauloSyntheticRows.get(5).date, DateTimeUtils.parseDate("2017-09-27", saoPauloTZ));

        List<SyntheticTransitionsDataRow> torontoSyntheticRows = dataToronto.syntheticsTransitionsDsList.get(0).rows;
        assertEquals(torontoSyntheticRows.get(0).date, DateTimeUtils.parseDate("2017-09-24", torontoTZ));
        assertEquals(torontoSyntheticRows.get(1).date, DateTimeUtils.parseDate("2017-09-24", torontoTZ));
        assertEquals(torontoSyntheticRows.get(2).date, DateTimeUtils.parseDate("2017-09-25", torontoTZ));
        assertEquals(torontoSyntheticRows.get(3).date, DateTimeUtils.parseDate("2017-09-25", torontoTZ));
        assertEquals(torontoSyntheticRows.get(4).date, DateTimeUtils.parseDate("2017-09-26", torontoTZ));
        assertEquals(torontoSyntheticRows.get(5).date, DateTimeUtils.parseDate("2017-09-26", torontoTZ));

        List<SyntheticTransitionsDataRow> sydneySyntheticsRows = dataSydney.syntheticsTransitionsDsList.get(0).rows;
        assertEquals(sydneySyntheticsRows.get(0).date, DateTimeUtils.parseDate("2017-09-25", sydneyTZ));
        assertEquals(sydneySyntheticsRows.get(1).date, DateTimeUtils.parseDate("2017-09-25", sydneyTZ));
        assertEquals(sydneySyntheticsRows.get(2).date, DateTimeUtils.parseDate("2017-09-26", sydneyTZ));
        assertEquals(sydneySyntheticsRows.get(3).date, DateTimeUtils.parseDate("2017-09-26", sydneyTZ));
        assertEquals(sydneySyntheticsRows.get(4).date, DateTimeUtils.parseDate("2017-09-27", sydneyTZ));
        assertEquals(sydneySyntheticsRows.get(5).date, DateTimeUtils.parseDate("2017-09-27", sydneyTZ));
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

    private Path createProjectZipV0(String project) throws IOException, URISyntaxException {
        return createProjectZip(project, "V0_followUpDataHistoryExpected.json");
    }

    private Path createProjectZipV1(String project) throws IOException, URISyntaxException {
        return createProjectZip(project, "V1_followUpDataHistoryExpected.json");
    }

    private Path createProjectZipV2(String project) throws IOException, URISyntaxException {
        return createProjectZip(project, "V2_followUpDataHistoryExpected.json");
    }

    private Path createProjectZip(String project, String contentFile) throws IOException, URISyntaxException {
        Path pathProject = dataBaseDirectory.path(anyString()).resolve(project);
        createDirectories(pathProject);

        Path pathInputJSON = Paths.get(getClass().getResource("impl/" + contentFile).toURI());
        Path pathOutputJSON = pathProject.resolve(YESTERDAY + EXTENSION_JSON);
        copy(pathInputJSON, pathOutputJSON);

        Path zipFile = Paths.get(pathOutputJSON.toString() + EXTENSION_ZIP);
        zip(pathOutputJSON, zipFile);
        return pathProject;
    }
}
