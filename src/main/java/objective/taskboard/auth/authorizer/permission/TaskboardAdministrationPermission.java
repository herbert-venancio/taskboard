package objective.taskboard.auth.authorizer.permission;

import static objective.taskboard.auth.authorizer.Permissions.TASKBOARD_ADMINISTRATION;

import org.springframework.stereotype.Service;

import objective.taskboard.auth.LoggedUserDetails;

@Service
public class TaskboardAdministrationPermission extends BasePermission implements TargetlessPermission {

    public TaskboardAdministrationPermission(LoggedUserDetails loggedUserDetails) {
        super(TASKBOARD_ADMINISTRATION, loggedUserDetails);
    }

    @Override
    public boolean isAuthorized(PermissionContext permissionContext) {
        validate(permissionContext);

        return getLoggedUser().isAdmin();
    }

}
