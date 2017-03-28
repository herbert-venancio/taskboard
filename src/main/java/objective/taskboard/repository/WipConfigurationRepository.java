package objective.taskboard.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import objective.taskboard.domain.WipConfiguration;

public interface WipConfigurationRepository extends JpaRepository<WipConfiguration, Long> {
    List<WipConfiguration> findByTeam(String teamName);
}
