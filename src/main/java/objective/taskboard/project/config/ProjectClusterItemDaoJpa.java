package objective.taskboard.project.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.cluster.SizingClusterItem;
import objective.taskboard.followup.cluster.SizingClusterItemRepository;
import objective.taskboard.project.config.ProjectClusterService.ProjectClusterItem;

@Component
class ProjectClusterItemDaoJpa implements ProjectClusterItemDao {

    private final SizingClusterItemRepository clusterItemRepository;

    @Autowired
    public ProjectClusterItemDaoJpa(SizingClusterItemRepository clusterItemRepository) {
        this.clusterItemRepository = clusterItemRepository;
    }

    @Override
    public List<SizingClusterItem> findByProjectKey(String projectKey) {
        return clusterItemRepository.findByProjectKey(projectKey);
    }

    @Override
    public void create(ProjectFilterConfiguration project, ProjectClusterItem itemUpdate) {
        SizingClusterItem newItem = new SizingClusterItem(
                itemUpdate.getIssueType(),
                "notused",
                itemUpdate.getSizing(),
                itemUpdate.getEffort(),
                itemUpdate.getCycle(),
                project.getProjectKey(),
                null);
        clusterItemRepository.save(newItem);
    }

    @Override
    public void update(SizingClusterItem item, ProjectClusterItem itemUpdate) {
        item.setEffort(itemUpdate.getEffort());
        item.setCycle(itemUpdate.getCycle());
        clusterItemRepository.save(item);
    }

}
