package objective.taskboard.extension;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import objective.taskboard.auth.authorizer.permission.SizingImportViewPermission;

@Component
public class SizingImportToolbarItem implements ApplicationToolbarItem {
    @Autowired
    private SizingImportViewPermission permission;
    
    @Override
    public String getButtonId() {
        return "sizing-button";
    }    

    @Override
    public String getIconName() {
        return "save";
    }

    @Override
    public String getOnClickLink() {
        return "#/sizing-import/open";
    }

    @Override
    public String getIconDisplayName() {
        return "Import Sizing Spreadsheet";
    }
    
    @Override
    public boolean isVisible() {
        return permission.isAuthorized();
    }
}
