package objective.taskboard.auth.authorizer;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static objective.taskboard.auth.LoggedUserDetails.JiraRole.PROJECT_ADMINISTRATORS;
import static objective.taskboard.auth.LoggedUserDetails.JiraRole.PROJECT_DEVELOPERS;
import static objective.taskboard.auth.LoggedUserDetails.JiraRole.PROJECT_KPI;
import static objective.taskboard.auth.authorizer.Permissions.FOLLOWUP_TEMPLATE_EDIT;
import static objective.taskboard.auth.authorizer.Permissions.PROJECT_ADMINISTRATION;
import static objective.taskboard.auth.authorizer.Permissions.PROJECT_DASHBOARD_OPERATIONAL;
import static objective.taskboard.auth.authorizer.Permissions.PROJECT_DASHBOARD_TACTICAL;
import static objective.taskboard.auth.authorizer.Permissions.PROJECT_DASHBOARD_VIEW;
import static objective.taskboard.auth.authorizer.Permissions.SIZING_IMPORT_VIEW;
import static objective.taskboard.auth.authorizer.Permissions.TASKBOARD_ADMINISTRATION;
import static objective.taskboard.auth.authorizer.Permissions.TEAMS_EDIT_VIEW;
import static objective.taskboard.auth.authorizer.Permissions.TEAM_EDIT;
import static objective.taskboard.auth.authorizer.Permissions.USER_VISIBILITY;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.auth.authorizer.permission.AnyProjectPermission;
import objective.taskboard.auth.authorizer.permission.AnyTeamPermissionAnyAcceptableRole;
import objective.taskboard.auth.authorizer.permission.ComposedPermissionAnyMatch;
import objective.taskboard.auth.authorizer.permission.PerProjectPermission;
import objective.taskboard.auth.authorizer.permission.PerTeamPermissionAnyAcceptableRole;
import objective.taskboard.auth.authorizer.permission.PerUserVisibilityOfUserPermission;
import objective.taskboard.auth.authorizer.permission.Permission;
import objective.taskboard.auth.authorizer.permission.TaskboardAdministrationPermission;
import objective.taskboard.data.UserTeam.UserTeamRole;
import objective.taskboard.repository.UserTeamCachedRepository;
import objective.taskboard.team.UserTeamService;

@Service
class PermissionRepository {

    @Autowired
    private UserTeamCachedRepository userTeamRepository;

    private Map<String, Permission> permissionsMap = new HashMap<>();

    @Autowired
    private UserTeamService userTeamService;

    @PostConstruct
    private void generatePermissions() {
        TaskboardAdministrationPermission taskboardAdministration = new TaskboardAdministrationPermission(TASKBOARD_ADMINISTRATION);

        List<Permission> permissions = asList(
                taskboardAdministration,
                new PerUserVisibilityOfUserPermission(USER_VISIBILITY, taskboardAdministration, userTeamService),
                new PerProjectPermission(PROJECT_ADMINISTRATION, PROJECT_ADMINISTRATORS),
                new AnyProjectPermission(PROJECT_DASHBOARD_VIEW, PROJECT_ADMINISTRATORS, PROJECT_DEVELOPERS, PROJECT_KPI),
                new PerProjectPermission(PROJECT_DASHBOARD_TACTICAL, PROJECT_ADMINISTRATORS, PROJECT_DEVELOPERS, PROJECT_KPI),
                new PerProjectPermission(PROJECT_DASHBOARD_OPERATIONAL, PROJECT_ADMINISTRATORS, PROJECT_DEVELOPERS),
                new AnyProjectPermission(FOLLOWUP_TEMPLATE_EDIT, PROJECT_ADMINISTRATORS),
                new AnyProjectPermission(SIZING_IMPORT_VIEW, PROJECT_ADMINISTRATORS),
                new ComposedPermissionAnyMatch(TEAMS_EDIT_VIEW,
                        taskboardAdministration,
                        new AnyTeamPermissionAnyAcceptableRole(TEAMS_EDIT_VIEW, userTeamRepository, UserTeamRole.MANAGER)),
                new ComposedPermissionAnyMatch(TEAM_EDIT,
                        taskboardAdministration,
                        new PerTeamPermissionAnyAcceptableRole(TEAM_EDIT, userTeamRepository, UserTeamRole.MANAGER))
                );

        permissionsMap = permissions.stream()
                .collect(toMap(Permission::name, p -> p));
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

    public List<Permission> findAllPerProjectPermissions() {
        return findAll().stream()
                .filter(p -> p instanceof PerProjectPermission)
                .collect(toList());
    }

}
