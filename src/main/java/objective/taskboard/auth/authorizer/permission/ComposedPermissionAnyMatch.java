package objective.taskboard.auth.authorizer.permission;

import objective.taskboard.auth.LoggedUserDetails;

public abstract class ComposedPermissionAnyMatch extends ComposedPermission {

    public ComposedPermissionAnyMatch(String name, LoggedUserDetails loggedUserDetails, Permission... permissions) {
        super(name, loggedUserDetails, permissions);
    }

    @Override
    public boolean accepts(PermissionContext permissionContext) {
        return permissions.stream().anyMatch(p -> {
            PermissionContext context = permissionContext;

            if (p instanceof TargetlessPermission)
                context = PermissionContext.empty();

            return p.accepts(context);
        });
    }

}
