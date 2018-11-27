package objective.taskboard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import objective.taskboard.data.UserTeam;

public interface UserTeamRepository extends JpaRepository<UserTeam, Long> {

}
