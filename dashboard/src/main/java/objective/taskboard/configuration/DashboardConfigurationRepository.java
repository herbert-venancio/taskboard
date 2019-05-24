package objective.taskboard.configuration;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import objective.taskboard.domain.ProjectFilterConfiguration;

public interface DashboardConfigurationRepository extends JpaRepository<DashboardConfiguration, Long>{

    Optional<DashboardConfiguration> findByProject(ProjectFilterConfiguration project);

}
