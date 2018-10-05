package objective.taskboard.auth.authorizer;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import objective.taskboard.auth.LoggedUserDetails;
import objective.taskboard.auth.LoggedUserDetails.JiraRole;
import objective.taskboard.auth.authorizer.permission.AnyProjectPermission;
import objective.taskboard.auth.authorizer.permission.Permission;
import objective.taskboard.auth.authorizer.permission.Permission.PermissionDto;
import objective.taskboard.auth.authorizer.permission.SpecificProjectPermission;
import objective.taskboard.auth.authorizer.permission.TaskboardPermission;

public class AuthorizerTest {

    @Test
    public void givenUserWithTaskboardAdministrationPermission_whenVerifyThatPermission_thenShouldReturnTrue() {
        Authorizer subject = AuthorizerDSLBuilder.init()
            .withLoggedUserIsAdmin(true)
            .withPermissionsInRepository(
                    new TaskboardPermission("taskboard.admin.test")
                    )
            .build();

        assertTrue(subject.hasPermission("taskboard.admin.test"));
    }

    @Test
    public void givenUserWithoutTaskboardAdministrationPermission_whenVerifyThatPermission_thenShouldReturnFalse() {
        Authorizer subject = AuthorizerDSLBuilder.init()
            .withLoggedUserIsAdmin(false)
            .withPermissionsInRepository(
                    new TaskboardPermission("taskboard.admin.test")
                    )
            .build();

        assertFalse(subject.hasPermission("taskboard.admin.test"));
    }

    @Test
    public void givenUserRoles_whenVerifyPermissionThatContainsAny_thenShouldReturnTrue() {
        Authorizer subject = AuthorizerDSLBuilder.init()
            .withLoggedUserJiraRoles(
                    role("Administrators", "PROJ1"),
                    role("Administrators", "PROJ2"),
                    role("Developers", "PROJ3")
                    )
            .withPermissionsInRepository(
                    new AnyProjectPermission("permission.test", "Administrators")
                    )
            .build();

        assertTrue(subject.hasPermission("permission.test"));
    }

    @Test
    public void givenUserRoles_whenVerifyPermissionThatNotContainsAny_thenShouldReturnFalse() {
        Authorizer subject = AuthorizerDSLBuilder.init()
                .withLoggedUserJiraRoles(
                        role("Administrators", "PROJ1"),
                        role("Administrators", "PROJ2"),
                        role("Developers", "PROJ3")
                        )
                .withPermissionsInRepository(
                        new AnyProjectPermission("permission.test", "Reviewer")
                        )
                .build();

        assertFalse(subject.hasPermission("permission.test"));
    }

    @Test
    public void givenUserHasNoRoles_whenVerifyPermission_thenShouldReturnFalse() {
        Authorizer subject = AuthorizerDSLBuilder.init()
                .withLoggedUserJiraRoles(
                        )
                .withPermissionsInRepository(
                        new AnyProjectPermission("permission.test", "Reviewer")
                        )
                .build();

        assertFalse(subject.hasPermission("permission.test"));
    }

    @Test
    public void givenUserRoles_whenVerifyPermissionInProjectThatContainsAny_thenShouldReturnTrue() {
        Authorizer subject = AuthorizerDSLBuilder.init()
                .withLoggedUserJiraRoles(
                        role("Administrators", "PROJ1"),
                        role("Administrators", "PROJ2"),
                        role("Developers", "PROJ3")
                        )
                .withPermissionsInRepository(
                        new SpecificProjectPermission("permission.test", "Developers")
                        )
                .build();

        assertTrue(subject.hasPermission("permission.test", "PROJ3"));
    }

    @Test
    public void givenUserRoles_whenVerifyPermissionInProjectThatNotContainsAny_thenShouldReturnFalse() {
        Authorizer subject = AuthorizerDSLBuilder.init()
                .withLoggedUserJiraRoles(
                        role("Administrators", "PROJ1"),
                        role("Administrators", "PROJ2"),
                        role("Developers", "PROJ3")
                        )
                .withPermissionsInRepository(
                        new SpecificProjectPermission("permission.test", "Administrators")
                        )
                .build();

        assertFalse(subject.hasPermission("permission.test", "PROJ3"));
    }

    @Test
    public void givenUserHasNoRoles_whenVerifyPermissionInProject_thenShouldReturnFalse() {
        Authorizer subject = AuthorizerDSLBuilder.init()
                .withLoggedUserJiraRoles(
                        )
                .withPermissionsInRepository(
                        new SpecificProjectPermission("permission.test", "Administrators")
                        )
                .build();

        assertFalse(subject.hasPermission("permission.test", "PROJ3"));
    }

    @Test
    public void givenUserHasPermissionsInSomeProjects_whenGetAllowedProjectForAPermission_thenShouldReturnTheProjectThatHasPermission() {
        Authorizer subject = AuthorizerDSLBuilder.init()
                .withLoggedUserJiraRoles(
                        role("Administrators", "PROJ1"),
                        role("Administrators", "PROJ2"),
                        role("Developers", "PROJ3")
                        )
                .withPermissionsInRepository(
                        new SpecificProjectPermission("permission.a", "Administrators"),
                        new SpecificProjectPermission("permission.b", "Developers"),
                        new SpecificProjectPermission("permission.c", "Administrators", "Developers"),
                        new SpecificProjectPermission("permission.d", "Reviewer")
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
    public void givenUserRoles_whenVerifyIfHasAnyRoleInProjects_thenShouldReturnCorrectly() {
        Authorizer subject = AuthorizerDSLBuilder.init()
                .withLoggedUserJiraRoles(
                        role("Administrators", "PROJ1"),
                        role("Administrators", "PROJ2"),
                        role("Developers", "PROJ3")
                        )
                .withPermissionsInRepository(
                        )
                .build();

        assertTrue(subject.hasAnyRoleInProjects(asList("Administrators", "Developers"), asList("PROJ1")));
        assertTrue(subject.hasAnyRoleInProjects(asList("Administrators", "Developers"), asList("PROJ3")));
        assertFalse(subject.hasAnyRoleInProjects(asList("Developers", "KPI"), asList("PROJ1")));
        assertFalse(subject.hasAnyRoleInProjects(asList("Administrators", "KPI"), asList("PROJ3")));
        assertFalse(subject.hasAnyRoleInProjects(asList("KPI"), asList("PROJ1", "PROJ2", "PROJ3")));
    }

    @Test
    public void givenUserRoles_whenGetRolesForProject_thenShouldReturnCorrectly() {
        Authorizer subject = AuthorizerDSLBuilder.init()
                .withLoggedUserJiraRoles(
                        role("Administrators", "PROJ1"),
                        role("Developers", "PROJ1"),
                        role("Administrators", "PROJ2"),
                        role("Developers", "PROJ3")
                        )
                .withPermissionsInRepository(
                        )
                .build();

        assertThat(subject.getRolesForProject("PROJ1"))
                .containsExactly("Administrators", "Developers");

        assertThat(subject.getRolesForProject("PROJ2"))
                .containsExactly("Administrators");

        assertThat(subject.getRolesForProject("PROJ3"))
                .containsExactly("Developers");
    }

    @Test
    public void givenSomePermissions_whenVerifyPermission_thenShouldReturnOnlyAllowedPermissions() {
        Authorizer subject = AuthorizerDSLBuilder.init()
                .withLoggedUserIsAdmin(true)
                .withLoggedUserJiraRoles(
                        role("Administrators", "PROJ1"),
                        role("Administrators", "PROJ2"),
                        role("Developers", "PROJ3")
                        )
                .withPermissionsInRepository(
                        new TaskboardPermission("taskboard.permission.a"),
                        new SpecificProjectPermission("permission.a", "Administrators"),
                        new AnyProjectPermission("permission.a.view", "Administrators"),
                        new SpecificProjectPermission("permission.b", "Developers"),
                        new AnyProjectPermission("permission.b.view", "Developers"),
                        new SpecificProjectPermission("permission.c", "Administrators", "Developers"),
                        new AnyProjectPermission("permission.c.view", "Administrators", "Developers"),
                        new SpecificProjectPermission("permission.d", "Reviewer")
                        )
                .build();

        assertPermissionDtoList(subject.getPermissions(),
                "taskboard.permission.a: null," +
                "permission.a: [PROJ1, PROJ2]," +
                "permission.a.view: null," +
                "permission.b: [PROJ3]," +
                "permission.b.view: null," +
                "permission.c: [PROJ1, PROJ2, PROJ3]," +
                "permission.c.view: null"
                );
    }

    private static JiraRole role(String roleName, String projectKey) {
        return new JiraRole(1L, roleName, projectKey);
    }

    private static void assertPermissionDtoList(List<PermissionDto> permissions, String expertedPermissions) {
        assertEquals(
                expertedPermissions,
                permissions.stream()
                    .map(permission -> {
                        String targetsAsString = permission.applicableTargets != null ? permission.applicableTargets.toString() : "null";
                        return permission.name +": "+ targetsAsString;
                    })
                    .collect(Collectors.joining(","))
                );
    }

    private static class AuthorizerDSLBuilder {

        private LoggedUserDetails userDetails = mock(LoggedUserDetails.class);
        private PermissionRepository permissionRepository = mock(PermissionRepository.class);

        public static AuthorizerDSLBuilder init() {
            return new AuthorizerDSLBuilder();
        }

        public AuthorizerDSLBuilder withLoggedUserIsAdmin(boolean isAdmin) {
            when(userDetails.isAdmin()).thenReturn(isAdmin);
            return this;
        }

        public AuthorizerDSLBuilder withLoggedUserJiraRoles(JiraRole... jiraRoles) {
            when(userDetails.getJiraRoles()).thenReturn(asList(jiraRoles));
            return this;
        }

        public AuthorizerDSLBuilder withPermissionsInRepository(Permission... supportedPermissions) {
            List<Permission> permissions = asList(supportedPermissions);

            when(permissionRepository.findAll()).thenReturn(permissions);

            when(permissionRepository.findByName(any())).thenAnswer(invocation -> {
                String name = (String) invocation.getArguments()[0];
                return permissions.stream().filter(p -> p.name().equals(name)).findFirst().orElseThrow(IllegalArgumentException::new);
            });

            when(permissionRepository.findAllSpecificProjectPermissions()).thenCallRealMethod();

            return this;
        }

        public Authorizer build() {
            return new Authorizer(userDetails, permissionRepository);
        }

    }

}
