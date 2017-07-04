package objective.taskboard.followup;

public interface FollowUpTemplateStorage {

    FollowUpTemplate getDefaultTemplate();

    FollowUpTemplate getTemplate();

    void updateTemplate(FollowUpTemplate template);

}
