package objective.taskboard.auth.authorizer.permission;

import objective.taskboard.auth.LoggedUserDetails;

public abstract class ComposedPermissionAnyMatch extends ComposedPermission {

    public ComposedPermissionAnyMatch(String name, LoggedUserDetails loggedUserDetails, Permission... permissions) {
        super(name, loggedUserDetails, permissions);
    }

    @Override
    protected boolean isAuthorized(LoggedUserDetails loggedUserDetails, String target) {
        return permissions.stream().anyMatch(p -> {
            if (p instanceof TargetlessPermission)
                return ((TargetlessPermission)p).isAuthorized();

            return ((TargettedPermission)p).isAuthorizedFor(target);
        });
    }

}
