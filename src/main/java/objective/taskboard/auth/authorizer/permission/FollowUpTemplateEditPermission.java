package objective.taskboard.auth.authorizer.permission;

import static objective.taskboard.auth.LoggedUserDetails.JiraRole.PROJECT_ADMINISTRATORS;
import static objective.taskboard.auth.authorizer.Permissions.FOLLOWUP_TEMPLATE_EDIT;

import org.springframework.stereotype.Service;

@Service
public class FollowUpTemplateEditPermission extends AnyProjectPermission {

    public FollowUpTemplateEditPermission() {
        super(FOLLOWUP_TEMPLATE_EDIT, PROJECT_ADMINISTRATORS);
    }

}
