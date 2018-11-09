package objective.taskboard.auth.authorizer.permission;

import java.util.List;

public interface TargettedPermission extends Permission {

    boolean isAuthorizedFor(String target);

    List<String> applicableTargets();

}
