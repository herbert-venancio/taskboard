package objective.taskboard.auth.authorizer.permission;

import java.util.List;
import java.util.Optional;

public interface TargetlessPermission extends Permission {

    default Optional<List<String>> applicableTargets() {
        return Optional.empty();
    }

    default void validate(PermissionContext permissionContext) {
        if (!permissionContext.isEmpty())
            throw new IllegalArgumentException("Only PermissionContext.empty() is allowed for permission "+ name() +".");
    }

}
