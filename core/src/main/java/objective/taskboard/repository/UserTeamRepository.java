package objective.taskboard.repository;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;

import objective.taskboard.data.UserTeam;

@JaversSpringDataAuditable
public interface UserTeamRepository extends JpaRepository<UserTeam, Long> {

}
