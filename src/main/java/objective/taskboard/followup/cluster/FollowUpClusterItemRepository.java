package objective.taskboard.followup.cluster;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import objective.taskboard.domain.ProjectFilterConfiguration;

public interface FollowUpClusterItemRepository extends JpaRepository<FollowUpClusterItem, String> {

    List<FollowUpClusterItem> findByProject(ProjectFilterConfiguration project);

}
