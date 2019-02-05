package objective.taskboard.followup;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import objective.taskboard.followup.ReleaseHistoryProvider.ProjectRelease;
import objective.taskboard.project.ProjectProfileItem;

class FixedFollowUpSnapshotValuesProvider implements FollowUpSnapshotValuesProvider {

    private List<EffortHistoryRow> effortHistory = Collections.emptyList();
    private List<ProjectRelease> releases = Collections.emptyList();
    private List<ProjectProfileItem> projectProfile = Collections.emptyList();
    private Optional<FollowUpData> scopeBaseline = Optional.empty();

    public static FollowUpSnapshotValuesProvider emptyValuesProvider() {
        return new FixedFollowUpSnapshotValuesProvider();
    }

    public FixedFollowUpSnapshotValuesProvider effortHistory(List<EffortHistoryRow> effortHistory) {
        this.effortHistory = effortHistory;
        return this;
    }

    public FixedFollowUpSnapshotValuesProvider releases(List<ProjectRelease> releases) {
        this.releases = releases;
        return this;
    }

    public FixedFollowUpSnapshotValuesProvider projectProfile(List<ProjectProfileItem> projectProfile) {
        this.projectProfile = projectProfile;
        return this;
    }

    public FixedFollowUpSnapshotValuesProvider scopeBaseline(Optional<FollowUpData> scopeBaseline) {
        this.scopeBaseline = scopeBaseline;
        return this;
    }

    @Override
    public List<EffortHistoryRow> getEffortHistory() {
        return effortHistory;
    }

    @Override
    public List<ProjectRelease> getReleases() {
        return releases;
    }

    @Override
    public Optional<FollowUpData> getScopeBaseline() {
        return scopeBaseline;
    }

    @Override
    public List<ProjectProfileItem> getProjectProfile() {
        return projectProfile;
    }
}
