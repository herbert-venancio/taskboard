package objective.taskboard.auth.authorizer.permission;

import static objective.taskboard.auth.LoggedUserDetails.JiraRole.PROJECT_ADMINISTRATORS;
import static objective.taskboard.auth.authorizer.Permissions.SIZING_IMPORT_VIEW;

import org.springframework.stereotype.Service;

import objective.taskboard.auth.LoggedUserDetails;

@Service
public class SizingImportViewPermission extends AnyProjectPermission {

    public SizingImportViewPermission(LoggedUserDetails loggedUserDetails) {
        super(SIZING_IMPORT_VIEW, loggedUserDetails, PROJECT_ADMINISTRATORS);
    }

}
