package objective.taskboard.auth.authorizer.permission;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;

public class PermissionUtils {

    public static boolean isAuthorizedForAnyPermission(TargetlessPermission... permissions) {
        return Arrays.stream(permissions).anyMatch(p -> p.isAuthorized());
    }

    public static boolean isAuthorizedForAnyPermission(String target, Permission... permissions) {
        return Arrays.stream(permissions).anyMatch(p -> {
            if (p instanceof TargetlessPermission)
                return ((TargetlessPermission)p).isAuthorized();

            return ((TargettedPermission)p).isAuthorizedFor(target);
        });
    }

    public static List<String> applicableTargetsInAnyPermission(TargettedPermission... permissions) {
        List<String> applicableTargets = Arrays.stream(permissions)
                .flatMap(p -> p.applicableTargets().stream())
                .distinct()
                .collect(toList());

        return applicableTargets;
    }

}
