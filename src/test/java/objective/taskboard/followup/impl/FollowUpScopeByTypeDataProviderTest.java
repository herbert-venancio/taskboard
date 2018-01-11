package objective.taskboard.followup.impl;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static objective.taskboard.Constants.FROMJIRA_HEADERS;
import static objective.taskboard.followup.impl.FollowUpScopeByTypeDataProvider.BASELINE_BACKLOG;
import static objective.taskboard.followup.impl.FollowUpScopeByTypeDataProvider.BASELINE_DONE;
import static objective.taskboard.followup.impl.FollowUpScopeByTypeDataProvider.INTANGIBLE;
import static objective.taskboard.followup.impl.FollowUpScopeByTypeDataProvider.NEW_SCOPE;
import static objective.taskboard.followup.impl.FollowUpScopeByTypeDataProvider.REWORK;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.followup.FollowUpDataSnapshot;
import objective.taskboard.followup.FollowUpFacade;
import objective.taskboard.followup.FollowUpScopeByTypeDataItem;
import objective.taskboard.followup.FollowUpScopeByTypeDataSet;
import objective.taskboard.followup.FollowupCluster;
import objective.taskboard.followup.FollowupClusterProvider;
import objective.taskboard.followup.FollowupData;
import objective.taskboard.followup.FollowupDataProvider;
import objective.taskboard.followup.FromJiraDataRow;
import objective.taskboard.followup.FromJiraDataSet;
import objective.taskboard.followup.FromJiraRowService;

@RunWith(MockitoJUnitRunner.class)
public class FollowUpScopeByTypeDataProviderTest {

    @Mock
    private FollowUpFacade followUpFacade;

    @Mock
    private FollowupDataProvider dataProvider;

    @Mock
    private FollowupClusterProvider clusterProvider;

    @Mock
    private FromJiraRowService rowService;

    @InjectMocks
    private FollowUpScopeByTypeDataProvider subject;

    private static final Double EFFORT_ESTIMATE = 1D;

    private static final String PROJECT_KEY = "PROJECT_TEST";
    private static final String DATE = "20180101";
    private static final ZoneId ZONE_ID = ZoneId.of("America/Sao_Paulo");

    private final FromJiraDataRow row = new FromJiraDataRow();

    private FollowUpScopeByTypeDataSet data;

    @Before
    public void setup() {
        row.taskBallpark = EFFORT_ESTIMATE;

        when(clusterProvider.getForProject(any())).thenReturn(Optional.of(mock(FollowupCluster.class)));
        when(followUpFacade.getProvider(any())).thenReturn(dataProvider);
        when(dataProvider.getJiraData(any(), any(), any())).thenReturn(getSnapshot(LocalDate.of(2018, 1, 1), asList(row)));
    }

    @Test
    public void ifIsBaselineBacklog_thenSumBaselineBacklog() {
        when(rowService.isBaselineBacklog(row)).thenReturn(true);

        data = subject.getScopeByTypeData(PROJECT_KEY, DATE, ZONE_ID);

        Double expectedOthers = 0D;
        assertEffortEstimateByType(INTANGIBLE, expectedOthers);
        assertEffortEstimateByType(NEW_SCOPE, expectedOthers);
        assertEffortEstimateByType(REWORK, expectedOthers);
        assertEffortEstimateByType(BASELINE_DONE, expectedOthers);
        assertEffortEstimateByType(BASELINE_BACKLOG, EFFORT_ESTIMATE);
    }

    @Test
    public void ifIsBaselineDone_thenSumBaselineDone() {
        when(rowService.isBaselineDone(row)).thenReturn(true);

        data = subject.getScopeByTypeData(PROJECT_KEY, DATE, ZONE_ID);

        Double expectedOthers = 0D;
        assertEffortEstimateByType(INTANGIBLE, expectedOthers);
        assertEffortEstimateByType(NEW_SCOPE, expectedOthers);
        assertEffortEstimateByType(REWORK, expectedOthers);
        assertEffortEstimateByType(BASELINE_DONE, EFFORT_ESTIMATE);
        assertEffortEstimateByType(BASELINE_BACKLOG, expectedOthers);
    }

    @Test
    public void ifIsRework_thenSumRework() {
        when(rowService.isRework(row)).thenReturn(true);

        data = subject.getScopeByTypeData(PROJECT_KEY, DATE, ZONE_ID);

        Double expectedOthers = 0D;
        assertEffortEstimateByType(INTANGIBLE, expectedOthers);
        assertEffortEstimateByType(NEW_SCOPE, expectedOthers);
        assertEffortEstimateByType(REWORK, EFFORT_ESTIMATE);
        assertEffortEstimateByType(BASELINE_DONE, expectedOthers);
        assertEffortEstimateByType(BASELINE_BACKLOG, expectedOthers);
    }

    @Test
    public void ifIsNewScope_thenSumNewScope() {
        when(rowService.isNewScope(row)).thenReturn(true);

        data = subject.getScopeByTypeData(PROJECT_KEY, DATE, ZONE_ID);

        Double expectedOthers = 0D;
        assertEffortEstimateByType(INTANGIBLE, expectedOthers);
        assertEffortEstimateByType(NEW_SCOPE, EFFORT_ESTIMATE);
        assertEffortEstimateByType(REWORK, expectedOthers);
        assertEffortEstimateByType(BASELINE_DONE, expectedOthers);
        assertEffortEstimateByType(BASELINE_BACKLOG, expectedOthers);
    }

    @Test
    public void ifIsIntangible_thenSumIntangible() {
        when(rowService.isIntangible(row)).thenReturn(true);

        data = subject.getScopeByTypeData(PROJECT_KEY, DATE, ZONE_ID);

        Double expectedOthers = 0D;
        assertEffortEstimateByType(INTANGIBLE, EFFORT_ESTIMATE);
        assertEffortEstimateByType(NEW_SCOPE, expectedOthers);
        assertEffortEstimateByType(REWORK, expectedOthers);
        assertEffortEstimateByType(BASELINE_DONE, expectedOthers);
        assertEffortEstimateByType(BASELINE_BACKLOG, expectedOthers);
    }

    @Test
    public void ifMultipleRows_thenSumAllRows() {
        List<FromJiraDataRow> rows = asList(row, row, row);
        when(dataProvider.getJiraData(any(), any(), any())).thenReturn(getSnapshot(LocalDate.of(2018, 1, 1), rows));
        when(rowService.isBaselineBacklog(row)).thenReturn(true);

        data = subject.getScopeByTypeData(PROJECT_KEY, DATE, ZONE_ID);

        Double expectedOthers = 0D;
        Double expectedTwoRows = EFFORT_ESTIMATE * rows.size();
        assertEffortEstimateByType(INTANGIBLE, expectedOthers);
        assertEffortEstimateByType(NEW_SCOPE, expectedOthers);
        assertEffortEstimateByType(REWORK, expectedOthers);
        assertEffortEstimateByType(BASELINE_DONE, expectedOthers);
        assertEffortEstimateByType(BASELINE_BACKLOG, expectedTwoRows);
    }

    @Test
    public void ifHasValues_thenTotalIsTheSumOfAllEffortEstimateValues() {
        List<FromJiraDataRow> rows = asList(row, row, row);
        when(dataProvider.getJiraData(any(), any(), any())).thenReturn(getSnapshot(LocalDate.of(2018, 1, 1), rows));
        when(rowService.isBaselineBacklog(row)).thenReturn(true);

        data = subject.getScopeByTypeData(PROJECT_KEY, DATE, ZONE_ID);

        Double total = data.values.stream().mapToDouble(i -> i.effortEstimate).sum();
        assertEquals(total, data.total);
    }

    @Test
    public void ifHasValues_thenFillAllInformationFields() {
        data = subject.getScopeByTypeData(PROJECT_KEY, DATE, ZONE_ID);
        assertEquals(PROJECT_KEY, data.projectKey);
        assertEquals(DATE, data.date);
        assertEquals(ZONE_ID.getId(), data.zoneId);
    }

    private FollowUpDataSnapshot getSnapshot(LocalDate data, List<FromJiraDataRow> rows) {
        FromJiraDataSet dataSet = new FromJiraDataSet(FROMJIRA_HEADERS, rows);
        FollowupData followupData = new FollowupData(dataSet, emptyList(), emptyList());
        return new FollowUpDataSnapshot(data, followupData);
    }

    private void assertEffortEstimateByType(String type, Double exceptedValue) {
        FollowUpScopeByTypeDataItem item = data.values.stream().filter(v -> type.equals(v.type)).findFirst().orElse(null);
        assertEquals(exceptedValue, item.effortEstimate);
    }
}
