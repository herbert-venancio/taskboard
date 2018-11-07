package objective.taskboard.auth.authorizer.permission;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import objective.taskboard.auth.LoggedUserDetails;

public abstract class BasePermission implements Permission {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasePermission.class);

    private final String name;
    private final LoggedUserDetails loggedUserDetails;

    public BasePermission(String name, LoggedUserDetails loggedUserDetails) {
        this.name = name;
        this.loggedUserDetails = loggedUserDetails;
    }

    @Override
    public String name() {
        return name;
    }

    public boolean isAuthorized() {
        boolean isAuthorized = isAuthorized(PermissionContext.empty());
        LOGGER.debug("Authorize permission \"{}\": {}", name(), isAuthorized);
        return isAuthorized;
    }

    public boolean isAuthorizedFor(String target) {
        boolean isAuthorized = isAuthorized(new PermissionContext(target));
        LOGGER.debug("Authorize permission \"{}\" for target \"{}\": {}", name(), target, isAuthorized);
        return isAuthorized;
    }

    protected LoggedUserDetails getLoggedUser() {
    	return loggedUserDetails;
    }

    @Override
    public String toString() {
        return name;
    }

}
