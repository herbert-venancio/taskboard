package objective.taskboard.project.config;

import java.util.List;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.cluster.SizingClusterItem;
import objective.taskboard.project.config.ProjectClusterService.ProjectClusterItem;

public interface ProjectClusterItemDao {

    List<SizingClusterItem> findByProjectKey(String projectKey);
    void create(ProjectFilterConfiguration project, ProjectClusterItem itemUpdate);
    void update(SizingClusterItem item, ProjectClusterItem itemUpdate);

}
