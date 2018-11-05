package objective.taskboard.auth.authorizer;

import static java.util.stream.Collectors.toList;
import static objective.taskboard.auth.LoggedUserDetails.JiraRole.PROJECT_ADMINISTRATORS;
import static objective.taskboard.auth.LoggedUserDetails.JiraRole.PROJECT_DEVELOPERS;
import static objective.taskboard.auth.LoggedUserDetails.JiraRole.PROJECT_KPI;
import static objective.taskboard.auth.authorizer.Permissions.FOLLOWUP_TEMPLATE_EDIT;
import static objective.taskboard.auth.authorizer.Permissions.PROJECT_ADMINISTRATION;
import static objective.taskboard.auth.authorizer.Permissions.PROJECT_DASHBOARD_OPERATIONAL;
import static objective.taskboard.auth.authorizer.Permissions.PROJECT_DASHBOARD_VIEW;
import static objective.taskboard.auth.authorizer.Permissions.PROJECT_DASHBOARD_TACTICAL;
import static objective.taskboard.auth.authorizer.Permissions.TASKBOARD_ADMINISTRATION;
import static objective.taskboard.auth.authorizer.Permissions.SIZING_IMPORT_VIEW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import objective.taskboard.auth.authorizer.permission.AnyProjectPermission;
import objective.taskboard.auth.authorizer.permission.Permission;
import objective.taskboard.auth.authorizer.permission.PerProjectPermission;
import objective.taskboard.auth.authorizer.permission.TaskboardAdministrationPermission;

@Service
class PermissionRepository {

    private Map<String, Permission> permissionsMap = new HashMap<>();

    {
        permissionsMap.put(TASKBOARD_ADMINISTRATION,           new TaskboardAdministrationPermission(TASKBOARD_ADMINISTRATION));

        permissionsMap.put(PROJECT_ADMINISTRATION,             new PerProjectPermission(PROJECT_ADMINISTRATION,        PROJECT_ADMINISTRATORS));
        permissionsMap.put(PROJECT_DASHBOARD_VIEW,             new AnyProjectPermission(PROJECT_DASHBOARD_VIEW,    PROJECT_ADMINISTRATORS, PROJECT_DEVELOPERS, PROJECT_KPI));
        permissionsMap.put(PROJECT_DASHBOARD_TACTICAL,         new PerProjectPermission(PROJECT_DASHBOARD_TACTICAL,    PROJECT_ADMINISTRATORS, PROJECT_DEVELOPERS, PROJECT_KPI));
        permissionsMap.put(PROJECT_DASHBOARD_OPERATIONAL,      new PerProjectPermission(PROJECT_DASHBOARD_OPERATIONAL, PROJECT_ADMINISTRATORS, PROJECT_DEVELOPERS));

        permissionsMap.put(FOLLOWUP_TEMPLATE_EDIT,             new AnyProjectPermission(FOLLOWUP_TEMPLATE_EDIT,             PROJECT_ADMINISTRATORS));

        permissionsMap.put(SIZING_IMPORT_VIEW,                 new AnyProjectPermission(SIZING_IMPORT_VIEW,                 PROJECT_ADMINISTRATORS));
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

    public List<Permission> findAllPerProjectPermissions() {
        return findAll().stream()
                .filter(p -> p instanceof PerProjectPermission)
                .collect(toList());
    }

}
