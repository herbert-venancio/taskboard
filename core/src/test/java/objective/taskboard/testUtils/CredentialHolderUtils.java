package objective.taskboard.testUtils;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import objective.taskboard.auth.LoggedUserDetails;

public class CredentialHolderUtils {

    public static void mockLoggedUser(String username, String password) {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        doReturn(username).when(authentication).getName();
        doReturn(password).when(authentication).getCredentials();
        doReturn(true).when(authentication).isAuthenticated();
        doReturn(authentication).when(securityContext).getAuthentication();

        LoggedUserDetails loggedInUser = mock(LoggedUserDetails.class);
        doReturn(username).when(loggedInUser).defineUsername();
        doReturn(username).when(loggedInUser).getRealUsername();
        doReturn(Optional.empty()).when(loggedInUser).getImpersonateUsername();
        doReturn(loggedInUser).when(authentication).getPrincipal();

        SecurityContextHolder.setContext(securityContext);
    }

}
