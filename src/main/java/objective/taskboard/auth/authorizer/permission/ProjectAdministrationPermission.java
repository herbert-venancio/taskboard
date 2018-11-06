package objective.taskboard.auth.authorizer.permission;

import static objective.taskboard.auth.LoggedUserDetails.JiraRole.PROJECT_ADMINISTRATORS;
import static objective.taskboard.auth.authorizer.Permissions.PROJECT_ADMINISTRATION;

import org.springframework.stereotype.Service;

import objective.taskboard.auth.LoggedUserDetails;

@Service
public class ProjectAdministrationPermission extends PerProjectPermission {

    public ProjectAdministrationPermission(LoggedUserDetails loggedUserDetails) {
        super(PROJECT_ADMINISTRATION, loggedUserDetails, PROJECT_ADMINISTRATORS);
    }

}
