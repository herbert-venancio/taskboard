package objective.taskboard.auth.authorizer.permission;

public interface TargettedPermission extends Permission {

    default void validate(PermissionContext permissionContext) {
        if (permissionContext.isEmpty())
            throw new IllegalArgumentException("Empty PermissionContext isn't allowed for permission "+ name() +".");
    }

}
