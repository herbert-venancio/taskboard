package objective.taskboard.auth.authorizer.permission;

import static objective.taskboard.auth.LoggedUserDetails.JiraRole.PROJECT_ADMINISTRATORS;
import static objective.taskboard.auth.LoggedUserDetails.JiraRole.PROJECT_DEVELOPERS;
import static objective.taskboard.auth.LoggedUserDetails.JiraRole.PROJECT_KPI;
import static objective.taskboard.auth.authorizer.Permissions.PROJECT_DASHBOARD_TACTICAL;

import org.springframework.stereotype.Service;

import objective.taskboard.auth.LoggedUserDetails;

@Service
public class ProjectDashboardTacticalPermission extends PerProjectPermission {

    public ProjectDashboardTacticalPermission(LoggedUserDetails loggedUserDetails) {
        super(PROJECT_DASHBOARD_TACTICAL, loggedUserDetails, PROJECT_ADMINISTRATORS, PROJECT_DEVELOPERS, PROJECT_KPI);
    }

}
