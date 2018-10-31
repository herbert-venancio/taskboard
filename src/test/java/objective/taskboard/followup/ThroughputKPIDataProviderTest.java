package objective.taskboard.followup;

import static java.time.LocalDate.parse;
import static objective.taskboard.followup.FixedFollowUpSnapshotValuesProvider.emptyValuesProvider;
import static objective.taskboard.utils.DateTimeUtils.parseDateTime;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.cluster.EmptyFollowupCluster;
import objective.taskboard.followup.kpi.ThroughputChartDataSet;
import objective.taskboard.followup.kpi.ThroughputDataPoint;
import objective.taskboard.followup.kpi.ThroughputKPIService;

@RunWith(MockitoJUnitRunner.class)
public class ThroughputKPIDataProviderTest {

    private static final String LEVEL_ALL = "All";
    private static final String LEVEL_DEMANDS = "Demand";
    private static final String LEVEL_FEATURES = "Feature";
    private static final String LEVEL_SUBTASKS = "Subtask";
    private static final ZoneId ZONE_ID = ZoneId.systemDefault();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private FollowUpDataRepository dataRepository;

    @Mock
    private FollowUpSnapshotService snapshotService;

    @Mock
    private ThroughputKPIService throughputKpiService;
    
    @Spy
    private WeekRangeNormalizer weekRangeNormalizer = new WeekRangeNormalizer();

    @InjectMocks
    private ThroughputKPIDataProvider subject = new ThroughputKPIDataProvider();

    @Test
    public void getThroughputChartDataSet_whenTimelineRangeEqualsDataSetRange_thenNotFiltering() {
        String projectKey = "TASKB";
        String issueLevel = LEVEL_ALL;
        
        new TestEnvConfigurator(projectKey)
            .withTimelineReferenceDate("2018-09-25")
            .withTimelineStartDate("2018-09-25")
            .withTimelineDeliveryDate("2018-10-05")
            .withTimelineBaselineDate("2018-09-25")
            .withDemands(
                    row("Demand").at("2018-09-25").withThroughput(1),
                    row("Demand").at("2018-09-26").withThroughput(0),
                    row("Demand").at("2018-09-27").withThroughput(0),
                    row("Demand").at("2018-09-28").withThroughput(2),
                    row("Demand").at("2018-09-29").withThroughput(0),
                    row("Demand").at("2018-09-30").withThroughput(0),
                    row("Demand").at("2018-10-01").withThroughput(0),
                    row("Demand").at("2018-10-02").withThroughput(0),
                    row("Demand").at("2018-10-03").withThroughput(0),
                    row("Demand").at("2018-10-04").withThroughput(0),
                    row("Demand").at("2018-10-05").withThroughput(3))
            .withFeatures(
                    row("Bug").at("2018-09-25").withThroughput(0),
                    row("Task").at("2018-09-25").withThroughput(2),
                    row("Bug").at("2018-09-26").withThroughput(0),
                    row("Task").at("2018-09-26").withThroughput(4),
                    row("Bug").at("2018-09-27").withThroughput(0),
                    row("Task").at("2018-09-27").withThroughput(1),
                    row("Bug").at("2018-09-28").withThroughput(0),
                    row("Task").at("2018-09-28").withThroughput(2),
                    row("Bug").at("2018-09-29").withThroughput(0),
                    row("Task").at("2018-09-29").withThroughput(0),
                    row("Bug").at("2018-09-30").withThroughput(0),
                    row("Task").at("2018-09-30").withThroughput(0),
                    row("Bug").at("2018-10-01").withThroughput(1),
                    row("Task").at("2018-10-01").withThroughput(1),
                    row("Bug").at("2018-10-02").withThroughput(0),
                    row("Task").at("2018-10-02").withThroughput(0),
                    row("Bug").at("2018-10-03").withThroughput(0),
                    row("Task").at("2018-10-03").withThroughput(2),
                    row("Bug").at("2018-10-04").withThroughput(1),
                    row("Task").at("2018-10-04").withThroughput(0),
                    row("Bug").at("2018-10-05").withThroughput(0),
                    row("Task").at("2018-10-05").withThroughput(2))
            .withSubtasks(
                    row("Alpha Test").at("2018-09-25").withThroughput(1),
                    row("Backend Development").at("2018-09-25").withThroughput(6),
                    row("Alpha Test").at("2018-09-26").withThroughput(4),
                    row("Backend Development").at("2018-09-26").withThroughput(8),
                    row("Alpha Test").at("2018-09-27").withThroughput(0),
                    row("Backend Development").at("2018-09-27").withThroughput(4),
                    row("Alpha Test").at("2018-09-28").withThroughput(1),
                    row("Backend Development").at("2018-09-28").withThroughput(4),
                    row("Alpha Test").at("2018-09-29").withThroughput(0),
                    row("Backend Development").at("2018-09-29").withThroughput(0),
                    row("Alpha Test").at("2018-09-30").withThroughput(0),
                    row("Backend Development").at("2018-09-30").withThroughput(0),
                    row("Alpha Test").at("2018-10-01").withThroughput(0),
                    row("Backend Development").at("2018-10-01").withThroughput(2),
                    row("Alpha Test").at("2018-10-02").withThroughput(2),
                    row("Backend Development").at("2018-10-02").withThroughput(3),
                    row("Alpha Test").at("2018-10-03").withThroughput(0),
                    row("Backend Development").at("2018-10-03").withThroughput(4),
                    row("Alpha Test").at("2018-10-04").withThroughput(1),
                    row("Backend Development").at("2018-10-04").withThroughput(5),
                    row("Alpha Test").at("2018-10-05").withThroughput(0),
                    row("Backend Development").at("2018-10-05").withThroughput(4))
            .configure();

        ThroughputChartDataSet ds = subject.getDataSet(projectKey, issueLevel, ZONE_ID);

        assertThat(ds.rows.size(), is(10));
        assertRow(ds.rows.get(0), "Demand", 3L, "2018-09-23");
        assertRow(ds.rows.get(1), "Demand", 3L, "2018-09-30");
        assertRow(ds.rows.get(2), "Bug", 0L, "2018-09-23");
        assertRow(ds.rows.get(3), "Task", 9L, "2018-09-23");
        assertRow(ds.rows.get(4), "Bug", 2L, "2018-09-30");
        assertRow(ds.rows.get(5), "Task", 5L, "2018-09-30");
        assertRow(ds.rows.get(6), "Alpha Test", 6L, "2018-09-23");
        assertRow(ds.rows.get(7), "Backend Development", 22L, "2018-09-23");
        assertRow(ds.rows.get(8), "Alpha Test", 3L, "2018-09-30");
        assertRow(ds.rows.get(9), "Backend Development", 18L, "2018-09-30");
    }

    @Test
    public void getThroughputChartDataSet_whenTimelineRangeBeforeDataSetDateRange_thenReturnsEmptyDataSet() {
        String projectKey = "TASKB";
        String issueLevel = LEVEL_SUBTASKS;
        
        new TestEnvConfigurator(projectKey)
            .withTimelineReferenceDate("2018-09-25")
            .withTimelineStartDate("2018-09-17")
            .withTimelineDeliveryDate("2018-09-21")
            .withTimelineBaselineDate("2018-09-17")
            .withSubtasks(
                    row("Alpha Test").at("2018-09-25").withThroughput(1),
                    row("Backend Development").at("2018-09-25").withThroughput(6),
                    row("Alpha Test").at("2018-09-26").withThroughput(4),
                    row("Backend Development").at("2018-09-26").withThroughput(8),
                    row("Alpha Test").at("2018-09-27").withThroughput(0),
                    row("Backend Development").at("2018-09-27").withThroughput(4),
                    row("Alpha Test").at("2018-09-28").withThroughput(1),
                    row("Backend Development").at("2018-09-28").withThroughput(4),
                    row("Alpha Test").at("2018-09-29").withThroughput(0),
                    row("Backend Development").at("2018-09-29").withThroughput(0),
                    row("Alpha Test").at("2018-09-30").withThroughput(0),
                    row("Backend Development").at("2018-09-30").withThroughput(0),
                    row("Alpha Test").at("2018-10-01").withThroughput(0),
                    row("Backend Development").at("2018-10-01").withThroughput(2),
                    row("Alpha Test").at("2018-10-02").withThroughput(2),
                    row("Backend Development").at("2018-10-02").withThroughput(3),
                    row("Alpha Test").at("2018-10-03").withThroughput(0),
                    row("Backend Development").at("2018-10-03").withThroughput(4),
                    row("Alpha Test").at("2018-10-04").withThroughput(1),
                    row("Backend Development").at("2018-10-04").withThroughput(5),
                    row("Alpha Test").at("2018-10-05").withThroughput(0),
                    row("Backend Development").at("2018-10-05").withThroughput(4))
            .configure();
        
        ThroughputChartDataSet ds = subject.getDataSet(projectKey, issueLevel, ZONE_ID);

        assertThat(ds.rows.size(), is(0));
    }

    @Test
    public void getThroughputChartDataSet_whenTimelineRangeAfterDataSetDateRange_thenReturnsEmptyDateSet() {
        String projectKey = "TASKB";
        String issueLevel = LEVEL_SUBTASKS;
        
        new TestEnvConfigurator(projectKey)
            .withTimelineReferenceDate("2018-09-25")
            .withTimelineStartDate("2018-10-08")
            .withTimelineDeliveryDate("2018-10-12")
            .withTimelineBaselineDate("2018-10-08")
            .withSubtasks(
                    row("Alpha Test").at("2018-09-25").withThroughput(1),
                    row("Backend Development").at("2018-09-25").withThroughput(6),
                    row("Alpha Test").at("2018-09-26").withThroughput(4),
                    row("Backend Development").at("2018-09-26").withThroughput(8),
                    row("Alpha Test").at("2018-09-27").withThroughput(0),
                    row("Backend Development").at("2018-09-27").withThroughput(4),
                    row("Alpha Test").at("2018-09-28").withThroughput(1),
                    row("Backend Development").at("2018-09-28").withThroughput(4),
                    row("Alpha Test").at("2018-09-29").withThroughput(0),
                    row("Backend Development").at("2018-09-29").withThroughput(0),
                    row("Alpha Test").at("2018-09-30").withThroughput(0),
                    row("Backend Development").at("2018-09-30").withThroughput(0),
                    row("Alpha Test").at("2018-10-01").withThroughput(0),
                    row("Backend Development").at("2018-10-01").withThroughput(2),
                    row("Alpha Test").at("2018-10-02").withThroughput(2),
                    row("Backend Development").at("2018-10-02").withThroughput(3),
                    row("Alpha Test").at("2018-10-03").withThroughput(0),
                    row("Backend Development").at("2018-10-03").withThroughput(4),
                    row("Alpha Test").at("2018-10-04").withThroughput(1),
                    row("Backend Development").at("2018-10-04").withThroughput(5),
                    row("Alpha Test").at("2018-10-05").withThroughput(0),
                    row("Backend Development").at("2018-10-05").withThroughput(4))
            .configure();

        ThroughputChartDataSet ds = subject.getDataSet(projectKey, issueLevel, ZONE_ID);

        assertThat(ds.rows.size(), is(0));
    }
    
    @Test
    public void getThroughputChartDataSet_whenTimelineRangeStartsBeforeAndEndsWithinDataSetDateRange_thenFiltersEndOnly() {
        String projectKey = "TASKB";
        String issueLevel = LEVEL_SUBTASKS;
        
        new TestEnvConfigurator(projectKey)
            .withTimelineReferenceDate("2018-09-25")
            .withTimelineStartDate("2018-09-20")
            .withTimelineDeliveryDate("2018-09-29")
            .withTimelineBaselineDate("2018-09-20")
            .withSubtasks(
                    row("Alpha Test").at("2018-09-25").withThroughput(1),
                    row("Backend Development").at("2018-09-25").withThroughput(6),
                    row("Alpha Test").at("2018-09-26").withThroughput(4),
                    row("Backend Development").at("2018-09-26").withThroughput(8),
                    row("Alpha Test").at("2018-09-27").withThroughput(0),
                    row("Backend Development").at("2018-09-27").withThroughput(4),
                    row("Alpha Test").at("2018-09-28").withThroughput(1),
                    row("Backend Development").at("2018-09-28").withThroughput(4),
                    row("Alpha Test").at("2018-09-29").withThroughput(0),
                    row("Backend Development").at("2018-09-29").withThroughput(0),
                    row("Alpha Test").at("2018-09-30").withThroughput(0),
                    row("Backend Development").at("2018-09-30").withThroughput(0),
                    row("Alpha Test").at("2018-10-01").withThroughput(0),
                    row("Backend Development").at("2018-10-01").withThroughput(2),
                    row("Alpha Test").at("2018-10-02").withThroughput(2),
                    row("Backend Development").at("2018-10-02").withThroughput(3),
                    row("Alpha Test").at("2018-10-03").withThroughput(0),
                    row("Backend Development").at("2018-10-03").withThroughput(4),
                    row("Alpha Test").at("2018-10-04").withThroughput(1),
                    row("Backend Development").at("2018-10-04").withThroughput(5),
                    row("Alpha Test").at("2018-10-05").withThroughput(0),
                    row("Backend Development").at("2018-10-05").withThroughput(4))
            .configure();

        ThroughputChartDataSet ds = subject.getDataSet(projectKey, issueLevel, ZONE_ID);

        assertThat(ds.rows.size(), is(2));
        assertRow(ds.rows.get(0), "Alpha Test", 6L, "2018-09-23");
        assertRow(ds.rows.get(1), "Backend Development", 22L, "2018-09-23");
    }
    
    @Test
    public void getThroughputChartDataSet_whenTimelineRangeStartsWithinAndEndsAfterDataSetDateRange_thenFiltersBeginOnly() {
        String projectKey = "TASKB";
        String issueLevel = LEVEL_SUBTASKS;
        
        new TestEnvConfigurator(projectKey)
            .withTimelineReferenceDate("2018-09-25")
            .withTimelineStartDate("2018-09-30")
            .withTimelineDeliveryDate("2018-10-08")
            .withTimelineBaselineDate("2018-09-25")
            .withSubtasks(
                    row("Alpha Test").at("2018-09-25").withThroughput(1),
                    row("Backend Development").at("2018-09-25").withThroughput(6),
                    row("Alpha Test").at("2018-09-26").withThroughput(4),
                    row("Backend Development").at("2018-09-26").withThroughput(8),
                    row("Alpha Test").at("2018-09-27").withThroughput(0),
                    row("Backend Development").at("2018-09-27").withThroughput(4),
                    row("Alpha Test").at("2018-09-28").withThroughput(1),
                    row("Backend Development").at("2018-09-28").withThroughput(4),
                    row("Alpha Test").at("2018-09-29").withThroughput(0),
                    row("Backend Development").at("2018-09-29").withThroughput(0),
                    row("Alpha Test").at("2018-09-30").withThroughput(0),
                    row("Backend Development").at("2018-09-30").withThroughput(0),
                    row("Alpha Test").at("2018-10-01").withThroughput(0),
                    row("Backend Development").at("2018-10-01").withThroughput(2),
                    row("Alpha Test").at("2018-10-02").withThroughput(2),
                    row("Backend Development").at("2018-10-02").withThroughput(3),
                    row("Alpha Test").at("2018-10-03").withThroughput(0),
                    row("Backend Development").at("2018-10-03").withThroughput(4),
                    row("Alpha Test").at("2018-10-04").withThroughput(1),
                    row("Backend Development").at("2018-10-04").withThroughput(5),
                    row("Alpha Test").at("2018-10-05").withThroughput(0),
                    row("Backend Development").at("2018-10-05").withThroughput(4))
            .configure();

        ThroughputChartDataSet ds = subject.getDataSet(projectKey, issueLevel, ZONE_ID);

        assertThat(ds.rows.size(), is(2));
        assertRow(ds.rows.get(0), "Alpha Test", 3L, "2018-09-30");
        assertRow(ds.rows.get(1), "Backend Development", 18L, "2018-09-30");
    }
    
    @Test
    public void getThroughputChartDataSet_whenTimelineRangeContainsDataSetDateRange_thenReturnsWholeDataSet() {
        String projectKey = "TASKB";
        String issueLevel = LEVEL_SUBTASKS;
        
        new TestEnvConfigurator(projectKey)
            .withTimelineReferenceDate("2018-09-25")
            .withTimelineStartDate("2018-09-23")
            .withTimelineDeliveryDate("2018-10-06")
            .withTimelineBaselineDate("2018-09-25")
            .withSubtasks(
                    row("Alpha Test").at("2018-09-25").withThroughput(1),
                    row("Backend Development").at("2018-09-25").withThroughput(6),
                    row("Alpha Test").at("2018-09-26").withThroughput(4),
                    row("Backend Development").at("2018-09-26").withThroughput(8),
                    row("Alpha Test").at("2018-09-27").withThroughput(0),
                    row("Backend Development").at("2018-09-27").withThroughput(4),
                    row("Alpha Test").at("2018-09-28").withThroughput(1),
                    row("Backend Development").at("2018-09-28").withThroughput(4),
                    row("Alpha Test").at("2018-09-29").withThroughput(0),
                    row("Backend Development").at("2018-09-29").withThroughput(0),
                    row("Alpha Test").at("2018-09-30").withThroughput(0),
                    row("Backend Development").at("2018-09-30").withThroughput(0),
                    row("Alpha Test").at("2018-10-01").withThroughput(0),
                    row("Backend Development").at("2018-10-01").withThroughput(2),
                    row("Alpha Test").at("2018-10-02").withThroughput(2),
                    row("Backend Development").at("2018-10-02").withThroughput(3),
                    row("Alpha Test").at("2018-10-03").withThroughput(0),
                    row("Backend Development").at("2018-10-03").withThroughput(4),
                    row("Alpha Test").at("2018-10-04").withThroughput(1),
                    row("Backend Development").at("2018-10-04").withThroughput(5),
                    row("Alpha Test").at("2018-10-05").withThroughput(0),
                    row("Backend Development").at("2018-10-05").withThroughput(4))
            .configure();

        ThroughputChartDataSet ds = subject.getDataSet(projectKey, issueLevel, ZONE_ID);

        assertThat(ds.rows.size(), is(4));
        assertRow(ds.rows.get(0), "Alpha Test", 6L, "2018-09-23");
        assertRow(ds.rows.get(1), "Backend Development", 22L, "2018-09-23");
        assertRow(ds.rows.get(2), "Alpha Test", 3L, "2018-09-30");
        assertRow(ds.rows.get(3), "Backend Development", 18L, "2018-09-30");
    }
    
    @Test
    public void getThroughputChartDataSet_whenTimelineRangeWithinDataSetRange_thenReturnsFilteredDataSet() {
        String projectKey = "TASKB";
        String issueLevel = LEVEL_SUBTASKS;
        
        new TestEnvConfigurator(projectKey)
            .withTimelineReferenceDate("2018-09-25")
            .withTimelineStartDate("2018-09-30")
            .withTimelineDeliveryDate("2018-10-06")
            .withTimelineBaselineDate("2018-09-26")
            .withSubtasks(
                    row("Alpha Test").at("2018-09-23").withThroughput(0),
                    row("Backend Development").at("2018-09-23").withThroughput(0),
                    row("Alpha Test").at("2018-09-24").withThroughput(0),
                    row("Backend Development").at("2018-09-24").withThroughput(0),
                    row("Alpha Test").at("2018-09-25").withThroughput(1),
                    row("Backend Development").at("2018-09-25").withThroughput(6),
                    row("Alpha Test").at("2018-09-26").withThroughput(4),
                    row("Backend Development").at("2018-09-26").withThroughput(8),
                    row("Alpha Test").at("2018-09-27").withThroughput(0),
                    row("Backend Development").at("2018-09-27").withThroughput(4),
                    row("Alpha Test").at("2018-09-28").withThroughput(1),
                    row("Backend Development").at("2018-09-28").withThroughput(4),
                    row("Alpha Test").at("2018-09-29").withThroughput(0),
                    row("Backend Development").at("2018-09-29").withThroughput(0),
                    row("Alpha Test").at("2018-09-30").withThroughput(0),
                    row("Backend Development").at("2018-09-30").withThroughput(0),
                    row("Alpha Test").at("2018-10-01").withThroughput(0),
                    row("Backend Development").at("2018-10-01").withThroughput(2),
                    row("Alpha Test").at("2018-10-02").withThroughput(2),
                    row("Backend Development").at("2018-10-02").withThroughput(3),
                    row("Alpha Test").at("2018-10-03").withThroughput(0),
                    row("Backend Development").at("2018-10-03").withThroughput(4),
                    row("Alpha Test").at("2018-10-04").withThroughput(1),
                    row("Backend Development").at("2018-10-04").withThroughput(5),
                    row("Alpha Test").at("2018-10-05").withThroughput(0),
                    row("Backend Development").at("2018-10-05").withThroughput(4))
            .configure();

        ThroughputChartDataSet ds = subject.getDataSet(projectKey, issueLevel, ZONE_ID);

        assertThat(ds.rows.size(), is(2));
        assertRow(ds.rows.get(0), "Alpha Test", 3L, "2018-09-30");
        assertRow(ds.rows.get(1), "Backend Development", 18L, "2018-09-30");
    }
    
    @Test
    public void getThroughputChartDataSet__whenTimelineDatesNotSet_thenNotFiltering() {
        String projectKey = "TASKB";
        String issueLevel = LEVEL_DEMANDS;

        new TestEnvConfigurator(projectKey)
            .withTimelineReferenceDate("2018-09-25")
            .withDemands(
                    row("Demand").at("2018-09-25").withThroughput(1),
                    row("Demand").at("2018-09-26").withThroughput(0),
                    row("Demand").at("2018-09-27").withThroughput(0),
                    row("Demand").at("2018-09-28").withThroughput(2),
                    row("Demand").at("2018-09-29").withThroughput(0),
                    row("Demand").at("2018-09-30").withThroughput(0),
                    row("Demand").at("2018-10-01").withThroughput(0),
                    row("Demand").at("2018-10-02").withThroughput(0),
                    row("Demand").at("2018-10-03").withThroughput(0),
                    row("Demand").at("2018-10-04").withThroughput(0),
                    row("Demand").at("2018-10-05").withThroughput(3))
            .configure();

        final ThroughputChartDataSet ds = subject.getDataSet(projectKey, issueLevel, ZONE_ID);
        
        assertThat(ds.rows.size(), is(2));
        assertRow(ds.rows.get(0), "Demand", 3L, "2018-09-23");
        assertRow(ds.rows.get(1), "Demand", 3L, "2018-09-30");
    }

    @Test
    public void getThroughputChartDataSet_whenDataSetWithoutSubtasksAndRequestingFeatures_thenReturnsSuccessfully() {
        String projectKey = "TASKB";
        String issueLevel = LEVEL_FEATURES;
        
        
        new TestEnvConfigurator(projectKey)
            .withTimelineReferenceDate("2018-09-25")
            .withTimelineStartDate("2018-09-25")
            .withTimelineDeliveryDate("2018-10-05")
            .withTimelineBaselineDate("2018-09-25")
            .withDemands(
                    row("Demand").at("2018-09-25").withThroughput(1),
                    row("Demand").at("2018-09-26").withThroughput(0),
                    row("Demand").at("2018-09-27").withThroughput(0),
                    row("Demand").at("2018-09-28").withThroughput(2),
                    row("Demand").at("2018-09-29").withThroughput(0),
                    row("Demand").at("2018-09-30").withThroughput(0),
                    row("Demand").at("2018-10-01").withThroughput(0),
                    row("Demand").at("2018-10-02").withThroughput(0),
                    row("Demand").at("2018-10-03").withThroughput(0),
                    row("Demand").at("2018-10-04").withThroughput(0),
                    row("Demand").at("2018-10-05").withThroughput(3))
            .withFeatures(
                    row("Bug").at("2018-09-25").withThroughput(0),
                    row("Task").at("2018-09-25").withThroughput(2),
                    row("Bug").at("2018-09-26").withThroughput(0),
                    row("Task").at("2018-09-26").withThroughput(4),
                    row("Bug").at("2018-09-27").withThroughput(0),
                    row("Task").at("2018-09-27").withThroughput(1),
                    row("Bug").at("2018-09-28").withThroughput(0),
                    row("Task").at("2018-09-28").withThroughput(2),
                    row("Bug").at("2018-09-29").withThroughput(0),
                    row("Task").at("2018-09-29").withThroughput(0),
                    row("Bug").at("2018-09-30").withThroughput(0),
                    row("Task").at("2018-09-30").withThroughput(0),
                    row("Bug").at("2018-10-01").withThroughput(1),
                    row("Task").at("2018-10-01").withThroughput(1),
                    row("Bug").at("2018-10-02").withThroughput(0),
                    row("Task").at("2018-10-02").withThroughput(0),
                    row("Bug").at("2018-10-03").withThroughput(0),
                    row("Task").at("2018-10-03").withThroughput(2),
                    row("Bug").at("2018-10-04").withThroughput(1),
                    row("Task").at("2018-10-04").withThroughput(0),
                    row("Bug").at("2018-10-05").withThroughput(0),
                    row("Task").at("2018-10-05").withThroughput(2))
            .configure();

        final ThroughputChartDataSet ds = subject.getDataSet(projectKey, issueLevel, ZONE_ID);
        assertThat(ds.rows.size(), is(4));
        assertRow(ds.rows.get(0), "Bug", 0L, "2018-09-23");
        assertRow(ds.rows.get(1), "Task", 9L, "2018-09-23");
        assertRow(ds.rows.get(2), "Bug", 2L, "2018-09-30");
        assertRow(ds.rows.get(3), "Task", 5L, "2018-09-30");
    }
    
    @Test
    public void getDataSet_whenRequestingFeaturesAndDataSetOnlyHasDemands_thenReturnsEmptyDataSet() {
        String projectKey = "TASKB";
        String issueLevel = LEVEL_FEATURES;
        
        
        new TestEnvConfigurator(projectKey)
            .withTimelineReferenceDate("2018-09-25")
            .withTimelineStartDate("2018-09-25")
            .withTimelineDeliveryDate("2018-09-28")
            .withTimelineBaselineDate("2018-09-25")
            .withDemands(
                    row("Demand").at("2018-09-25").withThroughput(1),
                    row("Demand").at("2018-09-26").withThroughput(0),
                    row("Demand").at("2018-09-27").withThroughput(0),
                    row("Demand").at("2018-09-28").withThroughput(2),
                    row("Demand").at("2018-09-29").withThroughput(0),
                    row("Demand").at("2018-09-30").withThroughput(0),
                    row("Demand").at("2018-10-01").withThroughput(0),
                    row("Demand").at("2018-10-02").withThroughput(0),
                    row("Demand").at("2018-10-03").withThroughput(0),
                    row("Demand").at("2018-10-04").withThroughput(0),
                    row("Demand").at("2018-10-05").withThroughput(3))
            .configure();

        final ThroughputChartDataSet ds = subject.getDataSet(projectKey, issueLevel, ZONE_ID);
        
        assertThat(ds.rows.size(), is(0));
    }
    
    @Test
    public void getThroughputChartDataSet_whenTimelineStartDateWithinAndEndDateNotSet_thenFiltersBeginOnly() {
        String projectKey = "TASKB";
        String issueLevel = LEVEL_FEATURES;
        
        
        new TestEnvConfigurator(projectKey)
            .withTimelineReferenceDate("2018-09-25")
            .withTimelineStartDate("2018-09-30")
            .withTimelineBaselineDate("2018-09-25")
            .withFeatures(
                    row("Bug").at("2018-09-25").withThroughput(0),
                    row("Task").at("2018-09-25").withThroughput(2),
                    row("Bug").at("2018-09-26").withThroughput(0),
                    row("Task").at("2018-09-26").withThroughput(4),
                    row("Bug").at("2018-09-27").withThroughput(0),
                    row("Task").at("2018-09-27").withThroughput(1),
                    row("Bug").at("2018-09-28").withThroughput(0),
                    row("Task").at("2018-09-28").withThroughput(2),
                    row("Bug").at("2018-09-29").withThroughput(0),
                    row("Task").at("2018-09-29").withThroughput(0),
                    row("Bug").at("2018-09-30").withThroughput(0),
                    row("Task").at("2018-09-30").withThroughput(0),
                    row("Bug").at("2018-10-01").withThroughput(1),
                    row("Task").at("2018-10-01").withThroughput(1),
                    row("Bug").at("2018-10-02").withThroughput(0),
                    row("Task").at("2018-10-02").withThroughput(0),
                    row("Bug").at("2018-10-03").withThroughput(0),
                    row("Task").at("2018-10-03").withThroughput(2),
                    row("Bug").at("2018-10-04").withThroughput(1),
                    row("Task").at("2018-10-04").withThroughput(0),
                    row("Bug").at("2018-10-05").withThroughput(0),
                    row("Task").at("2018-10-05").withThroughput(2))
            .configure();

        final ThroughputChartDataSet ds = subject.getDataSet(projectKey, issueLevel, ZONE_ID);
        
        assertThat(ds.rows.size(), is(2));
        assertRow(ds.rows.get(0), "Bug", 2L, "2018-09-30");
        assertRow(ds.rows.get(1), "Task", 5L, "2018-09-30");
    }

    @Test
    public void getThroughputChartDataSet_whenTimelineDatesNotSetDatasetEmpty_thenReturnsEmptyDataSet() {
        String projectKey = "TASKB";
        String issueLevel = LEVEL_ALL;

        new TestEnvConfigurator(projectKey)
            .withTimelineReferenceDate("2018-09-25")
            .configure();

        final ThroughputChartDataSet ds = subject.getDataSet(projectKey, issueLevel, ZONE_ID);
        
        assertThat(ds.rows.size(), is(0));
    }

    private void assertRow(ThroughputDataPoint actual, String expectedIssueType, Long expectedThroughputWeekCount, String date) {
        assertThat(actual.issueType, is(expectedIssueType));
        final ZonedDateTime expectedDate = parseDateTime(date);
        assertThat(DayOfWeek.SUNDAY, is(expectedDate.getDayOfWeek()));
        assertThat(actual.date, is(Date.from(expectedDate.toInstant())));
        assertThat(actual.count, is(expectedThroughputWeekCount));
    }
    
    private ThroughputRowBuilder row(String issueType) {
        ThroughputRowBuilder rowBuilder = new ThroughputRowBuilder(issueType);
        return rowBuilder;
    }
    
    private class TestEnvConfigurator {
        private String projectKey;
        private Map<String, ThroughputDataSetBuilder> dsBuilders;
        private Optional<LocalDate> startDate = Optional.empty();
        private Optional<LocalDate> deliveryDate = Optional.empty();
        private Optional<LocalDate> baselineDate = Optional.empty();
        private LocalDate referenceDate;
        private List<ThroughputDataSet> dataSets;
    
        private TestEnvConfigurator(String projectKey) {
            this.projectKey = projectKey;
            dsBuilders = new HashMap<>();
            dsBuilders.put(LEVEL_DEMANDS, new ThroughputDataSetBuilder(LEVEL_DEMANDS));
            dsBuilders.put(LEVEL_FEATURES, new ThroughputDataSetBuilder(LEVEL_FEATURES));
            dsBuilders.put(LEVEL_SUBTASKS, new ThroughputDataSetBuilder(LEVEL_SUBTASKS));
        }
    
        private TestEnvConfigurator withDemands(ThroughputRowBuilder ... rowBuilders) {
            ThroughputDataSetBuilder demandsBuilder = dsBuilders.get(LEVEL_DEMANDS);
            demandsBuilder.addRowBuilders(Arrays.asList(rowBuilders));
            return this;
        }
    
        private TestEnvConfigurator withFeatures(ThroughputRowBuilder ... rowBuilders) {
            ThroughputDataSetBuilder demandsBuilder = dsBuilders.get(LEVEL_FEATURES);
            demandsBuilder.addRowBuilders(Arrays.asList(rowBuilders));
            return this;
        }
    
        private TestEnvConfigurator withSubtasks(ThroughputRowBuilder ... rowBuilders) {
            ThroughputDataSetBuilder demandsBuilder = dsBuilders.get(LEVEL_SUBTASKS);
            demandsBuilder.addRowBuilders(Arrays.asList(rowBuilders));
            return this;
        }
        
        private TestEnvConfigurator withTimelineStartDate(String startDate) {
            this.startDate = Optional.of(parse(startDate));
            return this;
        }
        
        private TestEnvConfigurator withTimelineDeliveryDate(String deliveryDate) {
            this.deliveryDate = Optional.of(parse(deliveryDate));
            return this;
        }
        
        private TestEnvConfigurator withTimelineBaselineDate(String baselineDate) {
            this.baselineDate = Optional.of(parse(baselineDate));
            return this;
        }
        
        private TestEnvConfigurator withTimelineReferenceDate(String referenceDate) {
            this.referenceDate = parse(referenceDate);
            return this;
        }
    
        private void configure() {
            if (referenceDate == null) {
                throw new RuntimeException("Timeline reference Date must be set!");
            }
            dataSets = Arrays.asList(
                    dsBuilders.get(LEVEL_DEMANDS).build(), 
                    dsBuilders.get(LEVEL_FEATURES).build(), 
                    dsBuilders.get(LEVEL_SUBTASKS).build());
            when(dataRepository.getFirstDate(projectKey)).thenReturn(startDate);
            mockThroughputKpiService();
        }
        
        private void mockThroughputKpiService() {
            ProjectFilterConfiguration projectFilter = createProjectFilter();
            final FollowUpSnapshot snapshot = mockFollowUpSnapshot(projectFilter);
            when(throughputKpiService.getData(snapshot.getData())).thenReturn(dataSets);
        }
    
        private ProjectFilterConfiguration createProjectFilter() {
            ProjectFilterConfiguration project = new ProjectFilterConfiguration(projectKey, 1L);
            if (startDate.isPresent())
                project.setStartDate(startDate.get());
            if (deliveryDate.isPresent())
                project.setDeliveryDate(deliveryDate.get());
            if (baselineDate.isPresent())
                project.setBaselineDate(baselineDate.get());
        
            return project;
        }
    
        private FollowUpSnapshot mockFollowUpSnapshot(ProjectFilterConfiguration projectFilter) {
            final FollowUpData biggerFollowupData = FollowUpHelper.getBiggerFollowupData();
            FollowUpTimeline timeline = createCustomTimeline(projectFilter);
            FollowUpSnapshot snapshot = new FollowUpSnapshot(timeline, biggerFollowupData, new EmptyFollowupCluster(), emptyValuesProvider());
            when(snapshotService.getFromCurrentState(ZONE_ID, projectKey)).thenReturn(snapshot);
            return snapshot;
        }
    
        private FollowUpTimeline createCustomTimeline(ProjectFilterConfiguration projectFilter) {
            
            FollowUpTimeline timeline = FollowUpTimeline.build(referenceDate, projectFilter, dataRepository);
            return timeline;
        }
    }

    private class ThroughputDataSetBuilder {
    
        private String issueLevel;
        private List<ThroughputRowBuilder> rowBuilders = new LinkedList<ThroughputRowBuilder>();
    
        private ThroughputDataSetBuilder(String issueLevel) {
            this.issueLevel = issueLevel;
        }
        
        public void addRowBuilders(List<ThroughputRowBuilder> rowBuilders) {
            this.rowBuilders = rowBuilders;
        }
    
        private ThroughputDataSet build() {
            List<ThroughputRow> rows = rowBuilders.stream()
                    .map(b -> b.build()).collect(Collectors.toList());
            ThroughputDataSet dataset = new ThroughputDataSet(issueLevel, rows);
            return dataset;
        }
    }

    private class ThroughputRowBuilder {
        private ZonedDateTime date;
        private String issueType;
        private Long throughput;
        
        private ThroughputRowBuilder(String issueType) {
            this.issueType = issueType;
        }
        
        private ThroughputRowBuilder at(String date) { 
            this.date = parseDateTime(date);
            return this;
        }
        
        private ThroughputRowBuilder withThroughput(long throughput) {
            this.throughput = throughput;
            return this;
        }

        private ThroughputRow build() {
            if (date == null)
                throw new RuntimeException("Date required!");
            if (issueType == null) 
                throw new RuntimeException("Issue Type required!");
            if (throughput == null)
                throw new RuntimeException("Throughput required!");

            return new ThroughputRow(date, issueType, throughput);
        }
    }
}
