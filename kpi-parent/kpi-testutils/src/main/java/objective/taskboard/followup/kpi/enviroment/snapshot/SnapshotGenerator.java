package objective.taskboard.followup.kpi.enviroment.snapshot;

import static java.util.Collections.emptyList;

import java.util.List;
import java.util.Optional;

import org.mockito.Mockito;

import objective.taskboard.followup.AnalyticsTransitionsDataSet;
import objective.taskboard.followup.FollowUpData;
import objective.taskboard.followup.FollowUpSnapshot;
import objective.taskboard.followup.FromJiraDataSet;
import objective.taskboard.followup.KpiHelper;
import objective.taskboard.followup.SyntheticTransitionsDataSet;
import objective.taskboard.followup.kpi.enviroment.KpiEnvironment;

public class SnapshotGenerator {
    
    private Optional<FromJiraDataSet> fromJiraDs = Optional.empty();
    private AnalyticsDataSetsGenerator analyticGenerator;
    
    public SnapshotGenerator(KpiEnvironment environment) {
        this.analyticGenerator = new AnalyticsDataSetsGenerator(environment);
    }
    
    public SnapshotGenerator withFromJiraDs(FromJiraDataSet fromJiraDs) {
        this.fromJiraDs = Optional.of(fromJiraDs);
        return this;
    }
    
    public FollowUpData buildFollowupData() {
        List<SyntheticTransitionsDataSet> syntheticTransitionsDataSets = emptyList();
        List<AnalyticsTransitionsDataSet> analyticTransitionsDataSets = analyticGenerator.getAnalyticDataSets();
        return new FollowUpData(getFromJiraDs(), analyticTransitionsDataSets, syntheticTransitionsDataSets);
    }

    private FromJiraDataSet getFromJiraDs() {
        return fromJiraDs.orElse(KpiHelper.getDefaultFromJiraDs());
    }

    public AnalyticsDataSetsGenerator analyticBuilder() {
        return analyticGenerator;
    }

    public FollowUpSnapshot buildSnapshot() {
        FollowUpSnapshot snapshot = Mockito.mock(FollowUpSnapshot.class);
        Mockito.when(snapshot.getData()).thenReturn(buildFollowupData());
        return snapshot;
    }


}
