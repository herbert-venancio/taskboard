package objective.taskboard.auth.authorizer.permission;

import objective.taskboard.auth.LoggedUserDetails;

public abstract class BasePermission implements Permission {

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
    
    protected LoggedUserDetails getLoggedUser() {
    	return loggedUserDetails;
    }

    @Override
    public String toString() {
        return name;
    }

}
