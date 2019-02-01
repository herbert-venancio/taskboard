package objective.taskboard.extension;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import objective.taskboard.auth.authorizer.permission.ProjectDashboardViewPermission;

@Component
public class DashboardToolbarItem implements ApplicationToolbarItem {
    @Autowired
    private ProjectDashboardViewPermission permission;
    
    @Override
    public String getButtonId() {
        return "dashboard-button";
    }    

    @Override
    public String getIconName() {
        return "taskboard-icons:dashboard";
    }
    
    @Override
    public String getIconDisplayName() {
        return "Followup Dashboard";
    }

    @Override
    public String getOnClickLink() {
        return "/followup-dashboard/";
    }

    @Override
    public boolean isVisible() {
        return permission.isAuthorized();
    }
}
