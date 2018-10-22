package objective.taskboard.auth.authorizer.permission;

import java.util.List;
import java.util.Optional;

import objective.taskboard.auth.LoggedUserDetails;

public interface Permission {

    String name();

    boolean accepts(LoggedUserDetails userDetails, PermissionContext permissionContext);

    Optional<List<String>> applicableTargets(LoggedUserDetails userDetails);

    void validate(PermissionContext permissionContext);

}
