package objective.taskboard.auth.authorizer.permission;

import objective.taskboard.auth.LoggedUserDetails;

public class TaskboardPermission implements TargetlessPermission {

    private final String name;

    public TaskboardPermission(String name) {
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean accepts(LoggedUserDetails userDetails, PermissionContext permissionContext) {
        validate(permissionContext);

        return userDetails.isAdmin();
    }

}
