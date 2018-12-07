package objective.taskboard.project.config;

interface ProjectClusterItemRepository {

    void create(String projectKey, ProjectClusterItemDto itemUpdate);
    void update(Long id, ProjectClusterItemDto itemUpdate);

}
