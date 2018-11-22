package objective.taskboard.project.config.changeRequest;

import java.util.List;

import objective.taskboard.domain.ProjectFilterConfiguration;

interface ChangeRequestRepository {

    List<ChangeRequest> listByProject(ProjectFilterConfiguration project);
    void add(ChangeRequest item);
    void update(ChangeRequest item);
    void remove(ChangeRequest item);
}
