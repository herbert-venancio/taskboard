package objective.taskboard.cluster;

import java.util.List;
import java.util.Optional;

import objective.taskboard.followup.cluster.SizingCluster;

public interface ClusterRepository {
    Optional<SizingCluster> findById(Long cluster);
    List<SizingCluster> findAll();
    SizingCluster save(SizingCluster cluster);
}
