package objective.taskboard.followup;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static objective.taskboard.followup.FollowUpHelper.getEmptyFollowupData;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.FromJiraRowCalculator.FromJiraRowCalculation;
import objective.taskboard.followup.ReleaseHistoryProvider.ProjectRelease;
import objective.taskboard.followup.cluster.EmptyFollowupCluster;
import objective.taskboard.followup.cluster.FollowUpClusterItem;
import objective.taskboard.followup.cluster.FollowupCluster;
import objective.taskboard.followup.cluster.FollowupClusterImpl;

class FollowUpSnapshotMockBuilder {
    private FollowupCluster cluster;
    private FollowUpData data;
    private List<EffortHistoryRow> effortHistory;
    private EffortHistoryRow effortHistoryRow;
    private List<ProjectRelease> releases;
    private FollowUpData scopeBaseline;
    private FollowUpTimeline timeline;
    
    public FollowUpSnapshot build() {
        FollowUpData data = this.data == null ? getEmptyFollowupData() : this.data;
        FollowUpTimeline timeline = this.timeline == null ? new FollowUpTimeline(LocalDate.parse("2018-01-01")) : this.timeline;
        
        List<FromJiraRowCalculation> fromJiraRowCalculations = data.fromJiraDs.rows.stream()
                .map(r -> new FromJiraRowCalculation(r, 5.0, 2.0, 3.0))
                .collect(toList());

        Optional<FollowUpData> scopeBaseline = Optional.ofNullable(this.scopeBaseline);
        List<FromJiraRowCalculation> scopeBaselineRowCalculations = scopeBaseline.isPresent()
                ? scopeBaseline.get().fromJiraDs.rows.stream().map(r -> new FromJiraRowCalculation(r, 5.0, 2.0, 3.0)).collect(toList())
                : emptyList();

        FollowupCluster cluster = this.cluster == null ? new EmptyFollowupCluster() : this.cluster;

        FollowUpSnapshot snapshot = mock(FollowUpSnapshot.class);
        when(snapshot.getCluster()).thenReturn(cluster);
        when(snapshot.hasClusterConfiguration()).thenReturn(!cluster.isEmpty());
        when(snapshot.getData()).thenReturn(data);
        when(snapshot.getEffortHistory()).thenReturn(effortHistory == null ? emptyList() : effortHistory);
        when(snapshot.getEffortHistoryRow()).thenReturn(effortHistoryRow == null ? new EffortHistoryRow(timeline.getReference(), 0.0, 0.0) : effortHistoryRow);
        when(snapshot.getFromJiraRowCalculations()).thenReturn(fromJiraRowCalculations);
        when(snapshot.getReleases()).thenReturn(releases == null ? emptyList() : releases);
        when(snapshot.getScopeBaseline()).thenReturn(scopeBaseline);
        when(snapshot.getScopeBaselineRowCalculations()).thenReturn(scopeBaselineRowCalculations);
        when(snapshot.getTimeline()).thenReturn(timeline);

        return snapshot;
    }
    
    public static FollowUpSnapshot empty() {
        return new FollowUpSnapshotMockBuilder().build();
    }
    
    public FollowUpSnapshotMockBuilder cluster(FollowupCluster cluster) {
        this.cluster = cluster;
        return this;
    }
    
    public FollowUpSnapshotMockBuilder validCluster() {
        ProjectFilterConfiguration project = mock(ProjectFilterConfiguration.class);
        this.cluster = new FollowupClusterImpl(asList(new FollowUpClusterItem(project, "UX", "no", "M", 0.0, 0.0)));
        return this;
    }
    
    public FollowUpSnapshotMockBuilder emptyCluster() {
        this.cluster = null;
        return this;
    }

    public FollowUpSnapshotMockBuilder data(FollowUpData data) {
        this.data = data;
        return this;
    }

    public FollowUpSnapshotMockBuilder effortHistory(List<EffortHistoryRow> effortHistory) {
        this.effortHistory = effortHistory;
        return this;
    }

    public FollowUpSnapshotMockBuilder effortHistoryRow(EffortHistoryRow effortHistoryRow) {
        this.effortHistoryRow = effortHistoryRow;
        return this;
    }

    public FollowUpSnapshotMockBuilder releases(List<ProjectRelease> releases) {
        this.releases = releases;
        return this;
    }

    public FollowUpSnapshotMockBuilder scopeBaseline(FollowUpData scopeBaseline) {
        this.scopeBaseline = scopeBaseline;
        return this;
    }

    public FollowUpSnapshotMockBuilder timeline(FollowUpTimeline timeline) {
        this.timeline = timeline;
        return this;
    }
    
    public FollowUpSnapshotMockBuilder timeline(LocalDate reference) {
        this.timeline = new FollowUpTimeline(reference);
        return this;
    }
}