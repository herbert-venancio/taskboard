package objective.taskboard.followup.cluster;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SizingClusterRepository extends JpaRepository<SizingCluster, Long> {

    SizingCluster findById(Long id);

}
