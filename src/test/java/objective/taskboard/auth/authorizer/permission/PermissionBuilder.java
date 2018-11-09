package objective.taskboard.auth.authorizer.permission;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

public class PermissionBuilder {

    private PermissionMockDto dto = new PermissionMockDto();

    public static PermissionBuilder permission() {
        return new PermissionBuilder();
    }

    public PermissionBuilder withName(String name) {
        dto.name = name;
        return this;
    }

    public PermissionBuilder withIsAuthorized(boolean isAuthorized) {
        dto.isAuthorized = isAuthorized;
        return this;
    }

    public PermissionBuilder withApplicableTargets(String... applicableTargets) {
        dto.applicableTargets = asList(applicableTargets);
        return this;
    }

    public TargetlessPermissionMock asTargetless() {
        return new TargetlessPermissionMock(dto);
    }

    public TargettedPermissionMock asTargetted() {
        return new TargettedPermissionMock(dto);
    }

    public PerProjectPermission asPerProjectPermission() {
        PerProjectPermission permission = mock(PerProjectPermission.class);
        when(permission.name()).thenReturn(dto.name);
        when(permission.applicableTargets()).thenReturn(dto.applicableTargets);
        return permission;
    }

    private static class PermissionMockDto  {
        protected String name;
        protected boolean isAuthorized;
        protected List<String> applicableTargets;
        public String name() {
            return this.name;
        }
    }

    public static class TargetlessPermissionMock extends PermissionMockDto implements TargetlessPermission {
        public TargetlessPermissionMock(PermissionMockDto dto) {
            if (dto.applicableTargets != null)
                throw new IllegalArgumentException("TargettedPermission doesn't accept applicableTargets.");

            this.name = dto.name;
            this.isAuthorized = dto.isAuthorized;
        }

        @Override
        public boolean isAuthorized() {
            return isAuthorized;
        }
    }

    public static class TargettedPermissionMock extends PermissionMockDto implements TargettedPermission {
        public TargettedPermissionMock(PermissionMockDto dto) {
            this.name = dto.name;
            this.isAuthorized = dto.isAuthorized;
            this.applicableTargets = dto.applicableTargets;
        }

        @Override
        public boolean isAuthorizedFor(String target) {
            return isAuthorized;
        }

        @Override
        public List<String> applicableTargets() {
            return applicableTargets;
        }
    }

}
