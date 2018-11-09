package objective.taskboard.auth.authorizer.permission;

import static objective.taskboard.auth.authorizer.Permissions.TASKBOARD_ADMINISTRATION;

import org.springframework.stereotype.Service;

import objective.taskboard.auth.LoggedUserDetails;

@Service
public class TaskboardAdministrationPermission extends BaseTargetlessPermission {

    public TaskboardAdministrationPermission(LoggedUserDetails loggedUserDetails) {
        super(TASKBOARD_ADMINISTRATION, loggedUserDetails);
    }

    @Override
    protected boolean isAuthorized(LoggedUserDetails loggedUserDetails) {
        return loggedUserDetails.isAdmin();
    }

}
