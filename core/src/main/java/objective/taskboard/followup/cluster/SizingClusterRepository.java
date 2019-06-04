package objective.taskboard.followup.cluster;

import java.util.Optional;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;

@JaversSpringDataAuditable
public interface SizingClusterRepository extends JpaRepository<SizingCluster, Long> {

    Optional<SizingCluster> findById(long cluster);

}
