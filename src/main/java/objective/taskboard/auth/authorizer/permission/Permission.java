package objective.taskboard.auth.authorizer.permission;

import java.util.List;
import java.util.Optional;

public interface Permission {

    String name();

    boolean accepts(PermissionContext permissionContext);

    Optional<List<String>> applicableTargets();

    void validate(PermissionContext permissionContext);

}
