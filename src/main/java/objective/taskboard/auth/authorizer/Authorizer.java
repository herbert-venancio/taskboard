package objective.taskboard.auth.authorizer;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.auth.LoggedUserDetails;
import objective.taskboard.auth.authorizer.permission.TargetlessPermission;
import objective.taskboard.auth.authorizer.permission.TargettedPermission;

@Service
public class Authorizer {

    private LoggedUserDetails userDetails;
    private PermissionRepository permissionRepository;

    @Autowired
    public Authorizer(LoggedUserDetails userDetails, PermissionRepository permissionRepository) {
        this.userDetails = userDetails;
        this.permissionRepository = permissionRepository;
    }

    public List<PermissionDto> getPermissions() {
        return PermissionDto.from(permissionRepository.findAllTargetless(), permissionRepository.findAllTargetted());
    }

    public List<String> getAllowedProjectsForPermissions(String... permissions) {
        List<String> permissionsList = asList(permissions);

        List<String> targets = permissionRepository.findAllPerProjectPermissions().stream()
                .filter(permission -> permissionsList.contains(permission.name()))
                .flatMap(permission -> permission.applicableTargets().stream())
                .distinct()
                .collect(toList());

        return targets;
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

        public static List<PermissionDto> from(List<TargetlessPermission> targetlessPermissions, List<TargettedPermission> targettedPermissions) {
            Stream<PermissionDto> targetlessStream = targetlessPermissions.stream()
                    .filter(p -> p.isAuthorized())
                    .map(p -> new PermissionDto(p.name(), null));

            Stream<PermissionDto> targettedStream = targettedPermissions.stream()
                    .map(p -> new PermissionDto(p.name(), p.applicableTargets()))
                    .filter(dto -> !dto.applicableTargets.isEmpty());

            return Stream.concat(targetlessStream, targettedStream).collect(toList());
        }

    }

}
