package objective.taskboard.auth.authorizer.permission;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import objective.taskboard.auth.LoggedUserDetails;

public abstract class ComposedPermission implements Permission {

    private final String name;
    protected final List<Permission> permissions;

    public ComposedPermission(String name, Permission... permissions) {
        this.name = name;
        this.permissions = asList(permissions);
    }

    @Override
    public String name() {
        return name;
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
