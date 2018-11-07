package objective.taskboard.auth.authorizer;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static objective.taskboard.auth.authorizer.permission.PermissionBuilder.permission;
import static objective.taskboard.auth.authorizer.permission.PermissionTestUtils.role;
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
import objective.taskboard.auth.authorizer.Authorizer.PermissionDto;
import objective.taskboard.auth.authorizer.permission.PerProjectPermission;
import objective.taskboard.auth.authorizer.permission.Permission;

public class AuthorizerTest {

    @Test
    public void hasPermission_givenUserWithPermission_returnTrue() {
        Authorizer subject = authorizer()
                .withPermissions(
                        permission().withName("taskboard.admin.test").withIsAuthorized(true).asTargetless()
                        )
                .build();

        assertTrue(subject.hasPermission("taskboard.admin.test"));
    }

    @Test
    public void hasPermission_givenUserWithoutPermission_returnFalse() {
        Authorizer subject = authorizer()
                .withPermissions(
                        permission().withName("taskboard.admin.test").withIsAuthorized(false).asTargetless()
                        )
                .build();

        assertFalse(subject.hasPermission("taskboard.admin.test"));
    }

    @Test
    public void getPermissions_returnTargettedIfHApplicableTargetsIsntEmptyAndTargetlessIfIsAuthorizedEqualsTrue() {
        Authorizer subject = authorizer()
                .withPermissions(
                        permission().withName("permission.a").withIsAuthorized(true).withApplicableTargets("PROJ1", "PROJ2").asTargetted(),
                        permission().withName("permission.a.view").withIsAuthorized(true).asTargetless(),
                        permission().withName("permission.b").withIsAuthorized(true).withApplicableTargets("PROJ1").asTargetted(),
                        permission().withName("permission.b.view").withIsAuthorized(true).asTargetless(),
                        permission().withName("permission.not").withIsAuthorized(false).withApplicableTargets().asTargetted(),
                        permission().withName("permission.not.view").withIsAuthorized(false).asTargetless()
                        )
                .build();

        assertPermissionDtoList(subject.getPermissions(),
                "permission.a: [PROJ1, PROJ2]," +
                "permission.a.view: null," +
                "permission.b: [PROJ1]," +
                "permission.b.view: null"
                );
    }

    @Test
    public void getAllowedProjectsForPermissions_returnApplicableTargetsList() {
        Authorizer subject = authorizer()
                .withPermissions(
                        permission().withName("permission.y").withApplicableTargets("PROJ4", "PROJ5").asTargetted(),
                        permission().withName("permission.x").withApplicableTargets("PROJ1", "PROJ4").asTargetted(),
                        permission().withName("permission.a").withApplicableTargets("PROJ1", "PROJ2").asPerProjectPermission(),
                        permission().withName("permission.b").withApplicableTargets("PROJ3").asPerProjectPermission(),
                        permission().withName("permission.c").withApplicableTargets("PROJ1", "PROJ2").asPerProjectPermission(),
                        permission().withName("permission.d").withApplicableTargets().asPerProjectPermission()
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

    private static class DSLBuilder {

        private LoggedUserDetails userDetails = mock(LoggedUserDetails.class);
        private PermissionRepository permissionRepository = mock(PermissionRepository.class);

        public DSLBuilder withPermissions(Permission... permissions) {
            List<Permission> permissionsList = asList(permissions);

            when(permissionRepository.findAll()).thenReturn(permissionsList);

            when(permissionRepository.findByName(any())).thenAnswer(invocation -> {
                String name = (String) invocation.getArguments()[0];
                return permissionsList.stream().filter(p -> p.name().equals(name)).findFirst().orElseThrow(IllegalArgumentException::new);
            });

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
