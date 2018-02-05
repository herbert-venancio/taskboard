package objective.taskboard.followup;

import java.util.List;
import java.util.Optional;

import objective.taskboard.followup.cluster.FollowUpClusterItem;

public interface FollowupCluster {
    Optional<FollowUpClusterItem> getClusterFor(String subtaskType, String tshirtSize);

    List<FollowUpClusterItem> getClusterItems();

    boolean isEmpty();
}
