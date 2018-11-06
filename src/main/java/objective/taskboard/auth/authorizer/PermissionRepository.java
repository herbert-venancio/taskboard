package objective.taskboard.auth.authorizer;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.auth.authorizer.permission.PerProjectPermission;
import objective.taskboard.auth.authorizer.permission.Permission;

@Service
public class PermissionRepository {

    private final Map<String, Permission> permissionsMap;
    private final List<PerProjectPermission> perProjectPermissions;

    @Autowired
    public PermissionRepository(List<Permission> permissions, List<PerProjectPermission> perProjectPermissions) {
        this.permissionsMap = permissions.stream().collect(toMap(Permission::name, p -> p));
        this.perProjectPermissions = unmodifiableList(perProjectPermissions);
    }

    public List<Permission> findAll() {
        return new ArrayList<>(permissionsMap.values());
    }

    public Permission findByName(String name) {
        Permission permission = permissionsMap.get(name);
        if (permission == null)
            throw new IllegalArgumentException("Permission "+ name +" is invalid.");
        return permission;
    }

    public List<PerProjectPermission> findAllPerProjectPermissions() {
        return perProjectPermissions;
    }

}
