package objective.taskboard.auth.authorizer.permission;

public class PermissionContext {

    public final String target;

    public PermissionContext(String target) {
        this.target = target;
    }

    public static PermissionContext empty() {
        return new PermissionContext(null);
    }

    public boolean isEmpty() {
        return target == null;
    }

}
