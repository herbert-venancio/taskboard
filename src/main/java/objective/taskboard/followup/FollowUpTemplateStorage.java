package objective.taskboard.followup;

/**
 * Created by herbert on 03/07/17.
 */
public interface FollowUpTemplateStorage {

    FollowUpTemplate getDefaultTemplate();

    FollowUpTemplate getTemplate();

    void updateTemplate(FollowUpTemplate template);

}
