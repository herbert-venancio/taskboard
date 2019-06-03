package objective.taskboard.project.config.changeRequest;

import java.util.List;
import java.util.Optional;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;

import objective.taskboard.domain.ProjectFilterConfiguration;

@JaversSpringDataAuditable
interface ChangeRequestRepository extends JpaRepository<ChangeRequest, Long> {

    List<ChangeRequest> findByProjectOrderByRequestDateDesc(ProjectFilterConfiguration project);
    Optional<ChangeRequest> findBaselineIsTrueByProject(ProjectFilterConfiguration project);
    
}
