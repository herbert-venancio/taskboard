package objective.taskboard.followup;

import static java.time.LocalDate.parse;
import static objective.taskboard.followup.FixedFollowUpSnapshotValuesProvider.emptyValuesProvider;
import static objective.taskboard.utils.DateTimeUtils.parseDateTime;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

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
import objective.taskboard.followup.kpi.WipChartDataSet;
import objective.taskboard.followup.kpi.WipDataPoint;
import objective.taskboard.followup.kpi.WipKPIService;

@RunWith(MockitoJUnitRunner.class)
public class WipKPIDataProviderTest {

    public static final String TYPE_DEMAND = "Demand";
    public static final String TYPE_FEATURES = "Features";
    public static final String TYPE_SUBTASKS = "Subtasks";
    private static final ZoneId ZONE_ID = ZoneId.systemDefault();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private FollowUpDataRepository dataRepository;

    @Mock
    private FollowUpSnapshotService snapshotService;

    @Mock
    private WipKPIService wipKpiService;

    @Spy
    @InjectMocks
    private WipKPIDataProvider subject = new WipKPIDataProvider();

    @Test
    public void getWipChartDataSet_happyDay() {
        String projectKey = "TASKB";
        String issueLevel = "Subtask";
        
        new TestEnvConfigurator(projectKey)
                .withTimelineWithinRange()
                .withDemands().withFeatures().withSubtasks()
                .configure();

        WipChartDataSet dsSubtasks = subject.getDataSet(projectKey, issueLevel, ZONE_ID);

        assertThat(dsSubtasks.rows.size(), is(4));
        assertRow(dsSubtasks.rows.get(0), "Dev", "Doing", 0L, "2017-09-25");
        assertRow(dsSubtasks.rows.get(1), "Dev", "Doing", 1L, "2017-09-26");
        assertRow(dsSubtasks.rows.get(2), "Dev", "Doing", 2L, "2017-09-27");
        assertRow(dsSubtasks.rows.get(3), "Dev", "Doing", 1L, "2017-09-28");
    }

    @Test
    public void getWipChartDataSet_timelineShortThanDateRangeFromData() {
        String projectKey = "TASKB";
        String issueLevel = "Subtask";
        
        new TestEnvConfigurator(projectKey)
                .withShorterTimeline()
                .withDemands().withFeatures().withSubtasks()
                .configure();

        WipChartDataSet dsSubtasks = subject.getDataSet(projectKey, issueLevel, ZONE_ID);

        assertThat(dsSubtasks.rows.size(), is(0));
    }

    @Test
    public void getWipChartDataSet_timelineLongerThanDataRangeFromData() {
        String projectKey = "TASKB";
        String issueLevel = "Subtask";
        
        new TestEnvConfigurator(projectKey)
            .withLongerTimeline()
            .withDemands().withFeatures().withSubtasks()
            .configure();

        WipChartDataSet dsSubtasks = subject.getDataSet(projectKey, issueLevel, ZONE_ID);

        assertThat(dsSubtasks.rows.size(), is(0));
    }

    @Test
    public void getWipChartDataSet_fetchDataFromAllLevels() {
        String projectKey = "TASKB";
        String issueLevel = "All";

        new TestEnvConfigurator(projectKey)
            .withTimelineWithinRange()
            .withDemands().withFeatures().withSubtasks()
            .configure();

        WipChartDataSet dsAllLevels = subject.getDataSet(projectKey, issueLevel, ZONE_ID);

        assertThat(dsAllLevels.rows.size(), is(12));
        assertRow(dsAllLevels.rows.get(0), "Demand", "Doing", 0L, "2017-09-25");
        assertRow(dsAllLevels.rows.get(1), "OS", "Doing", 0L, "2017-09-25");
        assertRow(dsAllLevels.rows.get(2), "Demand", "Doing", 1L, "2017-09-26");
        assertRow(dsAllLevels.rows.get(3), "OS", "Doing", 1L, "2017-09-26");
        assertRow(dsAllLevels.rows.get(4), "Demand", "Doing", 0L, "2017-09-27");
        assertRow(dsAllLevels.rows.get(5), "OS", "Doing", 1L, "2017-09-27");
        assertRow(dsAllLevels.rows.get(6), "Feature", "Doing", 0L, "2017-09-25");
        assertRow(dsAllLevels.rows.get(7), "Feature", "Doing", 1L, "2017-09-26");
        assertRow(dsAllLevels.rows.get(8), "Dev", "Doing", 0L, "2017-09-25");
        assertRow(dsAllLevels.rows.get(9), "Dev", "Doing", 1L, "2017-09-26");
        assertRow(dsAllLevels.rows.get(10), "Dev", "Doing", 2L, "2017-09-27");
        assertRow(dsAllLevels.rows.get(11), "Dev", "Doing", 1L, "2017-09-28");
    }

    @Test
    public void getWipChartDataSet_withoutSubtasksDataSet() {
        String projectKey = "TASKB";
        String issueLevel = "All";
        
        new TestEnvConfigurator(projectKey)
            .withDemands().withFeatures()
            .configure();

        final WipChartDataSet dsAllLevels = subject.getDataSet(projectKey, issueLevel, ZONE_ID);

        assertThat(dsAllLevels.rows.size(), is(8));
        assertRow(dsAllLevels.rows.get(0), "Demand", "Doing", 0L, "2017-09-25");
        assertRow(dsAllLevels.rows.get(1), "OS", "Doing", 0L, "2017-09-25");
        assertRow(dsAllLevels.rows.get(2), "Demand", "Doing", 1L, "2017-09-26");
        assertRow(dsAllLevels.rows.get(3), "OS", "Doing", 1L, "2017-09-26");
        assertRow(dsAllLevels.rows.get(4), "Demand", "Doing", 0L, "2017-09-27");
        assertRow(dsAllLevels.rows.get(5), "OS", "Doing", 1L, "2017-09-27");
        assertRow(dsAllLevels.rows.get(6), "Feature", "Doing", 0L, "2017-09-25");
        assertRow(dsAllLevels.rows.get(7), "Feature", "Doing", 1L, "2017-09-26");
    }

    @Test
    public void getWipChartDataSet_timelineDatesEmptyDatasetEmpty() {
        String projectKey = "TASKB";
        String issueLevel = "All";

        new TestEnvConfigurator(projectKey)
                .configure();

        final WipChartDataSet ds = subject.getDataSet(projectKey, issueLevel, ZONE_ID);

        assertThat(ds.rows.size(), is(0));
    }

    private void assertRow(WipDataPoint wipDataPoint, String issueType, String issueStatus, Long wipCount, String date) {
        assertThat(wipDataPoint.type, is(issueType));
        assertThat(wipDataPoint.status, is(issueStatus));
        assertThat(wipDataPoint.date, is(Date.from(parseDateTime(date).toInstant())));
        assertThat(wipDataPoint.count, is(wipCount));
    }

    private class TestEnvConfigurator {
        private String projectKey;
        private WipDataSet demands = new WipDataSet(TYPE_DEMAND, new LinkedList<>());
        private WipDataSet features = new WipDataSet(TYPE_FEATURES, new LinkedList<>());
        private WipDataSet subtasks  = new WipDataSet(TYPE_SUBTASKS, new LinkedList<>());
        private Optional<LocalDate> startDate = Optional.empty();
        private Optional<LocalDate> deliveryDate = Optional.empty();
        private Optional<LocalDate> baselineDate = Optional.empty();

        private TestEnvConfigurator(String projectKey) {
            this.projectKey = projectKey; 
        }

        private TestEnvConfigurator withDemands() {
            demands.rows.add(new WipRow(parseDateTime("2017-09-25"), "Demand", "Doing", 0L));
            demands.rows.add(new WipRow(parseDateTime("2017-09-25"), "OS", "Doing", 0L));
            demands.rows.add(new WipRow(parseDateTime("2017-09-26"), "Demand", "Doing", 1L));
            demands.rows.add(new WipRow(parseDateTime("2017-09-26"), "OS", "Doing", 1L));
            demands.rows.add(new WipRow(parseDateTime("2017-09-27"), "Demand", "Doing", 0L));
            demands.rows.add(new WipRow(parseDateTime("2017-09-27"), "OS", "Doing", 1L));

            return this;
        }

        private TestEnvConfigurator withFeatures() {
            features.rows.add(new WipRow(parseDateTime("2017-09-25"), "Feature", "Doing", 0L));
            features.rows.add(new WipRow(parseDateTime("2017-09-26"), "Feature", "Doing", 1L));

            return this;
        }

        private TestEnvConfigurator withSubtasks() {
            subtasks.rows.add(new WipRow(parseDateTime("2017-09-25"), "Dev", "Doing", 0L));
            subtasks.rows.add(new WipRow(parseDateTime("2017-09-26"), "Dev", "Doing", 1L));
            subtasks.rows.add(new WipRow(parseDateTime("2017-09-27"), "Dev", "Doing", 2L));
            subtasks.rows.add(new WipRow(parseDateTime("2017-09-28"), "Dev", "Doing", 1L));

            return this;
        }
        
        private TestEnvConfigurator withTimelineWithinRange() {
            this.startDate = Optional.of(parse("2017-09-25"));
            this.deliveryDate = Optional.of(parse("2017-09-28"));
            this.baselineDate = Optional.of(parse("2017-09-25"));
            
            return this;
        }
        
        private TestEnvConfigurator withShorterTimeline() {
            this.startDate = Optional.of(parse("2017-09-20"));
            this.deliveryDate = Optional.of(parse("2017-09-23"));
            this.baselineDate = Optional.of(parse("2017-09-20"));
            
            return this;
        }
        
        private TestEnvConfigurator withLongerTimeline() {
            this.startDate = Optional.of(parse("2017-09-29"));
            this.deliveryDate = Optional.of(parse("2017-09-30"));
            this.baselineDate = Optional.of(parse("2017-09-29"));
            
            return this;
        }
        

        private void configure() {
            when(dataRepository.getFirstDate(projectKey)).thenReturn(startDate);
            mockWipKpiService();
        }
        
        private void mockWipKpiService() {
            List<WipDataSet> dataSets = Arrays.asList(demands, features, subtasks);
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
            LocalDate referenceDate = LocalDate.parse("2017-09-25");

            FollowUpTimeline timeline = FollowUpTimeline.build(referenceDate, projectFilter, dataRepository);
            return timeline;
        }
    }

}
