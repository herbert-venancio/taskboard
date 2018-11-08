package objective.taskboard.auth.authorizer.permission;

public class PermissionContext {

    private static final PermissionContext EMPTY = new PermissionContext(null);

    public final String target;

    public PermissionContext(String target) {
        this.target = target;
    }

    public static PermissionContext empty() {
        return EMPTY;
    }

    public boolean isEmpty() {
        return target == null;
    }

}
