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

public class GenerateSnapshot {
    
    private Optional<FromJiraDataSet> fromJiraDs = Optional.empty();
    private GenerateAnalyticsDataSets analyticBuilder;
    
    public GenerateSnapshot(KpiEnvironment environment) {
        this.analyticBuilder = new GenerateAnalyticsDataSets(environment);
    }
    
    public GenerateSnapshot withFromJiraDs(FromJiraDataSet fromJiraDs) {
        this.fromJiraDs = Optional.of(fromJiraDs);
        return this;
    }
    
    public FollowUpData buildFollowupData() {
        List<SyntheticTransitionsDataSet> syntheticTransitionsDataSets = emptyList();
        List<AnalyticsTransitionsDataSet> analyticTransitionsDataSets = analyticBuilder.getAnalyticDataSets();
        return new FollowUpData(getFromJiraDs(), analyticTransitionsDataSets, syntheticTransitionsDataSets);
    }

    private FromJiraDataSet getFromJiraDs() {
        return fromJiraDs.orElse(KpiHelper.getDefaultFromJiraDs());
    }

    public GenerateAnalyticsDataSets analyticBuilder() {
        return analyticBuilder;
    }

    public FollowUpSnapshot buildSnapshot() {
        FollowUpSnapshot snapshot = Mockito.mock(FollowUpSnapshot.class);
        Mockito.when(snapshot.getData()).thenReturn(buildFollowupData());
        return snapshot;
    }


}
