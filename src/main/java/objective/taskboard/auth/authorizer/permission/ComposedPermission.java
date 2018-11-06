package objective.taskboard.auth.authorizer.permission;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import objective.taskboard.auth.LoggedUserDetails;

public abstract class ComposedPermission extends BasePermission {

    protected final List<Permission> permissions;

    public ComposedPermission(String name, Permission... permissions) {
        super(name);
        this.permissions = asList(permissions);
    }

    @Override
    public Optional<List<String>> applicableTargets(LoggedUserDetails userDetails) {
        if (!permissions.stream().anyMatch(p -> p instanceof TargettedPermission))
            return Optional.empty();

        List<String> applicableTargets = permissions.stream()
                .flatMap(p -> p.applicableTargets(userDetails).map(List::stream).orElseGet(Stream::empty))
                .distinct()
                .collect(toList());
        return Optional.of(applicableTargets);
    }

    @Override
    public void validate(PermissionContext permissionContext) {}

}
