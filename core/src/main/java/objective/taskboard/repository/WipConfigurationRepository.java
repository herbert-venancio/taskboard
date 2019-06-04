package objective.taskboard.repository;

import java.util.Collection;
import java.util.List;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;

import objective.taskboard.domain.WipConfiguration;

@JaversSpringDataAuditable
public interface WipConfigurationRepository extends JpaRepository<WipConfiguration, Long> {
    List<WipConfiguration> findByTeamIn(Collection<String> teamNames);
}
