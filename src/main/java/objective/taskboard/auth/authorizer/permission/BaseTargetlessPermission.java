package objective.taskboard.auth.authorizer.permission;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import objective.taskboard.auth.LoggedUserDetails;

public abstract class BaseTargetlessPermission extends BasePermission implements TargetlessPermission {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseTargetlessPermission.class);

    protected BaseTargetlessPermission(String name, LoggedUserDetails loggedUserDetails) {
        super(name, loggedUserDetails);
    }

    public boolean isAuthorized() {
        boolean isAuthorized = isAuthorized(getLoggedUser());
        LOGGER.debug("Authorize permission \"{}\": {}", name(), isAuthorized);
        return isAuthorized;
    }

    protected abstract boolean isAuthorized(LoggedUserDetails loggedUserDetails);

}
