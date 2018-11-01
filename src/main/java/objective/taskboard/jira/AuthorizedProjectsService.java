package objective.taskboard.jira;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.auth.authorizer.Authorizer;
import objective.taskboard.domain.ProjectFilterConfiguration;

@Service
public class AuthorizedProjectsService {

    private final ProjectService projectService;
    private final Authorizer authorizer;

    @Autowired
    public AuthorizedProjectsService(ProjectService projectService, Authorizer authorizer) {
        this.projectService = projectService;
        this.authorizer = authorizer;
    }

    public List<ProjectFilterConfiguration> getTaskboardProjects(
            Predicate<String> filterProjectByKey, String... permissions) {
        return getTaskboardProjects(permissions).stream()
                .filter(projectFilterConfiguration -> filterProjectByKey.test(projectFilterConfiguration.getProjectKey()))
                .collect(toList());
    }

    public List<ProjectFilterConfiguration> getTaskboardProjects(String... permissions) {
        List<String> allowedProjectsKeys = authorizer.getAllowedProjectsForPermissions(permissions);

        return projectService.getTaskboardProjects().stream()
                .filter(projectFilterConfiguration -> allowedProjectsKeys.contains(projectFilterConfiguration.getProjectKey()))
                .collect(toList());
    }

    public Optional<ProjectFilterConfiguration> getTaskboardProject(String projectKey, String... permissions) {
        List<String> allowedProjectsKeys = authorizer.getAllowedProjectsForPermissions(permissions);
        return allowedProjectsKeys.contains(projectKey) ? projectService.getTaskboardProject(projectKey) : Optional.empty();
    }

}
