package objective.taskboard.project.config;

interface ProjectClusterItemRepository {

    void create(ProjectClusterItemDto itemUpdate);
    void update(Long id, ProjectClusterItemDto itemUpdate);

}
