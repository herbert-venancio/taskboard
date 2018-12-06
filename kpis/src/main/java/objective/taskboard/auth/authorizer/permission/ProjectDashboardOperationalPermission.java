package objective.taskboard.auth.authorizer.permission;

import static objective.taskboard.auth.LoggedUserDetails.JiraRole.PROJECT_ADMINISTRATORS;
import static objective.taskboard.auth.LoggedUserDetails.JiraRole.PROJECT_DEVELOPERS;
import static objective.taskboard.auth.authorizer.Permissions.PROJECT_DASHBOARD_OPERATIONAL;

import org.springframework.stereotype.Service;

import objective.taskboard.auth.LoggedUserDetails;

@Service
public class ProjectDashboardOperationalPermission extends PerProjectPermission {

    public ProjectDashboardOperationalPermission(LoggedUserDetails loggedUserDetails) {
        super(PROJECT_DASHBOARD_OPERATIONAL, loggedUserDetails, PROJECT_ADMINISTRATORS, PROJECT_DEVELOPERS);
    }

}
