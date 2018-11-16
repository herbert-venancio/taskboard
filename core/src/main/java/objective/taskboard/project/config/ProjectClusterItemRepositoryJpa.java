package objective.taskboard.project.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import objective.taskboard.followup.cluster.SizingClusterItem;
import objective.taskboard.followup.cluster.SizingClusterItemRepository;

@Repository
class ProjectClusterItemRepositoryJpa implements ProjectClusterItemRepository {

    private static final String NOTUSED = "notused";
    private final SizingClusterItemRepository clusterItemRepository;

    @Autowired
    public ProjectClusterItemRepositoryJpa(SizingClusterItemRepository clusterItemRepository) {
        this.clusterItemRepository = clusterItemRepository;
    }

    @Override
    public void create(String projectKey, ProjectClusterItemDto itemUpdate) {
        SizingClusterItem newItem = new SizingClusterItem(
                itemUpdate.getIssueType(),
                NOTUSED,
                itemUpdate.getSizing(),
                itemUpdate.getEffort(),
                itemUpdate.getCycle(),
                projectKey,
                null);
        clusterItemRepository.save(newItem);
    }

    @Override
    public void update(Long id, ProjectClusterItemDto itemUpdate) {
        SizingClusterItem item = clusterItemRepository.findOne(id);
        item.setEffort(itemUpdate.getEffort());
        item.setCycle(itemUpdate.getCycle());
        clusterItemRepository.save(item);
    }

}
