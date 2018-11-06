package objective.taskboard.auth.authorizer;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.auth.authorizer.permission.FollowUpTemplateEditPermission;
import objective.taskboard.auth.authorizer.permission.PerProjectPermission;
import objective.taskboard.auth.authorizer.permission.Permission;
import objective.taskboard.auth.authorizer.permission.ProjectAdministrationPermission;
import objective.taskboard.auth.authorizer.permission.ProjectDashboardOperationalPermission;
import objective.taskboard.auth.authorizer.permission.ProjectDashboardTacticalPermission;
import objective.taskboard.auth.authorizer.permission.ProjectDashboardViewPermission;
import objective.taskboard.auth.authorizer.permission.SizingImportViewPermission;
import objective.taskboard.auth.authorizer.permission.TaskboardAdministrationPermission;
import objective.taskboard.auth.authorizer.permission.TeamEditPermission;
import objective.taskboard.auth.authorizer.permission.TeamsEditViewPermission;
import objective.taskboard.auth.authorizer.permission.UserVisibilityPermission;

@Service
public class PermissionRepository {

    private Map<String, Permission> permissionsMap = new HashMap<>();

    @Autowired
    private TaskboardAdministrationPermission taskboardAdministration;

    @Autowired
    private UserVisibilityPermission userVisibilityPermission;

    @Autowired
    private ProjectAdministrationPermission projectAdministrationPermission;

    @Autowired
    private ProjectDashboardViewPermission projectDashboardViewPermission;

    @Autowired
    private ProjectDashboardTacticalPermission projectDashboardTacticalPermission;

    @Autowired
    private ProjectDashboardOperationalPermission projectDashboardOperationalPermission;

    @Autowired
    private FollowUpTemplateEditPermission followUpTemplateEditPermission;

    @Autowired
    private SizingImportViewPermission sizingImportViewPermission;

    @Autowired
    private TeamsEditViewPermission teamsEditViewPermission;

    @Autowired
    private TeamEditPermission teamEditPermission;

    @PostConstruct
    private void generatePermissions() {

        List<Permission> permissions = asList(
                taskboardAdministration,
                userVisibilityPermission,
                projectAdministrationPermission,
                projectDashboardViewPermission,
                projectDashboardViewPermission,
                projectDashboardTacticalPermission,
                projectDashboardOperationalPermission,
                followUpTemplateEditPermission,
                sizingImportViewPermission,
                teamsEditViewPermission,
                teamEditPermission);

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
