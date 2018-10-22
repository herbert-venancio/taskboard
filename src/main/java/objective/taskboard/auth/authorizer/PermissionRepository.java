package objective.taskboard.auth.authorizer;

import static java.util.stream.Collectors.toList;
import static objective.taskboard.auth.LoggedUserDetails.JiraRole.PROJECT_ADMINISTRATORS;
import static objective.taskboard.auth.LoggedUserDetails.JiraRole.PROJECT_DEVELOPERS;
import static objective.taskboard.auth.LoggedUserDetails.JiraRole.PROJECT_KPI;
import static objective.taskboard.auth.authorizer.Permissions.PROJECT_ADMINISTRATION;
import static objective.taskboard.auth.authorizer.Permissions.PROJECT_ADMINISTRATION_VIEW;
import static objective.taskboard.auth.authorizer.Permissions.PROJECT_DASHBOARD_OPERATIONAL;
import static objective.taskboard.auth.authorizer.Permissions.PROJECT_DASHBOARD_OPERATIONAL_VIEW;
import static objective.taskboard.auth.authorizer.Permissions.PROJECT_DASHBOARD_TACTICAL;
import static objective.taskboard.auth.authorizer.Permissions.PROJECT_DASHBOARD_TACTICAL_VIEW;
import static objective.taskboard.auth.authorizer.Permissions.TASKBOARD_ADMINISTRATION;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import objective.taskboard.auth.authorizer.permission.AnyProjectPermission;
import objective.taskboard.auth.authorizer.permission.Permission;
import objective.taskboard.auth.authorizer.permission.SpecificProjectPermission;
import objective.taskboard.auth.authorizer.permission.TaskboardPermission;

@Service
class PermissionRepository {

    private Map<String, Permission> permissionsMap = new HashMap<>();

    {
        permissionsMap.put(TASKBOARD_ADMINISTRATION,           new TaskboardPermission(TASKBOARD_ADMINISTRATION));

        permissionsMap.put(PROJECT_DASHBOARD_TACTICAL,         new SpecificProjectPermission(PROJECT_DASHBOARD_TACTICAL,    PROJECT_ADMINISTRATORS, PROJECT_DEVELOPERS, PROJECT_KPI));
        permissionsMap.put(PROJECT_DASHBOARD_TACTICAL_VIEW,    new AnyProjectPermission(PROJECT_DASHBOARD_TACTICAL_VIEW,    PROJECT_ADMINISTRATORS, PROJECT_DEVELOPERS, PROJECT_KPI));
        permissionsMap.put(PROJECT_DASHBOARD_OPERATIONAL,      new SpecificProjectPermission(PROJECT_DASHBOARD_OPERATIONAL, PROJECT_ADMINISTRATORS, PROJECT_DEVELOPERS));
        permissionsMap.put(PROJECT_DASHBOARD_OPERATIONAL_VIEW, new AnyProjectPermission(PROJECT_DASHBOARD_OPERATIONAL_VIEW, PROJECT_ADMINISTRATORS, PROJECT_DEVELOPERS));
        permissionsMap.put(PROJECT_ADMINISTRATION,             new SpecificProjectPermission(PROJECT_ADMINISTRATION,        PROJECT_ADMINISTRATORS));
        permissionsMap.put(PROJECT_ADMINISTRATION_VIEW,        new AnyProjectPermission(PROJECT_ADMINISTRATION_VIEW,        PROJECT_ADMINISTRATORS));
    }

    public List<Permission> findAll() {
        return new ArrayList<Permission>(permissionsMap.values());
    }

    public Permission findByName(String name) {
        Permission permission = permissionsMap.get(name);
        if (permission == null)
            throw new IllegalArgumentException("Permission "+ name +" is invalid.");
        return permission;
    }

    public List<Permission> findAllSpecificProjectPermissions() {
        return findAll().stream()
                .filter(p -> p instanceof SpecificProjectPermission)
                .collect(toList());
    }

}
