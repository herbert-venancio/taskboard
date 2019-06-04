package objective.taskboard.repository;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;

import objective.taskboard.domain.TeamFilterConfiguration;

@JaversSpringDataAuditable
public interface TeamFilterConfigurationRepository extends JpaRepository<TeamFilterConfiguration, Long> {

}
