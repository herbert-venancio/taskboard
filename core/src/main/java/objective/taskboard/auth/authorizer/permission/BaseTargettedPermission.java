package objective.taskboard.auth.authorizer.permission;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import objective.taskboard.auth.LoggedUserDetails;

public abstract class BaseTargettedPermission extends BasePermission implements TargettedPermission {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseTargettedPermission.class);

    protected BaseTargettedPermission(String name, LoggedUserDetails loggedUserDetails) {
        super(name, loggedUserDetails);
    }

    public boolean isAuthorizedFor(String target) {
        boolean isAuthorized = isAuthorized(getLoggedUser(), target);
        LOGGER.debug("Authorize permission \"{}\" for target \"{}\": {}", name(), target, isAuthorized);
        return isAuthorized;
    }

    protected abstract boolean isAuthorized(LoggedUserDetails loggedUserDetails, String target);

}
