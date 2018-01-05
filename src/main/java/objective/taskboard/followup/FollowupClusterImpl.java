package objective.taskboard.followup;

import java.util.List;
import java.util.Optional;

import objective.taskboard.followup.cluster.FollowUpClusterItem;

public class FollowupClusterImpl implements FollowupCluster {
    public final List<FollowUpClusterItem> clusterItems;
    
    public FollowupClusterImpl(List<FollowUpClusterItem> clusterItems) {
        this.clusterItems = clusterItems;
    }

    @Override
    public Optional<FollowUpClusterItem> getClusterFor(String subtaskType, String tshirtSize) {
        return clusterItems.stream()
        .filter(i -> matchingCluster(i, subtaskType, tshirtSize))
        .findFirst();
    }
    
    private static boolean matchingCluster(FollowUpClusterItem clusterItem, String subtaskType, String tshirtSize) {
        return clusterItem.getSubtaskTypeName().equals(subtaskType)
                && clusterItem.getSizing().equals(tshirtSize);
    }    
}
