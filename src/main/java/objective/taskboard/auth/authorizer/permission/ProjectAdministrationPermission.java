package objective.taskboard.auth.authorizer.permission;

import static objective.taskboard.auth.LoggedUserDetails.JiraRole.PROJECT_ADMINISTRATORS;
import static objective.taskboard.auth.authorizer.Permissions.PROJECT_ADMINISTRATION;

import org.springframework.stereotype.Service;

@Service
public class ProjectAdministrationPermission extends PerProjectPermission {

    public ProjectAdministrationPermission() {
        super(PROJECT_ADMINISTRATION, PROJECT_ADMINISTRATORS);
    }

}
