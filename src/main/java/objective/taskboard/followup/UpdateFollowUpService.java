package objective.taskboard.followup;

import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by herbert on 30/06/17.
 */
public interface UpdateFollowUpService {

    Path decompressTemplate(File template) throws IOException;

    void validateTemplate(Path decompressed);

    void updateFromJiraTemplate(Path decompressed, Path fromJiraTemplate) throws IOException;

    void updateSharedStrings(Path decompressed, Path sharedStringsTemplate) throws IOException;

    void deleteFilesThatAreGenerated(Path decompressed) throws IOException;

    class InvalidTemplateException extends RuntimeException {
        public InvalidTemplateException() {
        }
        public InvalidTemplateException(Exception e) {
            super(e);
        }
    }
}
