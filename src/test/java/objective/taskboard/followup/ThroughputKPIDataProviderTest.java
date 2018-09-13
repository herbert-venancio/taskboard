package objective.taskboard.followup;

import static java.time.LocalDate.parse;
import static objective.taskboard.followup.FixedFollowUpSnapshotValuesProvider.emptyValuesProvider;
import static objective.taskboard.utils.DateTimeUtils.parseDateTime;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

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
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.cluster.EmptyFollowupCluster;
import objective.taskboard.followup.kpi.ThroughputChartDataSet;
import objective.taskboard.followup.kpi.ThroughputDataPoint;
import objective.taskboard.followup.kpi.ThroughputKPIService;

@RunWith(MockitoJUnitRunner.class)
public class ThroughputKPIDataProviderTest {

    public static final String LEVEL_DEMANDS = "Demand";
    public static final String LEVEL_FEATURES = "Feature";
    public static final String LEVEL_SUBTASKS = "Subtask";
    private static final ZoneId ZONE_ID = ZoneId.systemDefault();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private FollowUpDataRepository dataRepository;

    @Mock
    private FollowUpSnapshotService snapshotService;

    @Mock
    private ThroughputKPIService throughputKpiService;

    @InjectMocks
    private ThroughputKPIDataProvider subject = new ThroughputKPIDataProvider();

    @Test
    public void getThroughputChartDataSet_happyPath() {
        String projectKey = "TASKB";
        String issueLevel = LEVEL_SUBTASKS;
        
        new TestEnvConfigurator(projectKey)
            .withTimelineReferenceDate("2017-09-25")
            .withTimelineStartDate("2017-09-25")
            .withTimelineDeliveryDate("2017-09-28")
            .withTimelineBaselineDate("2017-09-25")
            .withDemands(
                    row("Demand").at("2017-09-25").withThroughput(1),
                    row("Demand").at("2017-09-26").withThroughput(2),
                    row("Demand").at("2017-09-27").withThroughput(0),
                    row("Demand").at("2017-09-28").withThroughput(1))
            .withFeatures(
                    row("Bug").at("2017-09-25").withThroughput(0),
                    row("Task").at("2017-09-25").withThroughput(2),
                    row("Bug").at("2017-09-26").withThroughput(1),
                    row("Task").at("2017-09-26").withThroughput(4),
                    row("Bug").at("2017-09-27").withThroughput(0),
                    row("Task").at("2017-09-27").withThroughput(1),
                    row("Bug").at("2017-09-28").withThroughput(1),
                    row("Task").at("2017-09-28").withThroughput(2))
            .withSubtasks(
                    row("Alpha Bug").at("2017-09-25").withThroughput(0),
                    row("Alpha Test").at("2017-09-25").withThroughput(3),
                    row("Backend Development").at("2017-09-25").withThroughput(6),
                    row("Alpha Bug").at("2017-09-26").withThroughput(1),
                    row("Alpha Test").at("2017-09-26").withThroughput(4),
                    row("Backend Development").at("2017-09-26").withThroughput(8),
                    row("Alpha Bug").at("2017-09-27").withThroughput(0),
                    row("Alpha Test").at("2017-09-27").withThroughput(3),
                    row("Backend Development").at("2017-09-27").withThroughput(4),
                    row("Alpha Bug").at("2017-09-28").withThroughput(1),
                    row("Alpha Test").at("2017-09-28").withThroughput(2),
                    row("Backend Development").at("2017-09-28").withThroughput(5))
            .configure();

        ThroughputChartDataSet dsSubtasks = subject.getDataSet(projectKey, issueLevel, ZONE_ID);

        assertThat(dsSubtasks.rows.size(), is(12));
        assertRow(dsSubtasks.rows.get(0), "Alpha Bug", 0L, "2017-09-25");
        assertRow(dsSubtasks.rows.get(1), "Alpha Test", 3L, "2017-09-25");
        assertRow(dsSubtasks.rows.get(2), "Backend Development", 6L, "2017-09-25");
        assertRow(dsSubtasks.rows.get(3), "Alpha Bug", 1L, "2017-09-26");
        assertRow(dsSubtasks.rows.get(4), "Alpha Test", 4L, "2017-09-26");
        assertRow(dsSubtasks.rows.get(5), "Backend Development", 8L, "2017-09-26");
        assertRow(dsSubtasks.rows.get(6), "Alpha Bug", 0L, "2017-09-27");
        assertRow(dsSubtasks.rows.get(7), "Alpha Test", 3L, "2017-09-27");
        assertRow(dsSubtasks.rows.get(8), "Backend Development", 4L, "2017-09-27");
        assertRow(dsSubtasks.rows.get(9), "Alpha Bug", 1L, "2017-09-28");
        assertRow(dsSubtasks.rows.get(10), "Alpha Test", 2L, "2017-09-28");
        assertRow(dsSubtasks.rows.get(11), "Backend Development", 5L, "2017-09-28");
    }

    @Test
    public void getThroughputChartDataSet_timelineRangeBeforeDateRangeFromData() {
        String projectKey = "TASKB";
        String issueLevel = LEVEL_SUBTASKS;
        
        new TestEnvConfigurator(projectKey)
            .withTimelineReferenceDate("2017-09-25")
            .withTimelineStartDate("2017-09-20")
            .withTimelineDeliveryDate("2017-09-23")
            .withTimelineBaselineDate("2017-09-20")
            .withDemands(
                    row("Demand").at("2017-09-25").withThroughput(1),
                    row("Demand").at("2017-09-26").withThroughput(2),
                    row("Demand").at("2017-09-27").withThroughput(0),
                    row("Demand").at("2017-09-28").withThroughput(1))
            .withFeatures(
                    row("Bug").at("2017-09-25").withThroughput(0),
                    row("Task").at("2017-09-25").withThroughput(2),
                    row("Bug").at("2017-09-26").withThroughput(1),
                    row("Task").at("2017-09-26").withThroughput(4),
                    row("Bug").at("2017-09-27").withThroughput(0),
                    row("Task").at("2017-09-27").withThroughput(1),
                    row("Bug").at("2017-09-28").withThroughput(1),
                    row("Task").at("2017-09-28").withThroughput(2))
            .withSubtasks(
                    row("Alpha Bug").at("2017-09-25").withThroughput(0),
                    row("Alpha Test").at("2017-09-25").withThroughput(3),
                    row("Backend Development").at("2017-09-25").withThroughput(6),
                    row("Alpha Bug").at("2017-09-26").withThroughput(1),
                    row("Alpha Test").at("2017-09-26").withThroughput(4),
                    row("Backend Development").at("2017-09-26").withThroughput(8),
                    row("Alpha Bug").at("2017-09-27").withThroughput(0),
                    row("Alpha Test").at("2017-09-27").withThroughput(3),
                    row("Backend Development").at("2017-09-27").withThroughput(4),
                    row("Alpha Bug").at("2017-09-28").withThroughput(1),
                    row("Alpha Test").at("2017-09-28").withThroughput(2),
                    row("Backend Development").at("2017-09-28").withThroughput(5))
            .configure();
        
        ThroughputChartDataSet dsSubtasks = subject.getDataSet(projectKey, issueLevel, ZONE_ID);

        assertThat(dsSubtasks.rows.size(), is(0));
    }

    @Test
    public void getThroughputChartDataSet_timelineRangeAfterDataRangeFromData() {
        String projectKey = "TASKB";
        String issueLevel = LEVEL_SUBTASKS;
        
        new TestEnvConfigurator(projectKey)
            .withTimelineReferenceDate("2017-09-25")
            .withTimelineStartDate("2017-09-29")
            .withTimelineDeliveryDate("2017-09-30")
            .withTimelineBaselineDate("2017-09-29")
            .withDemands(
                    row("Demand").at("2017-09-25").withThroughput(1),
                    row("Demand").at("2017-09-26").withThroughput(2),
                    row("Demand").at("2017-09-27").withThroughput(0),
                    row("Demand").at("2017-09-28").withThroughput(1))
            .withFeatures(
                    row("Bug").at("2017-09-25").withThroughput(0),
                    row("Task").at("2017-09-25").withThroughput(2),
                    row("Bug").at("2017-09-26").withThroughput(1),
                    row("Task").at("2017-09-26").withThroughput(4),
                    row("Bug").at("2017-09-27").withThroughput(0),
                    row("Task").at("2017-09-27").withThroughput(1),
                    row("Bug").at("2017-09-28").withThroughput(1),
                    row("Task").at("2017-09-28").withThroughput(2))
            .withSubtasks(
                    row("Alpha Bug").at("2017-09-25").withThroughput(0),
                    row("Alpha Test").at("2017-09-25").withThroughput(3),
                    row("Backend Development").at("2017-09-25").withThroughput(6),
                    row("Alpha Bug").at("2017-09-26").withThroughput(1),
                    row("Alpha Test").at("2017-09-26").withThroughput(4),
                    row("Backend Development").at("2017-09-26").withThroughput(8),
                    row("Alpha Bug").at("2017-09-27").withThroughput(0),
                    row("Alpha Test").at("2017-09-27").withThroughput(3),
                    row("Backend Development").at("2017-09-27").withThroughput(4),
                    row("Alpha Bug").at("2017-09-28").withThroughput(1),
                    row("Alpha Test").at("2017-09-28").withThroughput(2),
                    row("Backend Development").at("2017-09-28").withThroughput(5))
            .configure();

        ThroughputChartDataSet dsSubtasks = subject.getDataSet(projectKey, issueLevel, ZONE_ID);

        assertThat(dsSubtasks.rows.size(), is(0));
    }
    
    @Test
    public void getThroughputChartDataSet_timelineRangeStartsBeforeDataRangeFromData() {
        String projectKey = "TASKB";
        String issueLevel = LEVEL_SUBTASKS;
        
        new TestEnvConfigurator(projectKey)
            .withTimelineReferenceDate("2017-09-25")
            .withTimelineStartDate("2017-09-20")
            .withTimelineDeliveryDate("2017-09-28")
            .withTimelineBaselineDate("2017-09-25")
            .withDemands(
                    row("Demand").at("2017-09-25").withThroughput(1),
                    row("Demand").at("2017-09-26").withThroughput(2),
                    row("Demand").at("2017-09-27").withThroughput(0),
                    row("Demand").at("2017-09-28").withThroughput(1))
            .withFeatures(
                    row("Bug").at("2017-09-25").withThroughput(0),
                    row("Task").at("2017-09-25").withThroughput(2),
                    row("Bug").at("2017-09-26").withThroughput(1),
                    row("Task").at("2017-09-26").withThroughput(4),
                    row("Bug").at("2017-09-27").withThroughput(0),
                    row("Task").at("2017-09-27").withThroughput(1),
                    row("Bug").at("2017-09-28").withThroughput(1),
                    row("Task").at("2017-09-28").withThroughput(2))
            .withSubtasks(
                    row("Alpha Bug").at("2017-09-25").withThroughput(0),
                    row("Alpha Test").at("2017-09-25").withThroughput(3),
                    row("Backend Development").at("2017-09-25").withThroughput(6),
                    row("Alpha Bug").at("2017-09-26").withThroughput(1),
                    row("Alpha Test").at("2017-09-26").withThroughput(4),
                    row("Backend Development").at("2017-09-26").withThroughput(8),
                    row("Alpha Bug").at("2017-09-27").withThroughput(0),
                    row("Alpha Test").at("2017-09-27").withThroughput(3),
                    row("Backend Development").at("2017-09-27").withThroughput(4),
                    row("Alpha Bug").at("2017-09-28").withThroughput(1),
                    row("Alpha Test").at("2017-09-28").withThroughput(2),
                    row("Backend Development").at("2017-09-28").withThroughput(5))
            .configure();

        ThroughputChartDataSet dsSubtasks = subject.getDataSet(projectKey, issueLevel, ZONE_ID);

        assertThat(dsSubtasks.rows.size(), is(12));
        assertRow(dsSubtasks.rows.get(0), "Alpha Bug", 0L, "2017-09-25");
        assertRow(dsSubtasks.rows.get(1), "Alpha Test", 3L, "2017-09-25");
        assertRow(dsSubtasks.rows.get(2), "Backend Development", 6L, "2017-09-25");
        assertRow(dsSubtasks.rows.get(3), "Alpha Bug", 1L, "2017-09-26");
        assertRow(dsSubtasks.rows.get(4), "Alpha Test", 4L, "2017-09-26");
        assertRow(dsSubtasks.rows.get(5), "Backend Development", 8L, "2017-09-26");
        assertRow(dsSubtasks.rows.get(6), "Alpha Bug", 0L, "2017-09-27");
        assertRow(dsSubtasks.rows.get(7), "Alpha Test", 3L, "2017-09-27");
        assertRow(dsSubtasks.rows.get(8), "Backend Development", 4L, "2017-09-27");
        assertRow(dsSubtasks.rows.get(9), "Alpha Bug", 1L, "2017-09-28");
        assertRow(dsSubtasks.rows.get(10), "Alpha Test", 2L, "2017-09-28");
        assertRow(dsSubtasks.rows.get(11), "Backend Development", 5L, "2017-09-28");
    }
    
    @Test
    public void getThroughputChartDataSet_timelineRangeEndsAfterDataRangeFromData() {
        String projectKey = "TASKB";
        String issueLevel = LEVEL_SUBTASKS;
        
        new TestEnvConfigurator(projectKey)
            .withTimelineReferenceDate("2017-09-25")
            .withTimelineStartDate("2017-09-25")
            .withTimelineDeliveryDate("2017-09-30")
            .withTimelineBaselineDate("2017-09-25")
            .withDemands(
                    row("Demand").at("2017-09-25").withThroughput(1),
                    row("Demand").at("2017-09-26").withThroughput(2),
                    row("Demand").at("2017-09-27").withThroughput(0),
                    row("Demand").at("2017-09-28").withThroughput(1))
            .withFeatures(
                    row("Bug").at("2017-09-25").withThroughput(0),
                    row("Task").at("2017-09-25").withThroughput(2),
                    row("Bug").at("2017-09-26").withThroughput(1),
                    row("Task").at("2017-09-26").withThroughput(4),
                    row("Bug").at("2017-09-27").withThroughput(0),
                    row("Task").at("2017-09-27").withThroughput(1),
                    row("Bug").at("2017-09-28").withThroughput(1),
                    row("Task").at("2017-09-28").withThroughput(2))
            .withSubtasks(
                    row("Alpha Bug").at("2017-09-25").withThroughput(0),
                    row("Alpha Test").at("2017-09-25").withThroughput(3),
                    row("Backend Development").at("2017-09-25").withThroughput(6),
                    row("Alpha Bug").at("2017-09-26").withThroughput(1),
                    row("Alpha Test").at("2017-09-26").withThroughput(4),
                    row("Backend Development").at("2017-09-26").withThroughput(8),
                    row("Alpha Bug").at("2017-09-27").withThroughput(0),
                    row("Alpha Test").at("2017-09-27").withThroughput(3),
                    row("Backend Development").at("2017-09-27").withThroughput(4),
                    row("Alpha Bug").at("2017-09-28").withThroughput(1),
                    row("Alpha Test").at("2017-09-28").withThroughput(2),
                    row("Backend Development").at("2017-09-28").withThroughput(5))
            .configure();

        ThroughputChartDataSet dsSubtasks = subject.getDataSet(projectKey, issueLevel, ZONE_ID);

        assertThat(dsSubtasks.rows.size(), is(12));
        assertRow(dsSubtasks.rows.get(0), "Alpha Bug", 0L, "2017-09-25");
        assertRow(dsSubtasks.rows.get(1), "Alpha Test", 3L, "2017-09-25");
        assertRow(dsSubtasks.rows.get(2), "Backend Development", 6L, "2017-09-25");
        assertRow(dsSubtasks.rows.get(3), "Alpha Bug", 1L, "2017-09-26");
        assertRow(dsSubtasks.rows.get(4), "Alpha Test", 4L, "2017-09-26");
        assertRow(dsSubtasks.rows.get(5), "Backend Development", 8L, "2017-09-26");
        assertRow(dsSubtasks.rows.get(6), "Alpha Bug", 0L, "2017-09-27");
        assertRow(dsSubtasks.rows.get(7), "Alpha Test", 3L, "2017-09-27");
        assertRow(dsSubtasks.rows.get(8), "Backend Development", 4L, "2017-09-27");
        assertRow(dsSubtasks.rows.get(9), "Alpha Bug", 1L, "2017-09-28");
        assertRow(dsSubtasks.rows.get(10), "Alpha Test", 2L, "2017-09-28");
        assertRow(dsSubtasks.rows.get(11), "Backend Development", 5L, "2017-09-28");
    }
    
    @Test
    public void getThroughputChartDataSet_timelineRangeContainsDataRangeFromData() {
        String projectKey = "TASKB";
        String issueLevel = LEVEL_SUBTASKS;
        
        new TestEnvConfigurator(projectKey)
            .withTimelineReferenceDate("2017-09-25")
            .withTimelineStartDate("2017-09-23")
            .withTimelineDeliveryDate("2017-09-30")
            .withTimelineBaselineDate("2017-09-25")
            .withDemands(
                    row("Demand").at("2017-09-25").withThroughput(1),
                    row("Demand").at("2017-09-26").withThroughput(2),
                    row("Demand").at("2017-09-27").withThroughput(0),
                    row("Demand").at("2017-09-28").withThroughput(1))
            .withFeatures(
                    row("Bug").at("2017-09-25").withThroughput(0),
                    row("Task").at("2017-09-25").withThroughput(2),
                    row("Bug").at("2017-09-26").withThroughput(1),
                    row("Task").at("2017-09-26").withThroughput(4),
                    row("Bug").at("2017-09-27").withThroughput(0),
                    row("Task").at("2017-09-27").withThroughput(1),
                    row("Bug").at("2017-09-28").withThroughput(1),
                    row("Task").at("2017-09-28").withThroughput(2))
            .withSubtasks(
                    row("Alpha Bug").at("2017-09-25").withThroughput(0),
                    row("Alpha Test").at("2017-09-25").withThroughput(3),
                    row("Backend Development").at("2017-09-25").withThroughput(6),
                    row("Alpha Bug").at("2017-09-26").withThroughput(1),
                    row("Alpha Test").at("2017-09-26").withThroughput(4),
                    row("Backend Development").at("2017-09-26").withThroughput(8),
                    row("Alpha Bug").at("2017-09-27").withThroughput(0),
                    row("Alpha Test").at("2017-09-27").withThroughput(3),
                    row("Backend Development").at("2017-09-27").withThroughput(4),
                    row("Alpha Bug").at("2017-09-28").withThroughput(1),
                    row("Alpha Test").at("2017-09-28").withThroughput(2),
                    row("Backend Development").at("2017-09-28").withThroughput(5))
            .configure();

        ThroughputChartDataSet dsSubtasks = subject.getDataSet(projectKey, issueLevel, ZONE_ID);

        assertThat(dsSubtasks.rows.size(), is(12));
        assertRow(dsSubtasks.rows.get(0), "Alpha Bug", 0L, "2017-09-25");
        assertRow(dsSubtasks.rows.get(1), "Alpha Test", 3L, "2017-09-25");
        assertRow(dsSubtasks.rows.get(2), "Backend Development", 6L, "2017-09-25");
        assertRow(dsSubtasks.rows.get(3), "Alpha Bug", 1L, "2017-09-26");
        assertRow(dsSubtasks.rows.get(4), "Alpha Test", 4L, "2017-09-26");
        assertRow(dsSubtasks.rows.get(5), "Backend Development", 8L, "2017-09-26");
        assertRow(dsSubtasks.rows.get(6), "Alpha Bug", 0L, "2017-09-27");
        assertRow(dsSubtasks.rows.get(7), "Alpha Test", 3L, "2017-09-27");
        assertRow(dsSubtasks.rows.get(8), "Backend Development", 4L, "2017-09-27");
        assertRow(dsSubtasks.rows.get(9), "Alpha Bug", 1L, "2017-09-28");
        assertRow(dsSubtasks.rows.get(10), "Alpha Test", 2L, "2017-09-28");
        assertRow(dsSubtasks.rows.get(11), "Backend Development", 5L, "2017-09-28");
    }
    
    @Test
    public void getThroughputChartDataSet_timelineRangeWithinDataRangeFromData() {
        String projectKey = "TASKB";
        String issueLevel = LEVEL_SUBTASKS;
        
        new TestEnvConfigurator(projectKey)
            .withTimelineReferenceDate("2017-09-25")
            .withTimelineStartDate("2017-09-26")
            .withTimelineDeliveryDate("2017-09-27")
            .withTimelineBaselineDate("2017-09-26")
            .withDemands(
                    row("Demand").at("2017-09-25").withThroughput(1),
                    row("Demand").at("2017-09-26").withThroughput(2),
                    row("Demand").at("2017-09-27").withThroughput(0),
                    row("Demand").at("2017-09-28").withThroughput(1))
            .withFeatures(
                    row("Bug").at("2017-09-25").withThroughput(0),
                    row("Task").at("2017-09-25").withThroughput(2),
                    row("Bug").at("2017-09-26").withThroughput(1),
                    row("Task").at("2017-09-26").withThroughput(4),
                    row("Bug").at("2017-09-27").withThroughput(0),
                    row("Task").at("2017-09-27").withThroughput(1),
                    row("Bug").at("2017-09-28").withThroughput(1),
                    row("Task").at("2017-09-28").withThroughput(2))
            .withSubtasks(
                    row("Alpha Bug").at("2017-09-25").withThroughput(0),
                    row("Alpha Test").at("2017-09-25").withThroughput(3),
                    row("Backend Development").at("2017-09-25").withThroughput(6),
                    row("Alpha Bug").at("2017-09-26").withThroughput(1),
                    row("Alpha Test").at("2017-09-26").withThroughput(4),
                    row("Backend Development").at("2017-09-26").withThroughput(8),
                    row("Alpha Bug").at("2017-09-27").withThroughput(0),
                    row("Alpha Test").at("2017-09-27").withThroughput(3),
                    row("Backend Development").at("2017-09-27").withThroughput(4),
                    row("Alpha Bug").at("2017-09-28").withThroughput(1),
                    row("Alpha Test").at("2017-09-28").withThroughput(2),
                    row("Backend Development").at("2017-09-28").withThroughput(5))
            .configure();

        ThroughputChartDataSet dsSubtasks = subject.getDataSet(projectKey, issueLevel, ZONE_ID);

        assertThat(dsSubtasks.rows.size(), is(6));
        assertRow(dsSubtasks.rows.get(0), "Alpha Bug", 1L, "2017-09-26");
        assertRow(dsSubtasks.rows.get(1), "Alpha Test", 4L, "2017-09-26");
        assertRow(dsSubtasks.rows.get(2), "Backend Development", 8L, "2017-09-26");
        assertRow(dsSubtasks.rows.get(3), "Alpha Bug", 0L, "2017-09-27");
        assertRow(dsSubtasks.rows.get(4), "Alpha Test", 3L, "2017-09-27");
        assertRow(dsSubtasks.rows.get(5), "Backend Development", 4L, "2017-09-27");
    }
    
    @Test
    public void getThroughputChartDataSet_timelineDatesNotSet() {
        String projectKey = "TASKB";
        String issueLevel = LEVEL_DEMANDS;

        new TestEnvConfigurator(projectKey)
            .withTimelineReferenceDate("2017-09-25")
            .withDemands(
                    row("Demand").at("2017-09-25").withThroughput(1),
                    row("Demand").at("2017-09-26").withThroughput(2),
                    row("Demand").at("2017-09-27").withThroughput(0),
                    row("Demand").at("2017-09-28").withThroughput(1))
            .withFeatures(
                    row("Bug").at("2017-09-25").withThroughput(0),
                    row("Task").at("2017-09-25").withThroughput(2),
                    row("Bug").at("2017-09-26").withThroughput(1),
                    row("Task").at("2017-09-26").withThroughput(4),
                    row("Bug").at("2017-09-27").withThroughput(0),
                    row("Task").at("2017-09-27").withThroughput(1),
                    row("Bug").at("2017-09-28").withThroughput(1),
                    row("Task").at("2017-09-28").withThroughput(2))
            .withSubtasks(
                    row("Alpha Bug").at("2017-09-25").withThroughput(0),
                    row("Alpha Test").at("2017-09-25").withThroughput(3),
                    row("Backend Development").at("2017-09-25").withThroughput(6),
                    row("Alpha Bug").at("2017-09-26").withThroughput(1),
                    row("Alpha Test").at("2017-09-26").withThroughput(4),
                    row("Backend Development").at("2017-09-26").withThroughput(8),
                    row("Alpha Bug").at("2017-09-27").withThroughput(0),
                    row("Alpha Test").at("2017-09-27").withThroughput(3),
                    row("Backend Development").at("2017-09-27").withThroughput(4),
                    row("Alpha Bug").at("2017-09-28").withThroughput(1),
                    row("Alpha Test").at("2017-09-28").withThroughput(2),
                    row("Backend Development").at("2017-09-28").withThroughput(5))
            .configure();

        final ThroughputChartDataSet dsAllLevels = subject.getDataSet(projectKey, issueLevel, ZONE_ID);
        // when timeline dates aren't set returns all data
        assertThat(dsAllLevels.rows.size(), is(4));
        assertRow(dsAllLevels.rows.get(0), "Demand", 1L, "2017-09-25");
        assertRow(dsAllLevels.rows.get(1), "Demand", 2L, "2017-09-26");
        assertRow(dsAllLevels.rows.get(2), "Demand", 0L, "2017-09-27");
        assertRow(dsAllLevels.rows.get(3), "Demand", 1L, "2017-09-28");
    }

    @Test
    public void getThroughputChartDataSet_fetchDataFromAllLevels() {
        String projectKey = "TASKB";
        String issueLevel = "All";

        new TestEnvConfigurator(projectKey)
            .withTimelineReferenceDate("2017-09-25")
            .withTimelineStartDate("2017-09-25")
            .withTimelineDeliveryDate("2017-09-28")
            .withTimelineBaselineDate("2017-09-25")
            .withDemands(
                    row("Demand").at("2017-09-25").withThroughput(1),
                    row("Demand").at("2017-09-26").withThroughput(2),
                    row("Demand").at("2017-09-27").withThroughput(0),
                    row("Demand").at("2017-09-28").withThroughput(1))
            .withFeatures(
                    row("Bug").at("2017-09-25").withThroughput(0),
                    row("Task").at("2017-09-25").withThroughput(2),
                    row("Bug").at("2017-09-26").withThroughput(1),
                    row("Task").at("2017-09-26").withThroughput(4),
                    row("Bug").at("2017-09-27").withThroughput(0),
                    row("Task").at("2017-09-27").withThroughput(1),
                    row("Bug").at("2017-09-28").withThroughput(1),
                    row("Task").at("2017-09-28").withThroughput(2))
            .withSubtasks(
                    row("Alpha Bug").at("2017-09-25").withThroughput(0),
                    row("Alpha Test").at("2017-09-25").withThroughput(3),
                    row("Backend Development").at("2017-09-25").withThroughput(6),
                    row("Alpha Bug").at("2017-09-26").withThroughput(1),
                    row("Alpha Test").at("2017-09-26").withThroughput(4),
                    row("Backend Development").at("2017-09-26").withThroughput(8),
                    row("Alpha Bug").at("2017-09-27").withThroughput(0),
                    row("Alpha Test").at("2017-09-27").withThroughput(3),
                    row("Backend Development").at("2017-09-27").withThroughput(4),
                    row("Alpha Bug").at("2017-09-28").withThroughput(1),
                    row("Alpha Test").at("2017-09-28").withThroughput(2),
                    row("Backend Development").at("2017-09-28").withThroughput(5))
            .configure();

        ThroughputChartDataSet dsAllLevels = subject.getDataSet(projectKey, issueLevel, ZONE_ID);

        assertThat(dsAllLevels.rows.size(), is(24));
        assertRow(dsAllLevels.rows.get(0), "Demand", 1L, "2017-09-25");
        assertRow(dsAllLevels.rows.get(1), "Demand", 2L, "2017-09-26");
        assertRow(dsAllLevels.rows.get(2), "Demand", 0L, "2017-09-27");
        assertRow(dsAllLevels.rows.get(3), "Demand", 1L, "2017-09-28");
        assertRow(dsAllLevels.rows.get(4), "Bug", 0L, "2017-09-25");
        assertRow(dsAllLevels.rows.get(5), "Task", 2L, "2017-09-25");
        assertRow(dsAllLevels.rows.get(6), "Bug", 1L, "2017-09-26");
        assertRow(dsAllLevels.rows.get(7), "Task", 4L, "2017-09-26");
        assertRow(dsAllLevels.rows.get(8), "Bug", 0L, "2017-09-27");
        assertRow(dsAllLevels.rows.get(9), "Task", 1L, "2017-09-27");
        assertRow(dsAllLevels.rows.get(10), "Bug", 1L, "2017-09-28");
        assertRow(dsAllLevels.rows.get(11), "Task", 2L, "2017-09-28");
        assertRow(dsAllLevels.rows.get(12), "Alpha Bug", 0L, "2017-09-25");
        assertRow(dsAllLevels.rows.get(13), "Alpha Test", 3L, "2017-09-25");
        assertRow(dsAllLevels.rows.get(14), "Backend Development", 6L, "2017-09-25");
        assertRow(dsAllLevels.rows.get(15), "Alpha Bug", 1L, "2017-09-26");
        assertRow(dsAllLevels.rows.get(16), "Alpha Test", 4L, "2017-09-26");
        assertRow(dsAllLevels.rows.get(17), "Backend Development", 8L, "2017-09-26");
        assertRow(dsAllLevels.rows.get(18), "Alpha Bug", 0L, "2017-09-27");
        assertRow(dsAllLevels.rows.get(19), "Alpha Test", 3L, "2017-09-27");
        assertRow(dsAllLevels.rows.get(20), "Backend Development", 4L, "2017-09-27");
        assertRow(dsAllLevels.rows.get(21), "Alpha Bug", 1L, "2017-09-28");
        assertRow(dsAllLevels.rows.get(22), "Alpha Test", 2L, "2017-09-28");
        assertRow(dsAllLevels.rows.get(23), "Backend Development", 5L, "2017-09-28");
    }

    @Test
    public void getThroughputChartDataSet_withoutSubtasksDataSet() {
        String projectKey = "TASKB";
        String issueLevel = LEVEL_FEATURES;
        
        
        new TestEnvConfigurator(projectKey)
            .withTimelineReferenceDate("2017-09-25")
            .withDemands(
                    row("Demand").at("2017-09-25").withThroughput(1),
                    row("Demand").at("2017-09-26").withThroughput(2),
                    row("Demand").at("2017-09-27").withThroughput(0),
                    row("Demand").at("2017-09-28").withThroughput(1))
            .withFeatures(
                    row("Bug").at("2017-09-25").withThroughput(0),
                    row("Task").at("2017-09-25").withThroughput(2),
                    row("Bug").at("2017-09-26").withThroughput(1),
                    row("Task").at("2017-09-26").withThroughput(4),
                    row("Bug").at("2017-09-27").withThroughput(0),
                    row("Task").at("2017-09-27").withThroughput(1),
                    row("Bug").at("2017-09-28").withThroughput(1),
                    row("Task").at("2017-09-28").withThroughput(2))
            .configure();

        final ThroughputChartDataSet dsAllLevels = subject.getDataSet(projectKey, issueLevel, ZONE_ID);
        assertThat(dsAllLevels.rows.size(), is(8));
        assertRow(dsAllLevels.rows.get(0), "Bug", 0L, "2017-09-25");
        assertRow(dsAllLevels.rows.get(1), "Task", 2L, "2017-09-25");
        assertRow(dsAllLevels.rows.get(2), "Bug", 1L, "2017-09-26");
        assertRow(dsAllLevels.rows.get(3), "Task", 4L, "2017-09-26");
        assertRow(dsAllLevels.rows.get(4), "Bug", 0L, "2017-09-27");
        assertRow(dsAllLevels.rows.get(5), "Task", 1L, "2017-09-27");
        assertRow(dsAllLevels.rows.get(6), "Bug", 1L, "2017-09-28");
        assertRow(dsAllLevels.rows.get(7), "Task", 2L, "2017-09-28");
    }
    
    @Test
    public void getThroughputChartDataSet_demandsDataSetOnly() {
        String projectKey = "TASKB";
        String issueLevel = "All";
        
        
        new TestEnvConfigurator(projectKey)
            .withTimelineReferenceDate("2017-09-25")
            .withTimelineStartDate("2017-09-25")
            .withTimelineDeliveryDate("2017-09-28")
            .withTimelineBaselineDate("2017-09-25")
            .withDemands(
                    row("Demand").at("2017-09-25").withThroughput(1),
                    row("Demand").at("2017-09-26").withThroughput(2),
                    row("Demand").at("2017-09-27").withThroughput(0),
                    row("Demand").at("2017-09-28").withThroughput(1))
            .configure();

        final ThroughputChartDataSet dsAllLevels = subject.getDataSet(projectKey, issueLevel, ZONE_ID);
        assertThat(dsAllLevels.rows.size(), is(4));
        assertRow(dsAllLevels.rows.get(0), "Demand", 1L, "2017-09-25");
        assertRow(dsAllLevels.rows.get(1), "Demand", 2L, "2017-09-26");
        assertRow(dsAllLevels.rows.get(2), "Demand", 0L, "2017-09-27");
        assertRow(dsAllLevels.rows.get(3), "Demand", 1L, "2017-09-28");
    }

    @Test
    public void getThroughputChartDataSet_timelineDatesNotSetDatasetEmpty() {
        String projectKey = "TASKB";
        String issueLevel = "All";

        new TestEnvConfigurator(projectKey)
            .withTimelineReferenceDate("2017-09-25")
            .configure();

        final ThroughputChartDataSet ds = subject.getDataSet(projectKey, issueLevel, ZONE_ID);
        assertThat(ds.rows.size(), is(0));
    }

    private void assertRow(ThroughputDataPoint throughputDataPoint, String issueType, Long throughputCount, String date) {
        assertThat(throughputDataPoint.issueType, is(issueType));
        assertThat(throughputDataPoint.date, is(Date.from(parseDateTime(date).toInstant())));
        assertThat(throughputDataPoint.count, is(throughputCount));
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
            List<ThroughputDataSet> dataSets = Arrays.asList(
                    dsBuilders.get(LEVEL_DEMANDS).build(), 
                    dsBuilders.get(LEVEL_FEATURES).build(), 
                    dsBuilders.get(LEVEL_SUBTASKS).build());
            when(dataRepository.getFirstDate(projectKey)).thenReturn(startDate);
            mockThroughputKpiService(dataSets);
        }
        
        private void mockThroughputKpiService(List<ThroughputDataSet> dataSets) {
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
