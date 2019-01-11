package objective.taskboard.project.config.changeRequest;

import static java.util.stream.Collectors.toList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import objective.taskboard.domain.ProjectFilterConfiguration;

public abstract class ChangeRequestMockRepository implements ChangeRequestRepository {
    private static final Map<Long, ChangeRequest> data = new HashMap<>();
    private static long id = 0;

    @Override
    public List<ChangeRequest> findByProjectOrderByRequestDateDesc(ProjectFilterConfiguration project) {
        return data.values().stream()
                .filter(i -> i.getProject().equals(project))
                .collect(toList());
    }

    @Override
    public <S extends ChangeRequest> S save(S changeRequest) {
        changeRequest.setId(id++);
        data.put(changeRequest.getId(), changeRequest);
        return changeRequest;
    }

    @Override
    public void delete(ChangeRequest changeRequest) {
        data.remove(changeRequest.getId());
    }

    @Override 
    public Optional<ChangeRequest> findBaselineIsTrueByProject(ProjectFilterConfiguration project){
        return data.values().stream()
            .filter(i -> i.getProject().equals(project) && i.isBaseline())
            .findAny();
    }
}
