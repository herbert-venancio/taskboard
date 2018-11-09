package objective.taskboard.auth.authorizer;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import objective.taskboard.auth.authorizer.permission.PerProjectPermission;
import objective.taskboard.auth.authorizer.permission.TargetlessPermission;
import objective.taskboard.auth.authorizer.permission.TargettedPermission;

public class PermissionRepositoryTest {

    private List<TargetlessPermission> targetLessPermissions = new ArrayList<>();
    private List<TargettedPermission> targettedPermissions = new ArrayList<>();
    private List<PerProjectPermission> perProjectPermissions = new ArrayList<>();
    private PermissionRepository subject = new PermissionRepository(targetLessPermissions, targettedPermissions, perProjectPermissions);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void findAllTargetless_shouldReturnUnmodifiedListWithTargetlessPermissions() {
        targetLessPermissions.addAll(asList(mock(TargetlessPermission.class), mock(TargetlessPermission.class)));

        List<TargetlessPermission> allTargetless = subject.findAllTargetless();

        assertEquals(targetLessPermissions, allTargetless);
        assertIsUnmodifiable(allTargetless);
    }

    @Test
    public void findAllTargetted_shouldReturnUnmodifiedListWithTargettedPermissions() {
        targettedPermissions.addAll(asList(mock(TargettedPermission.class), mock(TargettedPermission.class)));

        List<TargettedPermission> allTargetted = subject.findAllTargetted();

        assertEquals(targettedPermissions, allTargetted);
        assertIsUnmodifiable(allTargetted);
    }

    @Test
    public void findAllPerProject_shouldReturnUnmodifiedListWithPerProjectPermissions() {
        perProjectPermissions.addAll(asList(mock(PerProjectPermission.class), mock(PerProjectPermission.class)));

        List<PerProjectPermission> allPerProject = subject.findAllPerProjectPermissions();

        assertEquals(perProjectPermissions, allPerProject);
        assertIsUnmodifiable(allPerProject);
    }

    private void assertIsUnmodifiable(List<?> list) {
        expectedException.expect(UnsupportedOperationException.class);
        list.clear();
    }

}
