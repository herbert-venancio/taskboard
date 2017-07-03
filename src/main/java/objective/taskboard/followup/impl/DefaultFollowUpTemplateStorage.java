package objective.taskboard.followup.impl;

import objective.taskboard.followup.FollowUpTemplate;
import objective.taskboard.followup.FollowUpTemplateStorage;
import org.springframework.stereotype.Service;

/**
 * Created by herbert on 03/07/17.
 */
@Service
public class DefaultFollowUpTemplateStorage implements FollowUpTemplateStorage {

    private FollowUpTemplate template;

    @Override
    public FollowUpTemplate getDefaultTemplate() {
        return new FollowUpTemplate(
            resolve("followup-template/sharedStrings-initial.xml")
            , resolve("followup-template/sharedStrings-template.xml")
            , resolve("followup-template/sharedStrings-si-template.xml")
            , resolve("followup-template/sheet7-template.xml")
            , resolve("followup-template/sheet7-row-template.xml")
            , resolve("followup-template/Followup-template.xlsm")
        );
    }

    @Override
    public FollowUpTemplate getTemplate() {
        return template;
    }

    @Override
    public void updateTemplate(FollowUpTemplate template) {
        this.template = template;
    }

    // ---

    private static String resolve(String resourceName) {
        return DefaultFollowUpTemplateStorage.class.getClassLoader().getResource(resourceName).getFile();
    }
}
