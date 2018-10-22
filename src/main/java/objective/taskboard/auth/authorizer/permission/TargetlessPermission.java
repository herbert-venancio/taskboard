package objective.taskboard.auth.authorizer.permission;

import java.util.List;
import java.util.Optional;

import objective.taskboard.auth.LoggedUserDetails;

public interface TargetlessPermission extends Permission {

    default Optional<List<String>> applicableTargets(LoggedUserDetails userDetails) {
        return Optional.empty();
    }

    default void validate(PermissionContext permissionContext) {
        if (!permissionContext.isEmpty())
            throw new IllegalArgumentException("Only PermissionContext.empty() is allowed for permission "+ name() +".");
    }

}
