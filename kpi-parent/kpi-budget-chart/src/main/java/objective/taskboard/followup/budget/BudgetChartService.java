package objective.taskboard.followup.budget;

import java.time.ZoneId;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.auth.authorizer.permission.ProjectDashboardCustomerPermission;
import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.project.ProjectNotFoundException;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;

@Service
public class BudgetChartService {

    private BudgetChartCalculator calculator;
    private ProjectFilterConfigurationCachedRepository projects;
    private ProjectDashboardCustomerPermission dashboardCustomerPermission;

    @Autowired
    public BudgetChartService(
            BudgetChartCalculator calculator, 
            ProjectFilterConfigurationCachedRepository projects, 
            ProjectDashboardCustomerPermission dashboardCustomerPermission) 
    {
        this.calculator = calculator;
        this.projects = projects;
        this.dashboardCustomerPermission = dashboardCustomerPermission;
    }

    public BudgetChartData load(ZoneId timezone, String projectKey ) throws RuntimeException {

        if (!dashboardCustomerPermission.isAuthorizedFor(projectKey))
            throw new ProjectNotFoundException(projectKey);

        Optional<ProjectFilterConfiguration> projectOpt = projects.getProjectByKey(projectKey);

        if (!projectOpt.isPresent())
            throw new ProjectNotFoundException(projectKey);

        ProjectFilterConfiguration project = projectOpt.get();

        return calculator.calculate(timezone, project);
    }
}
