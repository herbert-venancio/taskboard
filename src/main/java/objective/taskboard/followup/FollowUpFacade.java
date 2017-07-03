package objective.taskboard.followup;

import objective.taskboard.issueBuffer.IssueBufferState;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Created by herbert on 03/07/17.
 */
public interface FollowUpFacade {

    FollowUpGenerator getGenerator();

    void updateTemplate(MultipartFile file) throws IOException;

    IssueBufferState getFollowupState();
}
