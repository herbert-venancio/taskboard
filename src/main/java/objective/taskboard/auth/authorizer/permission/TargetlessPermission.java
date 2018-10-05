package objective.taskboard.auth.authorizer.permission;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import objective.taskboard.auth.LoggedUserDetails;

public interface TargetlessPermission extends Permission {

    default List<String> applicableTargets(LoggedUserDetails userDetails) {
        return Collections.emptyList();
    }

    default Optional<PermissionDto> toDto(LoggedUserDetails userDetails) {
        return accepts(userDetails, PermissionContext.empty())
                ? Optional.of(new PermissionDto(name(), Optional.empty()))
                : Optional.empty();
    }

    default void validate(PermissionContext permissionContext) {
        if (!permissionContext.isEmpty())
            throw new IllegalArgumentException("Only PermissionContext.empty() is allowed for permission "+ name() +".");
    }

}
