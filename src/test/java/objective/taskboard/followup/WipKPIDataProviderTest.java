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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.cluster.EmptyFollowupCluster;
import objective.taskboard.followup.kpi.WipChartDataSet;
import objective.taskboard.followup.kpi.WipDataPoint;
import objective.taskboard.followup.kpi.WipKPIService;

@RunWith(MockitoJUnitRunner.class)
public class WipKPIDataProviderTest {

    private static final String LEVEL_ALL = "All";
    private static final String LEVEL_DEMANDS = "Demand";
    private static final String LEVEL_FEATURES = "Feature";
    private static final String LEVEL_SUBTASKS = "Subtask";
    private static final ZoneId ZONE_ID = ZoneId.systemDefault();

    @Mock
    private FollowUpDataRepository dataRepository;

    @Mock
    private FollowUpSnapshotService snapshotService;

    @Mock
    private WipKPIService wipKpiService;
    
    @Spy
    private WeekRangeNormalizer weekRangeNormalizer = new WeekRangeNormalizer();

    @InjectMocks
    private WipKPIDataProvider subject = new WipKPIDataProvider();

    @Test
    public void getWipChartDataSet_whenTimelineRangeEqualsDataSetRange_thenNotFiltering() {
        String projectKey = "TASKB";
        String issueLevel = LEVEL_ALL;
        
        new TestEnvConfigurator(projectKey)
            .withTimelineReferenceDate("2018-09-25")
            .withTimelineStartDate("2018-09-25")
            .withTimelineDeliveryDate("2018-10-05")
            .withTimelineBaselineDate("2018-09-25")
            .withDemands(
                    row("Demand").withStatus("Development").at("2018-09-25").withWIP(1),
                    row("Demand").withStatus("Development").at("2018-09-26").withWIP(1),
                    row("Demand").withStatus("Development").at("2018-09-27").withWIP(1),
                    row("Demand").withStatus("Development").at("2018-09-28").withWIP(1),
                    row("Demand").withStatus("Development").at("2018-09-29").withWIP(1),
                    row("Demand").withStatus("Development").at("2018-09-30").withWIP(1),
                    row("Demand").withStatus("Development").at("2018-10-01").withWIP(1),
                    row("Demand").withStatus("Development").at("2018-10-02").withWIP(1),
                    row("Demand").withStatus("Development").at("2018-10-03").withWIP(2),
                    row("Demand").withStatus("Development").at("2018-10-04").withWIP(2),
                    row("Demand").withStatus("Development").at("2018-10-05").withWIP(2))
            .withFeatures(
                    row("Bug").withStatus("Development").at("2018-09-25").withWIP(0),
                    row("Task").withStatus("Development").at("2018-09-25").withWIP(2),
                    row("Bug").withStatus("Development").at("2018-09-26").withWIP(1),
                    row("Task").withStatus("Development").at("2018-09-26").withWIP(4),
                    row("Bug").withStatus("Development").at("2018-09-27").withWIP(0),
                    row("Task").withStatus("Development").at("2018-09-27").withWIP(1),
                    row("Bug").withStatus("Development").at("2018-09-28").withWIP(0),
                    row("Task").withStatus("Development").at("2018-09-28").withWIP(1),
                    row("Bug").withStatus("Development").at("2018-09-29").withWIP(0),
                    row("Task").withStatus("Development").at("2018-09-29").withWIP(1),
                    row("Bug").withStatus("Development").at("2018-09-30").withWIP(0),
                    row("Task").withStatus("Development").at("2018-09-30").withWIP(1),
                    row("Bug").withStatus("Development").at("2018-10-01").withWIP(0),
                    row("Task").withStatus("Development").at("2018-10-01").withWIP(1),
                    row("Bug").withStatus("Development").at("2018-10-02").withWIP(0),
                    row("Task").withStatus("Development").at("2018-10-02").withWIP(1),
                    row("Bug").withStatus("Development").at("2018-10-03").withWIP(0),
                    row("Task").withStatus("Development").at("2018-10-03").withWIP(1),
                    row("Bug").withStatus("Development").at("2018-10-04").withWIP(0),
                    row("Task").withStatus("Development").at("2018-10-04").withWIP(1),
                    row("Bug").withStatus("Development").at("2018-10-05").withWIP(0),
                    row("Task").withStatus("Development").at("2018-10-05").withWIP(1))
            .withSubtasks(
                    row("Alpha Test").withStatus("Doing").at("2018-09-25").withWIP(0),
                    row("Alpha Test").withStatus("Review").at("2018-09-25").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-09-25").withWIP(6),
                    row("Backend Development").withStatus("Review").at("2018-09-25").withWIP(0),
                    row("Alpha Test").withStatus("Doing").at("2018-09-26").withWIP(0),
                    row("Alpha Test").withStatus("Review").at("2018-09-26").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-09-26").withWIP(8),
                    row("Backend Development").withStatus("Review").at("2018-09-26").withWIP(0),
                    row("Alpha Test").withStatus("Doing").at("2018-09-27").withWIP(0),
                    row("Alpha Test").withStatus("Review").at("2018-09-27").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-09-27").withWIP(4),
                    row("Backend Development").withStatus("Review").at("2018-09-27").withWIP(2),
                    row("Alpha Test").withStatus("Doing").at("2018-09-28").withWIP(0),
                    row("Alpha Test").withStatus("Review").at("2018-09-28").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-09-28").withWIP(4),
                    row("Backend Development").withStatus("Review").at("2018-09-28").withWIP(2),
                    row("Alpha Test").withStatus("Doing").at("2018-09-29").withWIP(0),
                    row("Alpha Test").withStatus("Review").at("2018-09-29").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-09-29").withWIP(4),
                    row("Backend Development").withStatus("Review").at("2018-09-29").withWIP(1),
                    row("Alpha Test").withStatus("Doing").at("2018-09-30").withWIP(1),
                    row("Alpha Test").withStatus("Review").at("2018-09-30").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-09-30").withWIP(4),
                    row("Backend Development").withStatus("Review").at("2018-09-30").withWIP(0),
                    row("Alpha Test").withStatus("Doing").at("2018-10-01").withWIP(1),
                    row("Alpha Test").withStatus("Review").at("2018-10-01").withWIP(1),
                    row("Backend Development").withStatus("Doing").at("2018-10-01").withWIP(4),
                    row("Backend Development").withStatus("Review").at("2018-09-01").withWIP(0),
                    row("Alpha Test").withStatus("Doing").at("2018-10-02").withWIP(1),
                    row("Alpha Test").withStatus("Review").at("2018-10-02").withWIP(1),
                    row("Backend Development").withStatus("Doing").at("2018-10-02").withWIP(4),
                    row("Backend Development").withStatus("Review").at("2018-10-02").withWIP(0),
                    row("Alpha Test").withStatus("Doing").at("2018-10-03").withWIP(1),
                    row("Alpha Test").withStatus("Review").at("2018-10-03").withWIP(1),
                    row("Backend Development").withStatus("Doing").at("2018-10-03").withWIP(3),
                    row("Backend Development").withStatus("Review").at("2018-10-04").withWIP(0),
                    row("Alpha Test").withStatus("Doing").at("2018-10-04").withWIP(1),
                    row("Alpha Test").withStatus("Review").at("2018-10-04").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-10-04").withWIP(4),
                    row("Backend Development").withStatus("Review").at("2018-10-04").withWIP(0),
                    row("Alpha Test").withStatus("Doing").at("2018-10-05").withWIP(1),
                    row("Alpha Test").withStatus("Review").at("2018-10-05").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-10-05").withWIP(5),
                    row("Backend Development").withStatus("Review").at("2018-10-05").withWIP(0))
            .configure();

        WipChartDataSet ds = subject.getDataSet(projectKey, issueLevel, ZONE_ID);

        assertThat(ds.rows.size(), is(14));
        assertRow(ds.rows.get(0), "Demand", "Development", 1.0, "2018-09-23");
        assertRow(ds.rows.get(1), "Demand", "Development", 1.5, "2018-09-30");
        assertRow(ds.rows.get(2), "Bug", "Development", 0.2, "2018-09-23");
        assertRow(ds.rows.get(3), "Task", "Development", 1.8, "2018-09-23");
        assertRow(ds.rows.get(4), "Bug", "Development", 0.0, "2018-09-30");
        assertRow(ds.rows.get(5), "Task", "Development", 1.0, "2018-09-30");
        assertRow(ds.rows.get(6), "Alpha Test", "Doing", 0.0, "2018-09-23");
        assertRow(ds.rows.get(7), "Alpha Test", "Review", 0.0, "2018-09-23");
        assertRow(ds.rows.get(8), "Backend Development", "Doing", 5.2, "2018-09-23");
        assertRow(ds.rows.get(9), "Backend Development", "Review", 1.0, "2018-09-23");
        assertRow(ds.rows.get(10), "Alpha Test", "Doing", 1.0, "2018-09-30");
        assertRow(ds.rows.get(11), "Alpha Test", "Review", 0.5, "2018-09-30");
        assertRow(ds.rows.get(12), "Backend Development", "Doing", 4.0, "2018-09-30");
        assertRow(ds.rows.get(13), "Backend Development", "Review", 0.0, "2018-09-30");
    }

    @Test
    public void getWipChartDataSet_whenTimelineRangeBeforeDataSetDateRange_thenReturnsEmptyDataSet() {
        String projectKey = "TASKB";
        String issueLevel = LEVEL_SUBTASKS;
        
        new TestEnvConfigurator(projectKey)
            .withTimelineReferenceDate("2018-09-25")
            .withTimelineStartDate("2018-09-17")
            .withTimelineDeliveryDate("2018-09-21")
            .withTimelineBaselineDate("2018-09-17")
            .withDemands()
            .withFeatures()
            .withSubtasks(
                    row("Alpha Test").withStatus("Doing").at("2018-09-25").withWIP(0),
                    row("Alpha Test").withStatus("Review").at("2018-09-25").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-09-25").withWIP(6),
                    row("Backend Development").withStatus("Review").at("2018-09-25").withWIP(0),
                    row("Alpha Test").withStatus("Doing").at("2018-09-26").withWIP(0),
                    row("Alpha Test").withStatus("Review").at("2018-09-26").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-09-26").withWIP(8),
                    row("Backend Development").withStatus("Review").at("2018-09-26").withWIP(0),
                    row("Alpha Test").withStatus("Doing").at("2018-09-27").withWIP(0),
                    row("Alpha Test").withStatus("Review").at("2018-09-27").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-09-27").withWIP(4),
                    row("Backend Development").withStatus("Review").at("2018-09-27").withWIP(2),
                    row("Alpha Test").withStatus("Doing").at("2018-09-28").withWIP(0),
                    row("Alpha Test").withStatus("Review").at("2018-09-28").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-09-28").withWIP(4),
                    row("Backend Development").withStatus("Review").at("2018-09-28").withWIP(2),
                    row("Alpha Test").withStatus("Doing").at("2018-09-29").withWIP(0),
                    row("Alpha Test").withStatus("Review").at("2018-09-29").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-09-29").withWIP(4),
                    row("Backend Development").withStatus("Review").at("2018-09-29").withWIP(1),
                    row("Alpha Test").withStatus("Doing").at("2018-09-30").withWIP(1),
                    row("Alpha Test").withStatus("Review").at("2018-09-30").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-09-30").withWIP(4),
                    row("Backend Development").withStatus("Review").at("2018-09-30").withWIP(0),
                    row("Alpha Test").withStatus("Doing").at("2018-10-01").withWIP(1),
                    row("Alpha Test").withStatus("Review").at("2018-10-01").withWIP(1),
                    row("Backend Development").withStatus("Doing").at("2018-10-01").withWIP(4),
                    row("Backend Development").withStatus("Review").at("2018-09-01").withWIP(0),
                    row("Alpha Test").withStatus("Doing").at("2018-10-02").withWIP(1),
                    row("Alpha Test").withStatus("Review").at("2018-10-02").withWIP(1),
                    row("Backend Development").withStatus("Doing").at("2018-10-02").withWIP(4),
                    row("Backend Development").withStatus("Review").at("2018-10-02").withWIP(0),
                    row("Alpha Test").withStatus("Doing").at("2018-10-03").withWIP(1),
                    row("Alpha Test").withStatus("Review").at("2018-10-03").withWIP(1),
                    row("Backend Development").withStatus("Doing").at("2018-10-03").withWIP(3),
                    row("Backend Development").withStatus("Review").at("2018-10-04").withWIP(0),
                    row("Alpha Test").withStatus("Doing").at("2018-10-04").withWIP(1),
                    row("Alpha Test").withStatus("Review").at("2018-10-04").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-10-04").withWIP(4),
                    row("Backend Development").withStatus("Review").at("2018-10-04").withWIP(0),
                    row("Alpha Test").withStatus("Doing").at("2018-10-05").withWIP(1),
                    row("Alpha Test").withStatus("Review").at("2018-10-05").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-10-05").withWIP(5),
                    row("Backend Development").withStatus("Review").at("2018-10-05").withWIP(0))
            .configure();

        WipChartDataSet ds = subject.getDataSet(projectKey, issueLevel, ZONE_ID);

        assertThat(ds.rows.size(), is(0));
    }

    @Test
    public void getWipChartDataSet_whenTimelineRangeAfterDataSetDateRange_thenReturnsEmptyDateSet() {
        String projectKey = "TASKB";
        String issueLevel = LEVEL_SUBTASKS;
        
        new TestEnvConfigurator(projectKey)
            .withTimelineReferenceDate("2018-09-25")
            .withTimelineStartDate("2018-10-08")
            .withTimelineDeliveryDate("2018-10-12")
            .withTimelineBaselineDate("2018-10-08")
            .withDemands()
            .withFeatures()
            .withSubtasks(
                    row("Alpha Test").withStatus("Doing").at("2018-09-25").withWIP(0),
                    row("Alpha Test").withStatus("Review").at("2018-09-25").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-09-25").withWIP(6),
                    row("Backend Development").withStatus("Review").at("2018-09-25").withWIP(0),
                    row("Alpha Test").withStatus("Doing").at("2018-09-26").withWIP(0),
                    row("Alpha Test").withStatus("Review").at("2018-09-26").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-09-26").withWIP(8),
                    row("Backend Development").withStatus("Review").at("2018-09-26").withWIP(0),
                    row("Alpha Test").withStatus("Doing").at("2018-09-27").withWIP(0),
                    row("Alpha Test").withStatus("Review").at("2018-09-27").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-09-27").withWIP(4),
                    row("Backend Development").withStatus("Review").at("2018-09-27").withWIP(2),
                    row("Alpha Test").withStatus("Doing").at("2018-09-28").withWIP(0),
                    row("Alpha Test").withStatus("Review").at("2018-09-28").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-09-28").withWIP(4),
                    row("Backend Development").withStatus("Review").at("2018-09-28").withWIP(2),
                    row("Alpha Test").withStatus("Doing").at("2018-09-29").withWIP(0),
                    row("Alpha Test").withStatus("Review").at("2018-09-29").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-09-29").withWIP(4),
                    row("Backend Development").withStatus("Review").at("2018-09-29").withWIP(1),
                    row("Alpha Test").withStatus("Doing").at("2018-09-30").withWIP(1),
                    row("Alpha Test").withStatus("Review").at("2018-09-30").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-09-30").withWIP(4),
                    row("Backend Development").withStatus("Review").at("2018-09-30").withWIP(0),
                    row("Alpha Test").withStatus("Doing").at("2018-10-01").withWIP(1),
                    row("Alpha Test").withStatus("Review").at("2018-10-01").withWIP(1),
                    row("Backend Development").withStatus("Doing").at("2018-10-01").withWIP(4),
                    row("Backend Development").withStatus("Review").at("2018-09-01").withWIP(0),
                    row("Alpha Test").withStatus("Doing").at("2018-10-02").withWIP(1),
                    row("Alpha Test").withStatus("Review").at("2018-10-02").withWIP(1),
                    row("Backend Development").withStatus("Doing").at("2018-10-02").withWIP(4),
                    row("Backend Development").withStatus("Review").at("2018-10-02").withWIP(0),
                    row("Alpha Test").withStatus("Doing").at("2018-10-03").withWIP(1),
                    row("Alpha Test").withStatus("Review").at("2018-10-03").withWIP(1),
                    row("Backend Development").withStatus("Doing").at("2018-10-03").withWIP(3),
                    row("Backend Development").withStatus("Review").at("2018-10-04").withWIP(0),
                    row("Alpha Test").withStatus("Doing").at("2018-10-04").withWIP(1),
                    row("Alpha Test").withStatus("Review").at("2018-10-04").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-10-04").withWIP(4),
                    row("Backend Development").withStatus("Review").at("2018-10-04").withWIP(0),
                    row("Alpha Test").withStatus("Doing").at("2018-10-05").withWIP(1),
                    row("Alpha Test").withStatus("Review").at("2018-10-05").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-10-05").withWIP(5),
                    row("Backend Development").withStatus("Review").at("2018-10-05").withWIP(0))
            .configure();

        WipChartDataSet ds = subject.getDataSet(projectKey, issueLevel, ZONE_ID);

        assertThat(ds.rows.size(), is(0));
    }
    
    @Test
    public void getWipChartDataSet_whenTimelineRangeStartsBeforeAndEndsWithinDataSetDateRange_thenFiltersEndOnly() {
        String projectKey = "TASKB";
        String issueLevel = LEVEL_SUBTASKS;
        
        new TestEnvConfigurator(projectKey)
            .withTimelineReferenceDate("2018-09-25")
            .withTimelineStartDate("2018-09-20")
            .withTimelineDeliveryDate("2018-09-29")
            .withTimelineBaselineDate("2018-09-20")
            .withDemands()
            .withFeatures()
            .withSubtasks(
                    row("Alpha Test").withStatus("Doing").at("2018-09-25").withWIP(0),
                    row("Alpha Test").withStatus("Review").at("2018-09-25").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-09-25").withWIP(6),
                    row("Backend Development").withStatus("Review").at("2018-09-25").withWIP(0),
                    row("Alpha Test").withStatus("Doing").at("2018-09-26").withWIP(0),
                    row("Alpha Test").withStatus("Review").at("2018-09-26").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-09-26").withWIP(8),
                    row("Backend Development").withStatus("Review").at("2018-09-26").withWIP(0),
                    row("Alpha Test").withStatus("Doing").at("2018-09-27").withWIP(0),
                    row("Alpha Test").withStatus("Review").at("2018-09-27").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-09-27").withWIP(4),
                    row("Backend Development").withStatus("Review").at("2018-09-27").withWIP(2),
                    row("Alpha Test").withStatus("Doing").at("2018-09-28").withWIP(0),
                    row("Alpha Test").withStatus("Review").at("2018-09-28").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-09-28").withWIP(4),
                    row("Backend Development").withStatus("Review").at("2018-09-28").withWIP(2),
                    row("Alpha Test").withStatus("Doing").at("2018-09-29").withWIP(0),
                    row("Alpha Test").withStatus("Review").at("2018-09-29").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-09-29").withWIP(4),
                    row("Backend Development").withStatus("Review").at("2018-09-29").withWIP(1),
                    row("Alpha Test").withStatus("Doing").at("2018-09-30").withWIP(1),
                    row("Alpha Test").withStatus("Review").at("2018-09-30").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-09-30").withWIP(4),
                    row("Backend Development").withStatus("Review").at("2018-09-30").withWIP(0),
                    row("Alpha Test").withStatus("Doing").at("2018-10-01").withWIP(1),
                    row("Alpha Test").withStatus("Review").at("2018-10-01").withWIP(1),
                    row("Backend Development").withStatus("Doing").at("2018-10-01").withWIP(4),
                    row("Backend Development").withStatus("Review").at("2018-09-01").withWIP(0),
                    row("Alpha Test").withStatus("Doing").at("2018-10-02").withWIP(1),
                    row("Alpha Test").withStatus("Review").at("2018-10-02").withWIP(1),
                    row("Backend Development").withStatus("Doing").at("2018-10-02").withWIP(4),
                    row("Backend Development").withStatus("Review").at("2018-10-02").withWIP(0),
                    row("Alpha Test").withStatus("Doing").at("2018-10-03").withWIP(1),
                    row("Alpha Test").withStatus("Review").at("2018-10-03").withWIP(1),
                    row("Backend Development").withStatus("Doing").at("2018-10-03").withWIP(3),
                    row("Backend Development").withStatus("Review").at("2018-10-04").withWIP(0),
                    row("Alpha Test").withStatus("Doing").at("2018-10-04").withWIP(1),
                    row("Alpha Test").withStatus("Review").at("2018-10-04").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-10-04").withWIP(4),
                    row("Backend Development").withStatus("Review").at("2018-10-04").withWIP(0),
                    row("Alpha Test").withStatus("Doing").at("2018-10-05").withWIP(1),
                    row("Alpha Test").withStatus("Review").at("2018-10-05").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-10-05").withWIP(5),
                    row("Backend Development").withStatus("Review").at("2018-10-05").withWIP(0))
            .configure();

        WipChartDataSet ds = subject.getDataSet(projectKey, issueLevel, ZONE_ID);

        assertThat(ds.rows.size(), is(4));
        assertRow(ds.rows.get(0), "Alpha Test", "Doing", 0.0, "2018-09-23");
        assertRow(ds.rows.get(1), "Alpha Test", "Review", 0.0, "2018-09-23");
        assertRow(ds.rows.get(2), "Backend Development", "Doing", 5.2, "2018-09-23");
        assertRow(ds.rows.get(3), "Backend Development", "Review", 1.0, "2018-09-23");
    }

    @Test
    public void getWipChartDataSet_whenTimelineRangeStartsWithinAndEndsAfterDataSetDateRange_thenFiltersBeginOnly() {
        String projectKey = "TASKB";
        String issueLevel = LEVEL_SUBTASKS;
        
        new TestEnvConfigurator(projectKey)
        .withTimelineReferenceDate("2018-09-25")
        .withTimelineStartDate("2018-09-30")
        .withTimelineDeliveryDate("2018-10-08")
        .withTimelineBaselineDate("2018-09-20")
        .withDemands()
        .withFeatures()
        .withSubtasks(
                row("Alpha Test").withStatus("Doing").at("2018-09-25").withWIP(0),
                row("Alpha Test").withStatus("Review").at("2018-09-25").withWIP(0),
                row("Backend Development").withStatus("Doing").at("2018-09-25").withWIP(6),
                row("Backend Development").withStatus("Review").at("2018-09-25").withWIP(0),
                row("Alpha Test").withStatus("Doing").at("2018-09-26").withWIP(0),
                row("Alpha Test").withStatus("Review").at("2018-09-26").withWIP(0),
                row("Backend Development").withStatus("Doing").at("2018-09-26").withWIP(8),
                row("Backend Development").withStatus("Review").at("2018-09-26").withWIP(0),
                row("Alpha Test").withStatus("Doing").at("2018-09-27").withWIP(0),
                row("Alpha Test").withStatus("Review").at("2018-09-27").withWIP(0),
                row("Backend Development").withStatus("Doing").at("2018-09-27").withWIP(4),
                row("Backend Development").withStatus("Review").at("2018-09-27").withWIP(2),
                row("Alpha Test").withStatus("Doing").at("2018-09-28").withWIP(0),
                row("Alpha Test").withStatus("Review").at("2018-09-28").withWIP(0),
                row("Backend Development").withStatus("Doing").at("2018-09-28").withWIP(4),
                row("Backend Development").withStatus("Review").at("2018-09-28").withWIP(2),
                row("Alpha Test").withStatus("Doing").at("2018-09-29").withWIP(0),
                row("Alpha Test").withStatus("Review").at("2018-09-29").withWIP(0),
                row("Backend Development").withStatus("Doing").at("2018-09-29").withWIP(4),
                row("Backend Development").withStatus("Review").at("2018-09-29").withWIP(1),
                row("Alpha Test").withStatus("Doing").at("2018-09-30").withWIP(1),
                row("Alpha Test").withStatus("Review").at("2018-09-30").withWIP(0),
                row("Backend Development").withStatus("Doing").at("2018-09-30").withWIP(4),
                row("Backend Development").withStatus("Review").at("2018-09-30").withWIP(0),
                row("Alpha Test").withStatus("Doing").at("2018-10-01").withWIP(1),
                row("Alpha Test").withStatus("Review").at("2018-10-01").withWIP(1),
                row("Backend Development").withStatus("Doing").at("2018-10-01").withWIP(4),
                row("Backend Development").withStatus("Review").at("2018-09-01").withWIP(0),
                row("Alpha Test").withStatus("Doing").at("2018-10-02").withWIP(1),
                row("Alpha Test").withStatus("Review").at("2018-10-02").withWIP(1),
                row("Backend Development").withStatus("Doing").at("2018-10-02").withWIP(4),
                row("Backend Development").withStatus("Review").at("2018-10-02").withWIP(0),
                row("Alpha Test").withStatus("Doing").at("2018-10-03").withWIP(1),
                row("Alpha Test").withStatus("Review").at("2018-10-03").withWIP(1),
                row("Backend Development").withStatus("Doing").at("2018-10-03").withWIP(3),
                row("Backend Development").withStatus("Review").at("2018-10-04").withWIP(0),
                row("Alpha Test").withStatus("Doing").at("2018-10-04").withWIP(1),
                row("Alpha Test").withStatus("Review").at("2018-10-04").withWIP(0),
                row("Backend Development").withStatus("Doing").at("2018-10-04").withWIP(4),
                row("Backend Development").withStatus("Review").at("2018-10-04").withWIP(0),
                row("Alpha Test").withStatus("Doing").at("2018-10-05").withWIP(1),
                row("Alpha Test").withStatus("Review").at("2018-10-05").withWIP(0),
                row("Backend Development").withStatus("Doing").at("2018-10-05").withWIP(5),
                row("Backend Development").withStatus("Review").at("2018-10-05").withWIP(0))
        .configure();

        WipChartDataSet ds = subject.getDataSet(projectKey, issueLevel, ZONE_ID);

        assertThat(ds.rows.size(), is(4));
        assertRow(ds.rows.get(0), "Alpha Test", "Doing", 1.0, "2018-09-30");
        assertRow(ds.rows.get(1), "Alpha Test", "Review", 0.5, "2018-09-30");
        assertRow(ds.rows.get(2), "Backend Development", "Doing", 4.0, "2018-09-30");
        assertRow(ds.rows.get(3), "Backend Development", "Review", 0.0, "2018-09-30");
    }

    @Test
    public void getWipChartDataSet_whenTimelineRangeContainsDataSetDateRange_thenReturnsWholeDataSet() {
        String projectKey = "TASKB";
        String issueLevel = LEVEL_SUBTASKS;
        
        new TestEnvConfigurator(projectKey)
            .withTimelineReferenceDate("2018-09-25")
            .withTimelineStartDate("2018-09-23")
            .withTimelineDeliveryDate("2018-10-06")
            .withTimelineBaselineDate("2018-09-25")
            .withDemands()
            .withFeatures()
            .withSubtasks(
                    row("Alpha Test").withStatus("Doing").at("2018-09-25").withWIP(0),
                    row("Alpha Test").withStatus("Review").at("2018-09-25").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-09-25").withWIP(6),
                    row("Backend Development").withStatus("Review").at("2018-09-25").withWIP(0),
                    row("Alpha Test").withStatus("Doing").at("2018-09-26").withWIP(0),
                    row("Alpha Test").withStatus("Review").at("2018-09-26").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-09-26").withWIP(8),
                    row("Backend Development").withStatus("Review").at("2018-09-26").withWIP(0),
                    row("Alpha Test").withStatus("Doing").at("2018-09-27").withWIP(0),
                    row("Alpha Test").withStatus("Review").at("2018-09-27").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-09-27").withWIP(4),
                    row("Backend Development").withStatus("Review").at("2018-09-27").withWIP(2),
                    row("Alpha Test").withStatus("Doing").at("2018-09-28").withWIP(0),
                    row("Alpha Test").withStatus("Review").at("2018-09-28").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-09-28").withWIP(4),
                    row("Backend Development").withStatus("Review").at("2018-09-28").withWIP(2),
                    row("Alpha Test").withStatus("Doing").at("2018-09-29").withWIP(0),
                    row("Alpha Test").withStatus("Review").at("2018-09-29").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-09-29").withWIP(4),
                    row("Backend Development").withStatus("Review").at("2018-09-29").withWIP(1),
                    row("Alpha Test").withStatus("Doing").at("2018-09-30").withWIP(1),
                    row("Alpha Test").withStatus("Review").at("2018-09-30").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-09-30").withWIP(4),
                    row("Backend Development").withStatus("Review").at("2018-09-30").withWIP(0),
                    row("Alpha Test").withStatus("Doing").at("2018-10-01").withWIP(1),
                    row("Alpha Test").withStatus("Review").at("2018-10-01").withWIP(1),
                    row("Backend Development").withStatus("Doing").at("2018-10-01").withWIP(4),
                    row("Backend Development").withStatus("Review").at("2018-09-01").withWIP(0),
                    row("Alpha Test").withStatus("Doing").at("2018-10-02").withWIP(1),
                    row("Alpha Test").withStatus("Review").at("2018-10-02").withWIP(1),
                    row("Backend Development").withStatus("Doing").at("2018-10-02").withWIP(4),
                    row("Backend Development").withStatus("Review").at("2018-10-02").withWIP(0),
                    row("Alpha Test").withStatus("Doing").at("2018-10-03").withWIP(1),
                    row("Alpha Test").withStatus("Review").at("2018-10-03").withWIP(1),
                    row("Backend Development").withStatus("Doing").at("2018-10-03").withWIP(3),
                    row("Backend Development").withStatus("Review").at("2018-10-04").withWIP(0),
                    row("Alpha Test").withStatus("Doing").at("2018-10-04").withWIP(1),
                    row("Alpha Test").withStatus("Review").at("2018-10-04").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-10-04").withWIP(4),
                    row("Backend Development").withStatus("Review").at("2018-10-04").withWIP(0),
                    row("Alpha Test").withStatus("Doing").at("2018-10-05").withWIP(1),
                    row("Alpha Test").withStatus("Review").at("2018-10-05").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-10-05").withWIP(5),
                    row("Backend Development").withStatus("Review").at("2018-10-05").withWIP(0))
            .configure();

        WipChartDataSet ds = subject.getDataSet(projectKey, issueLevel, ZONE_ID);

        assertThat(ds.rows.size(), is(8));
        assertRow(ds.rows.get(0), "Alpha Test", "Doing", 0.0, "2018-09-23");
        assertRow(ds.rows.get(1), "Alpha Test", "Review", 0.0, "2018-09-23");
        assertRow(ds.rows.get(2), "Backend Development", "Doing", 5.2, "2018-09-23");
        assertRow(ds.rows.get(3), "Backend Development", "Review", 1.0, "2018-09-23");
        assertRow(ds.rows.get(4), "Alpha Test", "Doing", 1.0, "2018-09-30");
        assertRow(ds.rows.get(5), "Alpha Test", "Review", 0.5, "2018-09-30");
        assertRow(ds.rows.get(6), "Backend Development", "Doing", 4.0, "2018-09-30");
        assertRow(ds.rows.get(7), "Backend Development", "Review", 0.0, "2018-09-30");
    }

    @Test
    public void getWipChartDataSet_whenTimelineRangeWithinDataSetRange_thenReturnsFilteredDataSet() {
        String projectKey = "TASKB";
        String issueLevel = LEVEL_SUBTASKS;
        
        new TestEnvConfigurator(projectKey)
            .withTimelineReferenceDate("2018-09-25")
            .withTimelineStartDate("2018-09-30")
            .withTimelineDeliveryDate("2018-10-06")
            .withTimelineBaselineDate("2018-09-30")
            .withDemands()
            .withFeatures()
            .withSubtasks(
                    row("Alpha Test").withStatus("Doing").at("2018-09-25").withWIP(0),
                    row("Alpha Test").withStatus("Review").at("2018-09-25").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-09-25").withWIP(6),
                    row("Backend Development").withStatus("Review").at("2018-09-25").withWIP(0),
                    row("Alpha Test").withStatus("Doing").at("2018-09-26").withWIP(0),
                    row("Alpha Test").withStatus("Review").at("2018-09-26").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-09-26").withWIP(8),
                    row("Backend Development").withStatus("Review").at("2018-09-26").withWIP(0),
                    row("Alpha Test").withStatus("Doing").at("2018-09-27").withWIP(0),
                    row("Alpha Test").withStatus("Review").at("2018-09-27").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-09-27").withWIP(4),
                    row("Backend Development").withStatus("Review").at("2018-09-27").withWIP(2),
                    row("Alpha Test").withStatus("Doing").at("2018-09-28").withWIP(0),
                    row("Alpha Test").withStatus("Review").at("2018-09-28").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-09-28").withWIP(4),
                    row("Backend Development").withStatus("Review").at("2018-09-28").withWIP(2),
                    row("Alpha Test").withStatus("Doing").at("2018-09-29").withWIP(0),
                    row("Alpha Test").withStatus("Review").at("2018-09-29").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-09-29").withWIP(4),
                    row("Backend Development").withStatus("Review").at("2018-09-29").withWIP(1),
                    row("Alpha Test").withStatus("Doing").at("2018-09-30").withWIP(1),
                    row("Alpha Test").withStatus("Review").at("2018-09-30").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-09-30").withWIP(4),
                    row("Backend Development").withStatus("Review").at("2018-09-30").withWIP(0),
                    row("Alpha Test").withStatus("Doing").at("2018-10-01").withWIP(1),
                    row("Alpha Test").withStatus("Review").at("2018-10-01").withWIP(1),
                    row("Backend Development").withStatus("Doing").at("2018-10-01").withWIP(4),
                    row("Backend Development").withStatus("Review").at("2018-09-01").withWIP(0),
                    row("Alpha Test").withStatus("Doing").at("2018-10-02").withWIP(1),
                    row("Alpha Test").withStatus("Review").at("2018-10-02").withWIP(1),
                    row("Backend Development").withStatus("Doing").at("2018-10-02").withWIP(4),
                    row("Backend Development").withStatus("Review").at("2018-10-02").withWIP(0),
                    row("Alpha Test").withStatus("Doing").at("2018-10-03").withWIP(1),
                    row("Alpha Test").withStatus("Review").at("2018-10-03").withWIP(1),
                    row("Backend Development").withStatus("Doing").at("2018-10-03").withWIP(3),
                    row("Backend Development").withStatus("Review").at("2018-10-04").withWIP(0),
                    row("Alpha Test").withStatus("Doing").at("2018-10-04").withWIP(1),
                    row("Alpha Test").withStatus("Review").at("2018-10-04").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-10-04").withWIP(4),
                    row("Backend Development").withStatus("Review").at("2018-10-04").withWIP(0),
                    row("Alpha Test").withStatus("Doing").at("2018-10-05").withWIP(1),
                    row("Alpha Test").withStatus("Review").at("2018-10-05").withWIP(0),
                    row("Backend Development").withStatus("Doing").at("2018-10-05").withWIP(5),
                    row("Backend Development").withStatus("Review").at("2018-10-05").withWIP(0))
            .configure();

        WipChartDataSet ds = subject.getDataSet(projectKey, issueLevel, ZONE_ID);

        assertThat(ds.rows.size(), is(4));
        assertRow(ds.rows.get(0), "Alpha Test", "Doing", 1.0, "2018-09-30");
        assertRow(ds.rows.get(1), "Alpha Test", "Review", 0.5, "2018-09-30");
        assertRow(ds.rows.get(2), "Backend Development", "Doing", 4.0, "2018-09-30");
        assertRow(ds.rows.get(3), "Backend Development", "Review", 0.0, "2018-09-30");
    }

    @Test
    public void getWipChartDataSet_whenTimelineDatesNotSet_thenNotFiltering() {
        String projectKey = "TASKB";
        String issueLevel = LEVEL_DEMANDS;
        
        new TestEnvConfigurator(projectKey)
            .withTimelineReferenceDate("2018-09-25")
            .withDemands(
                    row("Demand").withStatus("Development").at("2018-09-25").withWIP(1),
                    row("Demand").withStatus("Development").at("2018-09-26").withWIP(1),
                    row("Demand").withStatus("Development").at("2018-09-27").withWIP(1),
                    row("Demand").withStatus("Development").at("2018-09-28").withWIP(1),
                    row("Demand").withStatus("Development").at("2018-09-29").withWIP(1),
                    row("Demand").withStatus("Development").at("2018-09-30").withWIP(1),
                    row("Demand").withStatus("Development").at("2018-10-01").withWIP(1),
                    row("Demand").withStatus("Development").at("2018-10-02").withWIP(1),
                    row("Demand").withStatus("Development").at("2018-10-03").withWIP(2),
                    row("Demand").withStatus("Development").at("2018-10-04").withWIP(2),
                    row("Demand").withStatus("Development").at("2018-10-05").withWIP(2))
            .withFeatures()
            .withSubtasks()
            .configure();

        WipChartDataSet ds = subject.getDataSet(projectKey, issueLevel, ZONE_ID);

        assertThat(ds.rows.size(), is(2));
        assertRow(ds.rows.get(0), "Demand", "Development", 1.0, "2018-09-23");
        assertRow(ds.rows.get(1), "Demand", "Development", 1.5, "2018-09-30");
    }

    @Test
    public void getWipChartDataSet_whenDataSetWithoutSubtasksAndRequestingFeatures_thenReturnsSuccessfully() {
        String projectKey = "TASKB";
        String issueLevel = LEVEL_FEATURES;
        
        new TestEnvConfigurator(projectKey)
            .withTimelineReferenceDate("2018-09-25")
            .withTimelineStartDate("2018-09-25")
            .withTimelineDeliveryDate("2018-10-05")
            .withTimelineBaselineDate("2018-09-25")
            .withDemands(
                    row("Demand").withStatus("Development").at("2018-09-25").withWIP(1),
                    row("Demand").withStatus("Development").at("2018-09-26").withWIP(1),
                    row("Demand").withStatus("Development").at("2018-09-27").withWIP(1),
                    row("Demand").withStatus("Development").at("2018-09-28").withWIP(1),
                    row("Demand").withStatus("Development").at("2018-09-29").withWIP(1),
                    row("Demand").withStatus("Development").at("2018-09-30").withWIP(1),
                    row("Demand").withStatus("Development").at("2018-10-01").withWIP(1),
                    row("Demand").withStatus("Development").at("2018-10-02").withWIP(1),
                    row("Demand").withStatus("Development").at("2018-10-03").withWIP(2),
                    row("Demand").withStatus("Development").at("2018-10-04").withWIP(2),
                    row("Demand").withStatus("Development").at("2018-10-05").withWIP(2))
            .withFeatures(
                    row("Bug").withStatus("Development").at("2018-09-25").withWIP(0),
                    row("Task").withStatus("Development").at("2018-09-25").withWIP(2),
                    row("Bug").withStatus("Development").at("2018-09-26").withWIP(1),
                    row("Task").withStatus("Development").at("2018-09-26").withWIP(4),
                    row("Bug").withStatus("Development").at("2018-09-27").withWIP(0),
                    row("Task").withStatus("Development").at("2018-09-27").withWIP(1),
                    row("Bug").withStatus("Development").at("2018-09-28").withWIP(0),
                    row("Task").withStatus("Development").at("2018-09-28").withWIP(1),
                    row("Bug").withStatus("Development").at("2018-09-29").withWIP(0),
                    row("Task").withStatus("Development").at("2018-09-29").withWIP(1),
                    row("Bug").withStatus("Development").at("2018-09-30").withWIP(0),
                    row("Task").withStatus("Development").at("2018-09-30").withWIP(1),
                    row("Bug").withStatus("Development").at("2018-10-01").withWIP(0),
                    row("Task").withStatus("Development").at("2018-10-01").withWIP(1),
                    row("Bug").withStatus("Development").at("2018-10-02").withWIP(0),
                    row("Task").withStatus("Development").at("2018-10-02").withWIP(1),
                    row("Bug").withStatus("Development").at("2018-10-03").withWIP(0),
                    row("Task").withStatus("Development").at("2018-10-03").withWIP(1),
                    row("Bug").withStatus("Development").at("2018-10-04").withWIP(0),
                    row("Task").withStatus("Development").at("2018-10-04").withWIP(1),
                    row("Bug").withStatus("Development").at("2018-10-05").withWIP(0),
                    row("Task").withStatus("Development").at("2018-10-05").withWIP(1))
            .withSubtasks()
            .configure();

        final WipChartDataSet ds = subject.getDataSet(projectKey, issueLevel, ZONE_ID);

        assertThat(ds.rows.size(), is(4));
        assertRow(ds.rows.get(0), "Bug", "Development", 0.2, "2018-09-23");
        assertRow(ds.rows.get(1), "Task", "Development", 1.8, "2018-09-23");
        assertRow(ds.rows.get(2), "Bug", "Development", 0.0, "2018-09-30");
        assertRow(ds.rows.get(3), "Task", "Development", 1.0, "2018-09-30");
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
                    row("Demand").withStatus("Development").at("2018-09-25").withWIP(1),
                    row("Demand").withStatus("Development").at("2018-09-26").withWIP(1),
                    row("Demand").withStatus("Development").at("2018-09-27").withWIP(1),
                    row("Demand").withStatus("Development").at("2018-09-28").withWIP(1),
                    row("Demand").withStatus("Development").at("2018-09-29").withWIP(1),
                    row("Demand").withStatus("Development").at("2018-09-30").withWIP(1),
                    row("Demand").withStatus("Development").at("2018-10-01").withWIP(1),
                    row("Demand").withStatus("Development").at("2018-10-02").withWIP(1),
                    row("Demand").withStatus("Development").at("2018-10-03").withWIP(2),
                    row("Demand").withStatus("Development").at("2018-10-04").withWIP(2),
                    row("Demand").withStatus("Development").at("2018-10-05").withWIP(2))
            .configure();

        final WipChartDataSet ds = subject.getDataSet(projectKey, issueLevel, ZONE_ID);

        assertThat(ds.rows.size(), is(0));
    }
    @Test
    public void getWipChartDataSet_whenTimelineStartDateWithinAndEndDateNotSet_thenFiltersBeginOnly() {
        String projectKey = "TASKB";
        String issueLevel = LEVEL_FEATURES;
        
        new TestEnvConfigurator(projectKey)
            .withTimelineReferenceDate("2018-09-25")
            .withTimelineStartDate("2018-09-30")
            .withTimelineBaselineDate("2018-09-25")
            .withFeatures(
                    row("Bug").withStatus("Development").at("2018-09-25").withWIP(0),
                    row("Task").withStatus("Development").at("2018-09-25").withWIP(2),
                    row("Bug").withStatus("Development").at("2018-09-26").withWIP(1),
                    row("Task").withStatus("Development").at("2018-09-26").withWIP(4),
                    row("Bug").withStatus("Development").at("2018-09-27").withWIP(0),
                    row("Task").withStatus("Development").at("2018-09-27").withWIP(1),
                    row("Bug").withStatus("Development").at("2018-09-28").withWIP(0),
                    row("Task").withStatus("Development").at("2018-09-28").withWIP(1),
                    row("Bug").withStatus("Development").at("2018-09-29").withWIP(0),
                    row("Task").withStatus("Development").at("2018-09-29").withWIP(1),
                    row("Bug").withStatus("Development").at("2018-09-30").withWIP(0),
                    row("Task").withStatus("Development").at("2018-09-30").withWIP(1),
                    row("Bug").withStatus("Development").at("2018-10-01").withWIP(0),
                    row("Task").withStatus("Development").at("2018-10-01").withWIP(1),
                    row("Bug").withStatus("Development").at("2018-10-02").withWIP(0),
                    row("Task").withStatus("Development").at("2018-10-02").withWIP(1),
                    row("Bug").withStatus("Development").at("2018-10-03").withWIP(0),
                    row("Task").withStatus("Development").at("2018-10-03").withWIP(1),
                    row("Bug").withStatus("Development").at("2018-10-04").withWIP(0),
                    row("Task").withStatus("Development").at("2018-10-04").withWIP(1),
                    row("Bug").withStatus("Development").at("2018-10-05").withWIP(0),
                    row("Task").withStatus("Development").at("2018-10-05").withWIP(1))
            .configure();

        final WipChartDataSet ds = subject.getDataSet(projectKey, issueLevel, ZONE_ID);

        assertThat(ds.rows.size(), is(2));
        assertRow(ds.rows.get(0), "Bug", "Development", 0.0, "2018-09-30");
        assertRow(ds.rows.get(1), "Task", "Development", 1.0, "2018-09-30");
    }

    @Test
    public void getWipChartDataSet_whenTimelineDatesNotSetDatasetEmpty_thenReturnsEmptyDataSet() {
        String projectKey = "TASKB";
        String issueLevel = LEVEL_ALL;

        new TestEnvConfigurator(projectKey)
            .withTimelineReferenceDate("2018-09-25")
            .configure();

        final WipChartDataSet ds = subject.getDataSet(projectKey, issueLevel, ZONE_ID);

        assertThat(ds.rows.size(), is(0));
    }
    
    private void assertRow(WipDataPoint actual, String expectedIssueType, String expectedIssueStatus, Double expectedWIPAverage, String date) {
        assertThat(actual.issueType, is(expectedIssueType));
        assertThat(actual.issueStatus, is(expectedIssueStatus));
        final ZonedDateTime expectedDate = parseDateTime(date);
        assertThat(DayOfWeek.SUNDAY, is(expectedDate.getDayOfWeek()));
        assertThat(actual.date, is(Date.from(expectedDate.toInstant())));
        assertThat(actual.average, is(expectedWIPAverage));
    }
    
    private WipRowBuilder row(String issueType) {
        WipRowBuilder rowBuilder = new WipRowBuilder(issueType);
        return rowBuilder;
    }

    private class TestEnvConfigurator {
        private String projectKey;
        private Map<String, WipDataSetBuilder> dsBuilders;
        private Optional<LocalDate> startDate = Optional.empty();
        private Optional<LocalDate> deliveryDate = Optional.empty();
        private Optional<LocalDate> baselineDate = Optional.empty();
        private LocalDate referenceDate;

        private TestEnvConfigurator(String projectKey) {
            this.projectKey = projectKey;
            dsBuilders = new HashMap<>();
            dsBuilders.put(LEVEL_DEMANDS, new WipDataSetBuilder(LEVEL_DEMANDS));
            dsBuilders.put(LEVEL_FEATURES, new WipDataSetBuilder(LEVEL_FEATURES));
            dsBuilders.put(LEVEL_SUBTASKS, new WipDataSetBuilder(LEVEL_SUBTASKS));
        }
        
        private TestEnvConfigurator withDemands(WipRowBuilder ... rowBuilders) {
            WipDataSetBuilder demandsBuilder = dsBuilders.get(LEVEL_DEMANDS);
            demandsBuilder.addRowBuilders(Arrays.asList(rowBuilders));
            return this;
        }
        
        private TestEnvConfigurator withFeatures(WipRowBuilder ... rowBuilders) {
            WipDataSetBuilder demandsBuilder = dsBuilders.get(LEVEL_FEATURES);
            demandsBuilder.addRowBuilders(Arrays.asList(rowBuilders));
            return this;
        }
        
        private TestEnvConfigurator withSubtasks(WipRowBuilder ... rowBuilders) {
            WipDataSetBuilder demandsBuilder = dsBuilders.get(LEVEL_SUBTASKS);
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
            if (referenceDate == null)
                throw new RuntimeException("Timeline reference date must be set!");
            
            List<WipDataSet> dataSets = Arrays.asList(
                    dsBuilders.get(LEVEL_DEMANDS).build(), 
                    dsBuilders.get(LEVEL_FEATURES).build(), 
                    dsBuilders.get(LEVEL_SUBTASKS).build());
            when(dataRepository.getFirstDate(projectKey)).thenReturn(startDate);
            mockWipKpiService(dataSets);
        }
        
        private void mockWipKpiService(List<WipDataSet> dataSets) {
            ProjectFilterConfiguration projectFilter = createProjectFilter();
            final FollowUpSnapshot snapshot = mockFollowUpSnapshot(projectFilter);
            when(wipKpiService.getData(snapshot.getData())).thenReturn(dataSets);
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
    
    private class WipDataSetBuilder {
        private String issueLevel;
        private List<WipRowBuilder> rowBuilders = new LinkedList<>();
        
        private WipDataSetBuilder(String issueLevel) {
            this.issueLevel = issueLevel;
        }
        
        private void addRowBuilders(List<WipRowBuilder> rowBuilders) {
            this.rowBuilders = rowBuilders;
        }
        
        private WipDataSet build() {
            List<WipRow> rows = rowBuilders.stream()
                    .map(b -> b.build()).collect(Collectors.toList());
            WipDataSet dataset = new WipDataSet(issueLevel, rows);
            return dataset;
        }
    }
    
    private class WipRowBuilder {
        private ZonedDateTime date;
        private String issueType;
        private String issueStatus;
        private Long wip;
        
        private WipRowBuilder(String issueType) {
            this.issueType = issueType;
        }

        private WipRowBuilder withStatus(String issueStatus) {
            this.issueStatus = issueStatus;
            return this;
        }

        private WipRowBuilder at(String date) {
            this.date = parseDateTime(date);
            return this;
        }
        
        private WipRowBuilder withWIP(long wip) {
            this.wip = wip;
            return this;
        }
        
        private WipRow build() {
            if (date == null)
                throw new RuntimeException("Date required!");
            if (issueType == null)
                throw new RuntimeException("Issue Type required!");
            if (issueStatus == null)
                throw new RuntimeException("Issue Status required!");
            if (wip == null)
                throw new RuntimeException("WIP required!");
            
            return new WipRow(date, issueType, issueStatus, wip);
        }
    }

}
