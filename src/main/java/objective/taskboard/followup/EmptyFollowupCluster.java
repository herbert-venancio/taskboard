package objective.taskboard.followup;

import java.util.Optional;

import objective.taskboard.followup.cluster.FollowUpClusterItem;

public class EmptyFollowupCluster implements FollowupCluster {

    @Override
    public Optional<FollowUpClusterItem> getClusterFor(String subtaskType, String tshirtSize) {
        return Optional.empty();
    }
}
