package objective.taskboard.repository;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;

import objective.taskboard.domain.ProjectFilterConfiguration;

@JaversSpringDataAuditable
public interface ProjectFilterConfigurationRepository extends JpaRepository<ProjectFilterConfiguration, Long> {

}
