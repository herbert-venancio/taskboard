package objective.taskboard.followup.cluster;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SizingClusterRepository extends JpaRepository<SizingCluster, Long> {

    Optional<SizingCluster> findById(Optional<Long> cluster);

}
