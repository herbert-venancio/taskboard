package objective.taskboard.auth.authorizer;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.auth.LoggedUserDetails;
import objective.taskboard.auth.authorizer.permission.Permission;
import objective.taskboard.auth.authorizer.permission.PermissionContext;
import objective.taskboard.auth.authorizer.permission.TargetlessPermission;
import objective.taskboard.auth.authorizer.permission.TargettedPermission;

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
        return PermissionDto.from(permissionRepository.findAll(), userDetails);
    }

    public List<String> getAllowedProjectsForPermissions(String... permissions) {
        return permissionRepository.findAllPerProjectPermissions().stream()
                .filter(permission -> asList(permissions).contains(permission.name()))
                .flatMap(permission -> permission.applicableTargets(userDetails).orElseThrow(IllegalStateException::new).stream())
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

    public static class PermissionDto {
        public final String name;
        public final List<String> applicableTargets;

        private PermissionDto(String name, List<String> applicableTargets) {
            this.name = name;
            this.applicableTargets = applicableTargets;
        }

        public static List<PermissionDto> from(List<Permission> permissions, LoggedUserDetails userDetails) {
            List<PermissionDto> dtoList = new ArrayList<>();
            permissions
                .forEach(p -> PermissionDto.from(p, userDetails).ifPresent(dto -> dtoList.add(dto)));
            return dtoList;
        }

        private static Optional<PermissionDto> from(Permission permission, LoggedUserDetails userDetails) {
            Optional<List<String>> applicableTargets = permission.applicableTargets(userDetails);

            if (isTargetlessPermitted(permission, userDetails) || isTargettedPermitted(permission, applicableTargets))
                return Optional.of(new PermissionDto(permission.name(), applicableTargets.orElse(null)));

            return Optional.empty();
        }

        private static boolean isTargetlessPermitted(Permission permission, LoggedUserDetails userDetails) {
            return permission instanceof TargetlessPermission && permission.accepts(userDetails, PermissionContext.empty());
        }

        private static boolean isTargettedPermitted(Permission permission, Optional<List<String>> applicableTargets) {
            return permission instanceof TargettedPermission && applicableTargets.isPresent() && applicableTargets.get().size() > 0;
        }
    }

}
