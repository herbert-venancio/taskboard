package objective.taskboard.followup;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Comparator.comparing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import objective.taskboard.followup.FromJiraRowCalculator.FromJiraRowCalculation;
import objective.taskboard.followup.ReleaseHistoryProvider.ProjectRelease;
import objective.taskboard.followup.cluster.FollowupCluster;
import objective.taskboard.project.ProjectProfileItem;

public class FollowUpSnapshot {
    private final FollowUpTimeline timeline;
    private final FollowUpData followupData;
    private final FollowupCluster followupCluster;
    private final FromJiraRowCalculator rowCalculator;
    private final FollowUpSnapshotValuesProvider valuesProvider;
    private final List<FromJiraRowCalculation> fromJiraRowCalculations;
    
    private List<EffortHistoryRow> effortHistory;
    private List<ProjectRelease> releases;
    private List<ProjectProfileItem> projectProfile;
    private Optional<FollowUpData> scopeBaseline;
    private List<FromJiraRowCalculation> scopeBaselineRowCalculations;

    public FollowUpSnapshot(FollowUpTimeline timeline, FollowUpData followupData, FollowupCluster followupCluster, FollowUpSnapshotValuesProvider valuesProvider) {
        this.timeline = timeline;
        this.followupData = followupData;
        this.followupCluster = followupCluster;
        this.valuesProvider = valuesProvider;
        this.rowCalculator = new FromJiraRowCalculator(followupCluster);
        this.fromJiraRowCalculations = rowCalculator.calculate(followupData.fromJiraDs.rows);
    }

    public FollowUpTimeline getTimeline() {
        return timeline;
    }

    public FollowUpData getData() {
        return followupData;
    }

    public List<EffortHistoryRow> getEffortHistory() {
        if (effortHistory == null)
            buildEffortHistory();
        
        return effortHistory;
    }
    
    public List<ProjectRelease> getReleases() {
        if (releases == null)
            releases = unmodifiableList(valuesProvider.getReleases());

        return releases;
    }
    
    public List<ProjectProfileItem> getProjectProfile() {
        if (projectProfile == null)
            projectProfile = unmodifiableList(valuesProvider.getProjectProfile());

        return projectProfile;
    }
    
    public Optional<FollowUpData> getScopeBaseline() {
        if (scopeBaseline == null) //NOSONAR
            scopeBaseline = valuesProvider.getScopeBaseline();
        
        return scopeBaseline;
    }

    public List<FromJiraRowCalculation> getScopeBaselineRowCalculations() {
        if (scopeBaselineRowCalculations == null) {
            scopeBaselineRowCalculations = getScopeBaseline()
                    .map(data -> rowCalculator.calculate(data.fromJiraDs.rows))
                    .orElse(emptyList());
        }

        return scopeBaselineRowCalculations;
    }

    public List<FromJiraRowCalculation> getFromJiraRowCalculations() {
        return fromJiraRowCalculations;
    }

    public EffortHistoryRow getEffortHistoryRow() {
        double sumEffortDone = 0;
        double sumEffortBacklog = 0;
        
        for (FromJiraRowCalculation rowCalculation : getFromJiraRowCalculations()) {
            sumEffortDone     += rowCalculation.getEffortDone();
            sumEffortBacklog  += rowCalculation.getEffortOnBacklog();
        }

        return new EffortHistoryRow(getTimeline().getReference(), sumEffortDone, sumEffortBacklog);
    }

    public boolean hasClusterConfiguration() {
        return !followupCluster.isEmpty();
    }

    public FollowupCluster getCluster() {
        return followupCluster;
    }

    private void buildEffortHistory() {
        List<EffortHistoryRow> result = new ArrayList<>(valuesProvider.getEffortHistory());
        result.add(getEffortHistoryRow());
        Collections.sort(result, comparing(r -> r.date));

        this.effortHistory = unmodifiableList(result);
    }
}