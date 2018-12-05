package objective.taskboard.project.config.changeRequest;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import objective.taskboard.domain.ProjectFilterConfiguration;

interface ChangeRequestRepository extends JpaRepository<ChangeRequest, Long> {

    List<ChangeRequest> findByProjectOrderByDateDesc(ProjectFilterConfiguration project);
    Optional<ChangeRequest> findBaselineIsTrueByProject(ProjectFilterConfiguration project);
    
}
