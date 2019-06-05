package objective.taskboard.configuration;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.configuration.exception.DashboardConfigurationDuplicateException;
import objective.taskboard.configuration.exception.DashboardConfigurationNotFoundException;
import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.jira.ProjectService;

@Service
public class DashboardConfigurationService {
    private final ProjectService projectService;
    private final DashboardConfigurationRepository repository;

    @Autowired
    public DashboardConfigurationService(
            ProjectService projectService,
            DashboardConfigurationRepository dashboardConfigurationRepository) {
        this.projectService = projectService;
        this.repository = dashboardConfigurationRepository;
    }

    public Optional<DashboardConfiguration> retrieveConfiguration(String projectKey) {
        ProjectFilterConfiguration project = getProjectConfiguration(projectKey);
        return repository.findByProject(project);
    }

    public DashboardConfiguration persistConfigurationForProject(String projectKey, DashboardConfiguration newConfiguration) {
          ProjectFilterConfiguration project = getProjectConfiguration(projectKey);
          newConfiguration.setProject(project);
          checkAlreadyCreated(projectKey, project);
          return repository.save(newConfiguration);
    }

    private void checkAlreadyCreated(String projectKey, ProjectFilterConfiguration project) {
        Optional<DashboardConfiguration> configuration = repository.findByProject(project);
          if (configuration.isPresent())
              throw new DashboardConfigurationDuplicateException(projectKey);
    }

    public DashboardConfiguration updateConfigurationForProject(
            String projectKey,
            DashboardConfiguration newConfiguration) {
        ProjectFilterConfiguration project = getProjectConfiguration(projectKey);
        Optional<DashboardConfiguration> configuration = repository.findByProject(project);
        if (!configuration.isPresent())
            throw new DashboardConfigurationNotFoundException(projectKey);

        DashboardConfiguration currentConfiguration = configuration.get();
        currentConfiguration.setTimelineDaysToDisplay(newConfiguration.getTimelineDaysToDisplay());
        return repository.save(currentConfiguration);
    }

    private ProjectFilterConfiguration getProjectConfiguration(String projectKey) {
        Optional<ProjectFilterConfiguration> project = projectService.getTaskboardProject(projectKey);
        if (!project.isPresent())
            throw new DashboardConfigurationNotFoundException(projectKey);
        
        return project.get();
    }
}
