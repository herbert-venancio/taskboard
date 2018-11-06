package objective.taskboard.auth.authorizer.permission;

import static objective.taskboard.auth.authorizer.Permissions.TASKBOARD_ADMINISTRATION;

import org.springframework.stereotype.Service;

import objective.taskboard.auth.LoggedUserDetails;

@Service
public class TaskboardAdministrationPermission implements TargetlessPermission {

    @Override
    public String name() {
        return TASKBOARD_ADMINISTRATION;
    }

    @Override
    public boolean accepts(LoggedUserDetails userDetails, PermissionContext permissionContext) {
        validate(permissionContext);

        return userDetails.isAdmin();
    }

}
