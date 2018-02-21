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
import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.size;
import static java.util.Arrays.asList;
import static objective.taskboard.followup.FollowUpDataHistoryRepository.EXTENSION_JSON;
import static objective.taskboard.followup.FollowUpDataHistoryRepository.EXTENSION_ZIP;
import static objective.taskboard.followup.FollowUpDataHistoryRepository.FILE_NAME_FORMATTER;
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
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import objective.taskboard.database.directory.DataBaseDirectory;
import objective.taskboard.domain.FollowupDailySynthesis;
import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.cluster.FollowUpClusterItem;
import objective.taskboard.repository.FollowupDailySynthesisJpaRepository;
import objective.taskboard.repository.FollowupDailySynthesisRepository;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;
import objective.taskboard.rules.TimeZoneRule;
import objective.taskboard.testUtils.JpaRepositoryMock;
import objective.taskboard.utils.DateTimeUtils;

public class FollowUpDataHistoryRepositoryTest {
    private static final ZoneId TIMEZONE = ZoneId.systemDefault();
    private static final String PROJECT_TEST = "PROJECT TEST";
    private static final String PROJECT_TEST_2 = "PROJECT TEST 2";
    private static final LocalDate TODAY_DATE = LocalDate.now();
    private static final String TODAY = TODAY_DATE.format(FILE_NAME_FORMATTER);
    private static final String YESTERDAY = TODAY_DATE.minusDays(1).format(FILE_NAME_FORMATTER);
    private static final String BEFORE_YESTERDAY = TODAY_DATE.minusDays(2).format(FILE_NAME_FORMATTER);

    private Path dataPath;
    private FollowUpDataHistoryRepository subject;
    
    private FollowupDailySynthesisJpaRepositoryImplementation dailySynthesisRepo;
    
    @Rule
    public TimeZoneRule timeZoneRule = new TimeZoneRule("America/Sao_Paulo");
    
    @Before
    public void before() throws IOException {
        dataPath = createTempDirectory(getClass().getSimpleName());
        
        DataBaseDirectory dataBaseDirectory = mock(DataBaseDirectory.class);
        when(dataBaseDirectory.path(anyString())).thenReturn(dataPath);
        
        FollowupClusterProvider clusterProvider = mock(FollowupClusterProvider.class);
        
        dailySynthesisRepo = new FollowupDailySynthesisJpaRepositoryImplementation();
        FollowupDailySynthesisRepository synthesisRepo = new FollowupDailySynthesisRepository(dailySynthesisRepo);
        
        ProjectFilterConfigurationCachedRepository projectRepo = mock(ProjectFilterConfigurationCachedRepository.class);
        
        ProjectFilterConfiguration projectTest1 = mock(ProjectFilterConfiguration.class);
        when(projectTest1.getProjectKey()).thenReturn(PROJECT_TEST);
        when(projectTest1.getId()).thenReturn(PROJECT_TEST.hashCode());
        when(projectRepo.getProjectByKey(PROJECT_TEST)).thenReturn(Optional.of(projectTest1));
        
        ProjectFilterConfiguration projectTest2 = mock(ProjectFilterConfiguration.class);
        when(projectTest2.getProjectKey()).thenReturn(PROJECT_TEST_2);
        when(projectTest2.getId()).thenReturn(PROJECT_TEST_2.hashCode());
        when(projectRepo.getProjectByKey(PROJECT_TEST_2)).thenReturn(Optional.of(projectTest2));
        
        subject = new FollowUpDataHistoryRepository(dataBaseDirectory, clusterProvider, synthesisRepo, projectRepo);
        
        when(clusterProvider.getForProject(PROJECT_TEST)).thenReturn(new FollowupClusterImpl(Arrays.asList(
                new FollowUpClusterItem(projectTest1, "Sub-Task", "notused", "XXS", 1.9, 2.8),
                new FollowUpClusterItem(projectTest1, "Sub-Task", "notused", "XS", 2.0, 2.8),
                new FollowUpClusterItem(projectTest1, "Sub-Task", "notused", "S",  4.0, 2.8),
                new FollowUpClusterItem(projectTest1, "Sub-Task", "notused", "M",  6.0, 2.8),
                new FollowUpClusterItem(projectTest1, "Sub-Task", "notused", "L",  10.2, 2.8),
                new FollowUpClusterItem(projectTest1, "Sub-Task", "notused", "XL", 13.0, 2.8)
                )));
        
        when(clusterProvider.getForProject(PROJECT_TEST_2)).thenReturn(new FollowupClusterImpl(Arrays.asList(
                new FollowUpClusterItem(projectTest2, "Sub-Task", "notused", "XXS", 1.9, 2.8),
                new FollowUpClusterItem(projectTest2, "Sub-Task", "notused", "XS", 2.0, 2.8),
                new FollowUpClusterItem(projectTest2, "Sub-Task", "notused", "S",  4.0, 2.8),
                new FollowUpClusterItem(projectTest2, "Sub-Task", "notused", "M",  6.0, 2.8),
                new FollowUpClusterItem(projectTest2, "Sub-Task", "notused", "L",  10.2, 2.8),
                new FollowUpClusterItem(projectTest2, "Sub-Task", "notused", "XL", 13.0, 2.8)
                )));        
    }
    
    @After
    public void after() {
        deleteQuietly(dataPath.toFile());
    }

    @Test
    public void whenSaveOneProject_thenOneFileShouldBeGenerated() throws IOException, InterruptedException {
        subject.saveSnapshotInFile(PROJECT_TEST, TODAY_DATE, getDefaultFollowupData());

        assertGeneratedFile(PROJECT_TEST, followupExpectedV2());
    }

    @Test
    public void whenSaveEmptyData_thenNoDataShouldBeGenerated() throws IOException, InterruptedException {
        subject.saveSnapshotInFile(PROJECT_TEST, TODAY_DATE, getEmptyFollowupData());

        assertGeneratedFile(PROJECT_TEST, followupEmptyV2());
    }

    @Test
    public void whenSaveTwoProjects_thenTwoFilesShouldBeGenerated() throws IOException, InterruptedException {
        subject.saveSnapshotInFile(PROJECT_TEST, TODAY_DATE, getDefaultFollowupData());
        subject.saveSnapshotInFile(PROJECT_TEST_2, TODAY_DATE, getDefaultFollowupData());

        assertGeneratedFile(PROJECT_TEST, followupExpectedV2());
        assertGeneratedFile(PROJECT_TEST_2, followupExpectedV2());
    }

    @Test
    public void givenProjectWithNoHistory_whenGetHistoryGivenProjects_thenReturnNoData() {
        List<String> history = subject.getHistoryGivenProjects(PROJECT_TEST);
        assertTrue("History should be empty", history.isEmpty());
    }

    @Test
    public void givenProjectWithTodaysHistory_whenGetHistoryGivenProjects_thenReturnNoData() {
        createProjectZip(PROJECT_TEST, TODAY);

        List<String> history = subject.getHistoryGivenProjects(PROJECT_TEST);
        assertTrue("History should be empty", history.isEmpty());
    }

    @Test
    public void givenProjectWithYesterdaysHistory_whenGetHistoryGivenProjects_thenReturnData() throws IOException {
        createProjectZip(PROJECT_TEST, YESTERDAY);

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
        createProjectZip(PROJECT_TEST, YESTERDAY);
        createProjectZip(PROJECT_TEST, BEFORE_YESTERDAY);
        createProjectZip(PROJECT_TEST_2, BEFORE_YESTERDAY);

        List<String> history = subject.getHistoryGivenProjects(PROJECT_TEST, PROJECT_TEST_2);
        assertEquals("History size", 1, history.size());
        assertEquals("First history", BEFORE_YESTERDAY, history.get(0));
    }
    
    @Test
    public void givenProjectsWithDistinctHistory_whenGetHistoryGivenProjects_thenReturnNoData() throws IOException {
        createProjectZip(PROJECT_TEST, YESTERDAY);
        createProjectZip(PROJECT_TEST_2, BEFORE_YESTERDAY);

        List<String> history = subject.getHistoryGivenProjects(PROJECT_TEST, PROJECT_TEST_2);
        assertTrue("History should be empty", history.isEmpty());
    }


    @Test
    public void whenHasDataHistory_GetShouldReturnSomeData() throws IOException, URISyntaxException {
        createProjectZipV0(PROJECT_TEST);

        List<FromJiraDataRow> jiraData = subject.get(YESTERDAY, TIMEZONE, PROJECT_TEST).getData().fromJiraDs.rows;

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
            Path zipFile = dataPath.resolve(PROJECT_TEST).resolve(YESTERDAY + EXTENSION_JSON + EXTENSION_ZIP);
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

        FollowupData followupData = subject.get(YESTERDAY, TIMEZONE, PROJECT_TEST, PROJECT_TEST_2).getData();
        List<FromJiraDataRow> jiraData = followupData.fromJiraDs.rows;

        assertEquals("Jira data size", jiraData.size(), 2);
        assertFollowUpDataV0(jiraData.get(0));
        assertFollowUpDataV0(jiraData.get(1));
    }

    @Test
    public void whenHasDataHistoryWithCfd_GetShouldRestoreSyntheticsDataSources() throws IOException, URISyntaxException {
        // given
        createProjectZipV2(PROJECT_TEST);

        // when
        FollowupData data = subject.get(YESTERDAY, TIMEZONE, PROJECT_TEST).getData();

        // then
        assertThat(data).isEqualToComparingFieldByFieldRecursively(getDefaultFollowupData());
    }

    @Test
    public void testVersion2() throws IOException, URISyntaxException {
        // given
        createProjectZipV2(PROJECT_TEST);

        // when
        FollowupData data = subject.get(YESTERDAY, TIMEZONE, PROJECT_TEST).getData();

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
        FollowupData dataSaoPaulo = subject.get(YESTERDAY, saoPauloTZ, PROJECT_TEST).getData();
        FollowupData dataToronto = subject.get(YESTERDAY, torontoTZ, PROJECT_TEST).getData();
        FollowupData dataSydney = subject.get(YESTERDAY, sydneyTZ, PROJECT_TEST).getData();

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
    
    @Test
    public void forEachHistoryEntry_happyDay() {
        dailySynthesisRepo.addEntry(PROJECT_TEST,   "20161201", 1.0);
        dailySynthesisRepo.addEntry(PROJECT_TEST_2, "20161201", 1.0);
        dailySynthesisRepo.addEntry(PROJECT_TEST,   "20170125", 1.0);
        dailySynthesisRepo.addEntry(PROJECT_TEST,   "20170126", 1.0);
        dailySynthesisRepo.addEntry(PROJECT_TEST,   "20170127", 1.0);
        dailySynthesisRepo.addEntry(PROJECT_TEST,   "20170128", 1.0);

        List<EffortHistoryRow> rows = subject.getHistoryRows(asList(PROJECT_TEST), Optional.of("20170127"), null);
        
        assertEquals(3, rows.size());
        String dates = rows.stream().map(p->p.date.toString()).reduce((d1,d2)->d1+","+d2).get();
        assertEquals("2016-12-01,2017-01-25,2017-01-26", dates);
        
        Double sumOfBacklogs = rows.stream().map(p->p.sumEffortBacklog).reduce((d1,d2)->d1+d2).get();
        assertEquals(3.0,  sumOfBacklogs, .01);
    }
    
    @Test
    public void syncEffortHistory_VerifyThatDataIsPersisted() {
        createProjectZip(PROJECT_TEST,   "20161201");
        createProjectZip(PROJECT_TEST_2, "20161201");
        createProjectZip(PROJECT_TEST,   "20170125");
        createProjectZip(PROJECT_TEST,   "20170126");
        createProjectZip(PROJECT_TEST,   "20170127");
        createProjectZip(PROJECT_TEST,   "20170128");
        
        subject.syncEffortHistory(PROJECT_TEST);

        Set<String> keySet = dailySynthesisRepo.entriesByProjectHashCode.keySet();
        
        assertEquals(PROJECT_TEST.hashCode()+"", StringUtils.join(keySet,","));
        List<FollowupDailySynthesis> list = dailySynthesisRepo.entriesByProjectHashCode.get(PROJECT_TEST.hashCode()+"");
        
        assertEquals("20161201,20170125,20170126,20170127,20170128", 
                list.stream().map(p->p.getFollowupDate()).sorted().map(a->FILE_NAME_FORMATTER.format(a)).reduce((a,b)->a+","+b).get());
        
    }
    
    @Test
    public void forEachHistoryEntry_noData() {
        List<EffortHistoryRow> historyRows = subject.getHistoryRows(asList(PROJECT_TEST), Optional.of("20170127"), null);
        assertEquals(0, historyRows.size());
    }

    @Test
    public void forEachHistoryEntry_multiProject_datesMustBeInTheIntersection() {
        dailySynthesisRepo.addEntry(PROJECT_TEST_2, "20161115");
        dailySynthesisRepo.addEntry(PROJECT_TEST,   "20161201");
        dailySynthesisRepo.addEntry(PROJECT_TEST_2, "20161201");
        dailySynthesisRepo.addEntry(PROJECT_TEST,   "20170125");
        dailySynthesisRepo.addEntry(PROJECT_TEST,   "20170126");
        dailySynthesisRepo.addEntry(PROJECT_TEST_2, "20170126");
        dailySynthesisRepo.addEntry(PROJECT_TEST,   "20170127");

        List<EffortHistoryRow> rows = subject.getHistoryRows(asList(PROJECT_TEST, PROJECT_TEST_2), Optional.of("20170127"), null);

        String dts = rows.stream().map(p->p.date.toString()).distinct().reduce((d1,d2)->d1+","+d2).get();
        assertEquals("2016-12-01,2017-01-26",dts);
    }
    
    @Test
    public void forEachHistoryEntry_noEndDate() {
        dailySynthesisRepo.addEntry(PROJECT_TEST, BEFORE_YESTERDAY);
        dailySynthesisRepo.addEntry(PROJECT_TEST, YESTERDAY);
        dailySynthesisRepo.addEntry(PROJECT_TEST, TODAY);

        FollowUpDataSnapshot last = mock(FollowUpDataSnapshot.class);
        when(last.getEffortHistoryRow()).thenReturn(new EffortHistoryRow(LocalDate.parse(TODAY, FILE_NAME_FORMATTER), 10.0, 30.0));
        List<EffortHistoryRow> rows = subject.getHistoryRows(asList(PROJECT_TEST), Optional.empty(), last);

        String dates = rows.stream().map(p->p.date.toString()).reduce((d1,d2)->d1+","+d2).get();
        assertEquals(BEFORE_YESTERDAY + "," + YESTERDAY + "," + TODAY, dates.replaceAll("-", ""));
        EffortHistoryRow historyRow = rows.get(2);
        assertEquals(30.0, historyRow.sumEffortBacklog, 0.01);
    }
    
    @Test
    public void forEachHistoryEntry_endDate() {
        dailySynthesisRepo.addEntry(PROJECT_TEST, BEFORE_YESTERDAY);
        dailySynthesisRepo.addEntry(PROJECT_TEST, YESTERDAY);
        dailySynthesisRepo.addEntry(PROJECT_TEST, TODAY);

        FollowUpDataSnapshot last = mock(FollowUpDataSnapshot.class);
        when(last.getEffortHistoryRow()).thenReturn(new EffortHistoryRow(LocalDate.parse(TODAY, FILE_NAME_FORMATTER), 10.0, 30.0));
        List<EffortHistoryRow> rows = subject.getHistoryRows(asList(PROJECT_TEST), Optional.of(TODAY), last);

        String dates = rows.stream().map(p->p.date.toString()).reduce((d1,d2)->d1+","+d2).get();
        assertEquals(BEFORE_YESTERDAY + "," + YESTERDAY, dates.replaceAll("-", ""));
    }
    
    private void assertGeneratedFile(String project, String dataHistoryExpected) throws IOException {
        Path source = dataPath.resolve(project).resolve(TODAY + EXTENSION_JSON + EXTENSION_ZIP);

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
        return createProjectZip(project, YESTERDAY, "V0_followUpDataHistoryExpected.json");
    }

    private Path createProjectZipV1(String project) throws IOException, URISyntaxException {
        return createProjectZip(project, YESTERDAY, "V1_followUpDataHistoryExpected.json");
    }

    private Path createProjectZipV2(String project) throws IOException, URISyntaxException {
        return createProjectZip(project, YESTERDAY, "V2_followUpDataHistoryExpected.json");
    }
    
    private Path createProjectZip(String project, String date) {
        return createProjectZip(project, date, "V2_followUpDataHistoryExpected.json");
    }
    
    private Path createProjectZip(String projectKey, String date, String file) {
        try {
            Path pathProject = dataPath.resolve(projectKey);
            createDirectories(pathProject);

            Path pathInputJSON = Paths.get(getClass().getResource("impl/" + file).toURI());
            Path pathOutputJSON = pathProject.resolve(date + EXTENSION_JSON);
            copy(pathInputJSON, pathOutputJSON);

            Path zipFile = Paths.get(pathOutputJSON.toString() + EXTENSION_ZIP);
            zip(pathOutputJSON, zipFile);
            return pathProject;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private final class FollowupDailySynthesisJpaRepositoryImplementation extends JpaRepositoryMock<FollowupDailySynthesis> implements FollowupDailySynthesisJpaRepository {
        public Map<String, List<FollowupDailySynthesis>> entriesByProjectHashCode = new LinkedHashMap<>();

        public void addEntry(String projectKey, String date) {
            addEntry(projectKey, date, 1.0);
        }
        
        public void addEntry(String projectKey, String date, double effortBacklogForGivenDate) {
            List<FollowupDailySynthesis> l = entriesByProjectHashCode.get(""+projectKey.hashCode());
            if (l == null) {
                l = new LinkedList<>();
                entriesByProjectHashCode.put(""+projectKey.hashCode(), l);
            }
            
            l.add(new FollowupDailySynthesis(projectKey.hashCode(), LocalDate.parse(date, FILE_NAME_FORMATTER), 0.0, effortBacklogForGivenDate));
        }
        
        @Override
        public <S extends FollowupDailySynthesis> S save(S entity) {
            List<FollowupDailySynthesis> l = entriesByProjectHashCode.get(""+entity.getProjectId());
            if (l == null) {
                l = new LinkedList<>();
                entriesByProjectHashCode.put(""+entity.getProjectId(), l);
            }
            l.add(entity);
            return entity;
        }

        @Override
        public Optional<FollowupDailySynthesis> findByFollowupDateAndProjectId(LocalDate date, Integer projectId) {
            List<FollowupDailySynthesis> daily = this.findByProjectId(projectId);
            return daily.stream().filter(each -> each.getFollowupDate().equals(date)).findFirst();
        }
        
        @Override
        public List<FollowupDailySynthesis> findByProjectId(Integer projectId) {
            List<FollowupDailySynthesis> list = entriesByProjectHashCode.get(projectId+"");
            if (list == null)
                return new LinkedList<>();
            return list;
        }
    }
}
