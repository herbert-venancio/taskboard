package objective.taskboard.followup.cluster;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SizingClusterItemRepository extends JpaRepository<SizingClusterItem, Long> {

    Optional<List<SizingClusterItem>> findByProjectKeyOrBaseCluster(String projectKey, SizingCluster baseCluster);

}
