package objective.taskboard.extension;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import objective.taskboard.auth.authorizer.permission.FollowUpTemplateEditPermission;
import objective.taskboard.followup.FollowUpFacade;

@Component
public class FollowupToolbarItem implements ApplicationToolbarItem {
    @Autowired
    private FollowUpTemplateEditPermission permission;
    
    @Autowired
    private FollowUpFacade followupFacade;
    
    @Override
    public String getButtonId() {
        return "followup-button";
    }    

    @Override
    public String getIconName() {
        return "taskboard-icons:reports";
    }

    @Override
    public String getOnClickLink() {
        return "#/followup-report/open";
    }

    @Override
    public String getIconDisplayName() {
        return "Followup reports";
    }

    @Override
    public boolean isVisible() {
        return permission.isAuthorized() || followupFacade.getTemplatesForCurrentUser().size() > 0;
    }
}
