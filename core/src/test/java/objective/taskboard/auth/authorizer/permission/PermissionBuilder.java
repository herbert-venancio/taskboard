package objective.taskboard.auth.authorizer.permission;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

public abstract class PermissionBuilder {

    public static TargetlessPermissionBuilder targetlessPermission() {
        return new TargetlessPermissionBuilder();
    }
    public static TargetlessPermissionBuilder targetlessPermission(String name) {
        return targetlessPermission().withName(name);
    }

    public static TargettedPermissionBuilder<TargettedPermission> targettedPermission() {
        return new TargettedPermissionBuilder<TargettedPermission>() { };
    }

    public static TargettedPermissionBuilder<TargettedPermission> targettedPermission(String name) {
        return targettedPermission().withName(name);
    }

    public static TargettedPermissionBuilder<PerProjectPermission> perProjectPermission() {
        return new TargettedPermissionBuilder<PerProjectPermission>() { };
    }

    public static TargettedPermissionBuilder<PerProjectPermission> perProjectPermission(String name) {
        return perProjectPermission().withName(name);
    }


    private static abstract class BasePermissionBuilder<T extends Permission> {
        protected String name;

        @SuppressWarnings("unchecked")
		private Class<T> getPermissionClass() {
            return (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        }

        protected T build() {
            T permission = mock(getPermissionClass());
            when(permission.name()).thenReturn(name);
            return permission;
        }
    }

    public static class TargetlessPermissionBuilder extends BasePermissionBuilder<TargetlessPermission> {
        private boolean isAuthorized;

        @Override
        protected TargetlessPermission build() {
            TargetlessPermission permission = super.build();
            when(permission.isAuthorized()).thenReturn(isAuthorized);
            return permission;
        }

        public TargetlessPermissionBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public TargetlessPermission authorized() {
            isAuthorized = true;
            return build();
        }

        public TargetlessPermission notAuthorized() {
            isAuthorized = false;
            return build();
        }
    }


    public static class TargettedPermissionBuilder<T extends TargettedPermission> extends BasePermissionBuilder<T> {
        private List<String> applicableTargets = new ArrayList<>();

        @Override
        protected T build() {
            T permission = super.build();

            for (String target : applicableTargets)
                when(permission.isAuthorizedFor(target)).thenReturn(true);

            when(permission.applicableTargets()).thenReturn(new ArrayList<>(applicableTargets));

            return permission;
        }

        public TargettedPermissionBuilder<T> withName(String name) {
            this.name = name;
            return this;
        }

        public TargettedPermission applicableTo(String target, String... otherTargets) {
            applicableTargets.add(target);
            for (String t : otherTargets)
                applicableTargets.add(t);
            return build();
        }

        public TargettedPermission notApplicableToAnyTarget() {
            if (!applicableTargets.isEmpty())
                throw new IllegalStateException("A permission can't be applicable and not-applicable to the same target");
            return build();
        }
    }

}
