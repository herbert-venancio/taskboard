package objective.taskboard.followup;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import objective.taskboard.followup.cluster.FollowUpClusterItem;
import objective.taskboard.followup.cluster.FollowUpClusterItemRepository;
import objective.taskboard.followup.data.Template;

@Component
public class FollowupClusterProvider {
    @Autowired
    private FollowUpClusterItemRepository clusterItemRepository;
    
    public FollowupCluster getFor(Template followUpConfiguration) {
        List<FollowUpClusterItem> clusterItems = clusterItemRepository.findByFollowUpConfiguration(followUpConfiguration);
        return new FollowupCluster(clusterItems);
    }

}
