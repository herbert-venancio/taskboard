package objective.taskboard.project;

import java.util.List;

import objective.taskboard.domain.ProjectFilterConfiguration;

public interface ProjectProfileItemRepository {

    List<ProjectProfileItem> listByProject(ProjectFilterConfiguration project);
    void add(ProjectProfileItem item);
    void remove(ProjectProfileItem item);

}