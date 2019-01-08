package objective.taskboard.project.config.changeRequest;

import static java.util.stream.Collectors.toSet;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import objective.taskboard.auth.authorizer.permission.ProjectAdministrationPermission;
import objective.taskboard.domain.ProjectFilterConfiguration;

@Service
public class ChangeRequestService {

    private final ChangeRequestRepository changeRequestRepository;
    private final ProjectAdministrationPermission projectAdministrationPermission;

    @Autowired
    public ChangeRequestService(ChangeRequestRepository changeRequestRepository,
        ProjectAdministrationPermission projectAdministrationPermission) {
        this.changeRequestRepository = changeRequestRepository;
        this.projectAdministrationPermission = projectAdministrationPermission;
    }

    public List<ChangeRequest> listByProject(ProjectFilterConfiguration project) {

        if (!projectAdministrationPermission.isAuthorizedFor(project.getProjectKey()))
            throw new AccessDeniedException("The current user doesn't have administrative permissions for this project."); 
        
        return changeRequestRepository.findByProjectOrderByRequestDateDesc(project);
    }

    @Transactional
    public void updateItems(ProjectFilterConfiguration project, List<ChangeRequest> requestCrs) {
        
        if (!projectAdministrationPermission.isAuthorizedFor(project.getProjectKey()))
            throw new AccessDeniedException("The current user doesn't have administrative permissions for this project.");
        
        List<ChangeRequest> existingCrs = changeRequestRepository.findByProjectOrderByRequestDateDesc(project);

        addNewItems(requestCrs, project);
        updateExistingItems(requestCrs, existingCrs);
        deleteMissingItems(requestCrs, existingCrs);
    }

    @Transactional
    public void updateBaselineChangeRequest(ProjectFilterConfiguration project, LocalDate startDate){
        
        if (!projectAdministrationPermission.isAuthorizedFor(project.getProjectKey()))
            throw new AccessDeniedException("The current user doesn't have administrative permissions for this project.");

        boolean updateBaseline = project.getStartDate()
            .map(originalStartDate -> !originalStartDate.equals(startDate))
            .orElse(startDate != null);

        if (updateBaseline) {
            Optional<ChangeRequest> existentBaseline = changeRequestRepository.findBaselineIsTrueByProject(project);

            if (existentBaseline.isPresent()) {
                existentBaseline.get().setRequestDate(startDate);
            } else {
                ChangeRequest baseline = new ChangeRequest(project, "Baseline", startDate, 0, true);
                changeRequestRepository.save(baseline);
            }
        }
    }

    private void addNewItems(List<ChangeRequest> requestCrs, ProjectFilterConfiguration project) {
        requestCrs.stream().filter(cr -> cr.getId() == null).map(
                cr -> new ChangeRequest(project, cr.getName(), cr.getRequestDate(), cr.getBudgetIncrease(), cr.isBaseline()))
                .forEach(i -> changeRequestRepository.save(i));
    }

    private void updateExistingItems(List<ChangeRequest> requestCrs, List<ChangeRequest> existingCrs) {
        Map<Long, ChangeRequest> existingCrsById = existingCrs.stream()
                .collect(Collectors.toMap(i -> i.getId(), i -> i));

        requestCrs.stream().filter(cr -> cr.getId() != null && existingCrsById.keySet().contains(cr.getId()))
                .forEach(cr -> {
                    ChangeRequest existingCr = existingCrsById.get(cr.getId());

                    existingCr.setName(cr.getName());
                    existingCr.setRequestDate(cr.getRequestDate());
                    existingCr.setBudgetIncrease(cr.getBudgetIncrease());
                });
    }

    private void deleteMissingItems(List<ChangeRequest> requestCrs, List<ChangeRequest> existingItems) {
        Set<Long> updatedIds = requestCrs.stream().filter(cr -> cr.getId() != null).map(cr -> cr.getId())
                .collect(toSet());

        existingItems.stream().filter(cr -> !updatedIds.contains(cr.getId())).forEach(cr -> {
            if (cr.isBaseline()) {
                throw new ChangeRequestBaselineRemovalException();
            }

            changeRequestRepository.delete(cr);
        });
    }
}