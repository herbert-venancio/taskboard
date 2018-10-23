package objective.taskboard.auth.authorizer.permission;

import objective.taskboard.auth.LoggedUserDetails;

public class ComposedPermissionAnyMatch extends ComposedPermission {

    public ComposedPermissionAnyMatch(String name, Permission... permissions) {
        super(name, permissions);
    }

    @Override
    public boolean accepts(LoggedUserDetails userDetails, PermissionContext permissionContext) {
        return permissions.stream().anyMatch(p -> {
            PermissionContext context = permissionContext;

            if (p instanceof TargetlessPermission)
                context = PermissionContext.empty();

            return p.accepts(userDetails, context);
        });
    }

}
