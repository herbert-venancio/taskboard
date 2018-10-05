package objective.taskboard.auth.authorizer;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.auth.LoggedUserDetails;
import objective.taskboard.auth.authorizer.permission.Permission.PermissionDto;
import objective.taskboard.auth.authorizer.permission.PermissionContext;

@Service
public class Authorizer {

    private static final Logger log = LoggerFactory.getLogger(Authorizer.class);

    private LoggedUserDetails userDetails;
    private PermissionRepository permissionRepository;

    @Autowired
    public Authorizer(LoggedUserDetails userDetails, PermissionRepository permissionRepository) {
        this.userDetails = userDetails;
        this.permissionRepository = permissionRepository;
    }

    public boolean hasPermission(String permission) {
        log.debug("Authorize permission \"{}\"", permission);
        return permissionRepository.findByName(permission).accepts(userDetails, PermissionContext.empty());
    }

    public boolean hasPermission(String permission, String target) {
        log.debug("Authorize permission \"{}\" for target \"{}\"", permission, target);
        return permissionRepository.findByName(permission).accepts(userDetails, new PermissionContext(target));
    }

    public List<PermissionDto> getPermissions() {
        List<PermissionDto> permissions = new ArrayList<>();

        permissionRepository.findAll().stream()
            .forEach(p -> p.toDto(userDetails).ifPresent(dto -> permissions.add(dto)));

        return permissions;
    }

    public List<String> getAllowedProjectsForPermissions(String... permissions) {
        return permissionRepository.findAllSpecificProjectPermissions().stream()
                .filter(permission -> asList(permissions).contains(permission.name()))
                .flatMap(permission -> permission.applicableTargets(userDetails).stream())
                .distinct()
                .collect(toList());
    }

    public boolean hasAnyRoleInProjects(List<String> roles, List<String> projectKeys) {
        return userDetails.getJiraRoles().stream()
                .anyMatch(role -> roles.contains(role.name) && projectKeys.contains(role.projectKey));
    }

    public List<String> getRolesForProject(String projectKey) {
        return userDetails.getJiraRoles().stream()
                .filter(role -> role.projectKey.equals(projectKey))
                .map(role -> role.name)
                .collect(toList());
    }

}
