package objective.taskboard.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import objective.taskboard.domain.ProjectTeam;

public interface ProjectTeamRespository extends JpaRepository<ProjectTeam, Long> {

}
