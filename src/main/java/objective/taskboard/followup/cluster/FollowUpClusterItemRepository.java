package objective.taskboard.followup.cluster;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import objective.taskboard.followup.data.Template;

public interface FollowUpClusterItemRepository extends JpaRepository<FollowUpClusterItem, String> {

    List<FollowUpClusterItem> findByFollowUpConfiguration(Template followUpConfiguration);

}
