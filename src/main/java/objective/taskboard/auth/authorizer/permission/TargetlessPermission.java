package objective.taskboard.auth.authorizer.permission;

public interface TargetlessPermission extends Permission {

    boolean isAuthorized();

}
