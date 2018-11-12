package objective.taskboard.auth.authorizer;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static objective.taskboard.auth.authorizer.permission.PermissionBuilder.perProjectPermission;
import static objective.taskboard.auth.authorizer.permission.PermissionBuilder.targetlessPermission;
import static objective.taskboard.auth.authorizer.permission.PermissionBuilder.targettedPermission;
import static objective.taskboard.auth.authorizer.permission.PermissionTestUtils.role;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Test;

import objective.taskboard.auth.LoggedUserDetails;
import objective.taskboard.auth.LoggedUserDetails.JiraRole;
import objective.taskboard.auth.authorizer.Authorizer.PermissionDto;
import objective.taskboard.auth.authorizer.permission.PerProjectPermission;
import objective.taskboard.auth.authorizer.permission.Permission;
import objective.taskboard.auth.authorizer.permission.TargetlessPermission;
import objective.taskboard.auth.authorizer.permission.TargettedPermission;

public class AuthorizerTest {

    @Test
    public void getPermissions_returnTargettedIfHApplicableTargetsIsntEmptyAndTargetlessIfIsAuthorizedEqualsTrue() {
        Authorizer subject = authorizer()
                .withPermissions(
                        targettedPermission("permission.a").applicableTo("PROJ1", "PROJ2"),
                        targetlessPermission("permission.a.view").authorized(),
                        targettedPermission("permission.b").applicableTo("PROJ1"),
                        targetlessPermission("permission.b.view").authorized(),
                        targettedPermission("permission.not").notApplicableToAnyTarget(),
                        targetlessPermission("permission.not.view").notAuthorized()
                        )
                .build();

        assertPermissionDtoList(subject.getPermissions(),
                "permission.a.view: null",
                "permission.b.view: null",
                "permission.a: [PROJ1, PROJ2]",
                "permission.b: [PROJ1]"
                );
    }

    public static void main(String[] args) {
        Optional<List<String>> opt = Optional.empty();
        asList("aaa").stream()
                .flatMap(permission -> opt.orElseThrow(IllegalStateException::new).stream())
                .distinct()
                .collect(toList());
    }

    @Test
    public void getAllowedProjectsForPermissions_returnApplicableTargetsList() {
        Authorizer subject = authorizer()
                .withPermissions(
                        targettedPermission("permission.y").applicableTo("PROJ4", "PROJ5"),
                        targettedPermission("permission.x").applicableTo("PROJ1", "PROJ4"),
                        perProjectPermission("permission.a").applicableTo("PROJ1", "PROJ2"),
                        perProjectPermission("permission.b").applicableTo("PROJ3"),
                        perProjectPermission("permission.c").applicableTo("PROJ1", "PROJ2"),
                        perProjectPermission("permission.d").notApplicableToAnyTarget()
                        )
                .build();

        assertThat(subject.getAllowedProjectsForPermissions("permission.b"))
                .containsExactly("PROJ3");

        assertThat(subject.getAllowedProjectsForPermissions("permission.a", "permission.b"))
                .containsExactlyInAnyOrder("PROJ1", "PROJ2", "PROJ3");

        assertThat(subject.getAllowedProjectsForPermissions("permission.d"))
                .isEmpty();
    }

    @Test
    public void getRolesForProject_returnCorrectlyValues() {
        Authorizer subject = authorizer()
                .withLoggedUserJiraRoles(
                        role("Administrators", "PROJ1"),
                        role("Administrators", "PROJ2"),
                        role("Developers", "PROJ3")
                        )
                .withPermissions(
                        )
                .build();

        assertTrue(subject.hasAnyRoleInProjects(asList("Administrators", "Developers"), asList("PROJ1")));
        assertTrue(subject.hasAnyRoleInProjects(asList("Administrators", "Developers"), asList("PROJ3")));
        assertFalse(subject.hasAnyRoleInProjects(asList("Developers", "KPI"), asList("PROJ1")));
        assertFalse(subject.hasAnyRoleInProjects(asList("Administrators", "KPI"), asList("PROJ3")));
        assertFalse(subject.hasAnyRoleInProjects(asList("KPI"), asList("PROJ1", "PROJ2", "PROJ3")));
    }

    @Test
    public void getRolesForProject_thenShouldReturnCorrectly() {
        Authorizer subject = authorizer()
                .withLoggedUserJiraRoles(
                        role("Administrators", "PROJ1"),
                        role("Developers", "PROJ1"),
                        role("Administrators", "PROJ2"),
                        role("Developers", "PROJ3")
                        )
                .withPermissions(
                        )
                .build();

        assertThat(subject.getRolesForProject("PROJ1"))
                .containsExactly("Administrators", "Developers");

        assertThat(subject.getRolesForProject("PROJ2"))
                .containsExactly("Administrators");

        assertThat(subject.getRolesForProject("PROJ3"))
                .containsExactly("Developers");
    }

    private static DSLBuilder authorizer() {
        return new DSLBuilder();
    }

    private static void assertPermissionDtoList(List<PermissionDto> permissions, String... expectedPermissions) {
        assertEquals(
                String.join("\n", expectedPermissions),
                permissions.stream()
                    .map(permission -> {
                        String targetsAsString = permission.applicableTargets != null ? permission.applicableTargets.toString() : "null";
                        return permission.name +": "+ targetsAsString;
                    })
                    .collect(Collectors.joining("\n"))
                );
    }

    private static class DSLBuilder {

        private LoggedUserDetails userDetails = mock(LoggedUserDetails.class);
        private PermissionRepository permissionRepository = mock(PermissionRepository.class);

        public DSLBuilder withPermissions(Permission... permissions) {
            List<Permission> permissionsList = asList(permissions);

            when(permissionRepository.findAllTargetless()).thenReturn(
                    permissionsList.stream()
                            .filter(TargetlessPermission.class::isInstance)
                            .map(TargetlessPermission.class::cast)
                            .collect(toList()));
            when(permissionRepository.findAllTargetted()).thenReturn(
                    permissionsList.stream()
                            .filter(TargettedPermission.class::isInstance)
                            .map(TargettedPermission.class::cast)
                            .collect(toList()));

            List<PerProjectPermission> perProject = permissionsList.stream()
                    .filter(PerProjectPermission.class::isInstance)
                    .map(PerProjectPermission.class::cast)
                    .collect(toList());
            when(permissionRepository.findAllPerProjectPermissions()).thenReturn(perProject);

            return this;
        }

        public DSLBuilder withLoggedUserJiraRoles(JiraRole... jiraRoles) {
            when(userDetails.getJiraRoles()).thenReturn(asList(jiraRoles));
            return this;
        }

        public Authorizer build() {
            return new Authorizer(userDetails, permissionRepository);
        }

    }

}
