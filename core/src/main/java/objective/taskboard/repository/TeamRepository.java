package objective.taskboard.repository;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;

import objective.taskboard.data.Team;

@JaversSpringDataAuditable
public interface TeamRepository extends JpaRepository<Team, Long> {

}
