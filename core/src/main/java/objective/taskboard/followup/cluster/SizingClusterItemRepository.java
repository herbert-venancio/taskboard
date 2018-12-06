package objective.taskboard.followup.cluster;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SizingClusterItemRepository extends JpaRepository<SizingClusterItem, Long> {

    List<SizingClusterItem> findByProjectKeyOrBaseCluster(String projectKey, Optional<SizingCluster> baseCluster);

}
