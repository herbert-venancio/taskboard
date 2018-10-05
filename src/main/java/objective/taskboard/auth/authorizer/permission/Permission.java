package objective.taskboard.auth.authorizer.permission;

import java.util.List;
import java.util.Optional;

import objective.taskboard.auth.LoggedUserDetails;

public interface Permission {

    String name();

    boolean accepts(LoggedUserDetails userDetails, PermissionContext permissionContext);

    List<String> applicableTargets(LoggedUserDetails userDetails);

    Optional<PermissionDto> toDto(LoggedUserDetails userDetails);

    void validate(PermissionContext permissionContext);

    public static class PermissionDto {
        public final String name;
        public final List<String> applicableTargets;

        public PermissionDto(String name, Optional<List<String>> applicableTargets) {
            this.name = name;
            this.applicableTargets = applicableTargets.orElse(null);
        }
    }

}
