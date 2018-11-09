package objective.taskboard.auth.authorizer.permission;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import java.util.List;

import objective.taskboard.auth.LoggedUserDetails;

public abstract class ComposedPermission extends BaseTargettedPermission {

    protected final List<Permission> permissions;

    public ComposedPermission(String name, LoggedUserDetails loggedUserDetails, Permission... permissions) {
        super(name, loggedUserDetails);
        this.permissions = asList(permissions);
    }

    @Override
    public List<String> applicableTargets() {
        if (!permissions.stream().anyMatch(p -> p instanceof TargettedPermission))
            return emptyList();

        List<String> applicableTargets = permissions.stream()
                .filter(TargettedPermission.class::isInstance)
                .map(TargettedPermission.class::cast)
                .flatMap(p -> p.applicableTargets().stream())
                .distinct()
                .collect(toList());
        return applicableTargets;
    }

}
