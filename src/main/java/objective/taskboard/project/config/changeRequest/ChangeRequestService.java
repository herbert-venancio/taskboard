package objective.taskboard.project.config.changeRequest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.domain.ProjectFilterConfiguration;

@Service
public class ChangeRequestService {

    private final ChangeRequestRepository changeRequestRepository;

    @Autowired
    public ChangeRequestService(ChangeRequestRepository changeRequestRepository) {
        this.changeRequestRepository = changeRequestRepository;
    }

    public List<ChangeRequest> listByProject(ProjectFilterConfiguration project) {
        return changeRequestRepository.listByProject(project);
    }
    
    public void delete(ChangeRequest changeRequest) {
        if (changeRequest.isBaseline()){
            throw new ChangeRequestBaselineRemovalException();
        }

        changeRequestRepository.remove(changeRequest);
    }
}
