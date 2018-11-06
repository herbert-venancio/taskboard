package objective.taskboard.auth.authorizer.permission;

import static objective.taskboard.auth.LoggedUserDetails.JiraRole.PROJECT_ADMINISTRATORS;
import static objective.taskboard.auth.LoggedUserDetails.JiraRole.PROJECT_DEVELOPERS;
import static objective.taskboard.auth.authorizer.Permissions.PROJECT_DASHBOARD_OPERATIONAL;

import org.springframework.stereotype.Service;

@Service
public class ProjectDashboardOperationalPermission extends PerProjectPermission {

    public ProjectDashboardOperationalPermission() {
        super(PROJECT_DASHBOARD_OPERATIONAL, PROJECT_ADMINISTRATORS, PROJECT_DEVELOPERS);
    }

}
