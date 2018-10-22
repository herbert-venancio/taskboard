package objective.taskboard.auth.authorizer.permission;

import java.util.List;
import java.util.Optional;

import objective.taskboard.auth.LoggedUserDetails;

public interface TargettedPermission extends Permission {

    default Optional<PermissionDto> toDto(LoggedUserDetails userDetails) {
        List<String> applicableTargets = applicableTargets(userDetails);
        return applicableTargets.size() > 0
                ? Optional.of(new PermissionDto(name(), Optional.of(applicableTargets)))
                : Optional.empty();
    }

    default void validate(PermissionContext permissionContext) {
        if (permissionContext.isEmpty())
            throw new IllegalArgumentException("Empty PermissionContext isn't allowed for permission "+ name() +".");
    }

}
