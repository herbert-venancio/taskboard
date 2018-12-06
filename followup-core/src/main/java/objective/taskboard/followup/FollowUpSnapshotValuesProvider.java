package objective.taskboard.followup;

import java.util.List;
import java.util.Optional;

import objective.taskboard.followup.ReleaseHistoryProvider.ProjectRelease;
import objective.taskboard.project.ProjectProfileItem;

interface FollowUpSnapshotValuesProvider {

    List<EffortHistoryRow> getEffortHistory();
    List<ProjectRelease> getReleases();
    List<ProjectProfileItem> getProjectProfile();
    Optional<FollowUpData> getScopeBaseline();
 
}
