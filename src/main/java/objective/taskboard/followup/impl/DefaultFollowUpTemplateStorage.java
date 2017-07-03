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
            "followup-template/sharedStrings-initial.xml"
            , "followup-template/sharedStrings-template.xml"
            , "followup-template/sharedStrings-si-template.xml"
            , "followup-template/sheet7-template.xml"
            , "followup-template/sheet7-row-template.xml"
            , "followup-template/Followup-template.xlsm"
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
}
