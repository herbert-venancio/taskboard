package objective.taskboard.auth;

import static objective.taskboard.auth.authorizer.permission.ImpersonatePermission.IMPERSONATE_HEADER;

class ImpersonateException extends Exception {

    private static final long serialVersionUID = 1L;

    private ImpersonateException(String message) {
        super(message);
    }

    public static ImpersonateException blankHeaderValue() {
        return new ImpersonateException("\""+ IMPERSONATE_HEADER +"\" parameter cannot be blank.");
    }

    public static ImpersonateException userToImpersonateNotFound(String impersonateUsername) {
        return new ImpersonateException("User \"" + impersonateUsername + "\" not found.");
    }

}
