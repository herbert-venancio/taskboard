package objective.taskboard.auth.authorizer.permission;

import static objective.taskboard.auth.LoggedUserDetails.JiraRole.PROJECT_ADMINISTRATORS;
import static objective.taskboard.auth.authorizer.Permissions.SIZING_IMPORT_VIEW;

import org.springframework.stereotype.Service;

@Service
public class SizingImportViewPermission extends AnyProjectPermission {

    public SizingImportViewPermission() {
        super(SIZING_IMPORT_VIEW, PROJECT_ADMINISTRATORS);
    }

}
