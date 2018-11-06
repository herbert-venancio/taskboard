package objective.taskboard.auth.authorizer.permission;

import static objective.taskboard.auth.LoggedUserDetails.JiraRole.PROJECT_ADMINISTRATORS;
import static objective.taskboard.auth.LoggedUserDetails.JiraRole.PROJECT_DEVELOPERS;
import static objective.taskboard.auth.LoggedUserDetails.JiraRole.PROJECT_KPI;
import static objective.taskboard.auth.authorizer.Permissions.PROJECT_DASHBOARD_TACTICAL;

import org.springframework.stereotype.Service;

@Service
public class ProjectDashboardTacticalPermission extends PerProjectPermission {

    public ProjectDashboardTacticalPermission() {
        super(PROJECT_DASHBOARD_TACTICAL, PROJECT_ADMINISTRATORS, PROJECT_DEVELOPERS, PROJECT_KPI);
    }

}
