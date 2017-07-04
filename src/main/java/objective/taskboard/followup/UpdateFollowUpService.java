package objective.taskboard.followup;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public interface UpdateFollowUpService {

    /**
     * Decompress a .xlsx or .xlsm file to a temporary folder
     * @param template
     * @return
     * @throws IOException
     */
    Path decompressTemplate(File template) throws IOException;
    Path decompressTemplate(InputStream input) throws IOException;

    /**
     * Validates that template can be used as Follow-up Spreadsheet template
     * @param decompressed
     */
    void validateTemplate(Path decompressed);

    /**
     * Update 'From Jira' tab from spreadsheet template
     * @param decompressed
     * @param fromJiraTemplate
     * @throws IOException
     */
    void updateFromJiraTemplate(Path decompressed, Path fromJiraTemplate) throws IOException;

    /**
     * Update 'sharedStrings.xml' with data from template
     * @param decompressed
     * @param sharedStringsTemplate
     * @throws IOException
     */
    void updateSharedStrings(Path decompressed, Path sharedStringsTemplate) throws IOException;

    /**
     * Delete unused files since they are generated with data from jira/taskboard/follow-up.
     * @param decompressed
     * @throws IOException
     */
    void deleteGeneratedFiles(Path decompressed) throws IOException;

    /**
     * Compressed files to Follow-up template xlsm file
     * @param decompressed
     * @param pathFollowupXLSM
     * @return
     */
    Path compressTemplate(Path decompressed, Path pathFollowupXLSM) throws IOException;

    class InvalidTemplateException extends RuntimeException {
        private static final String MESSAGE = "Invalid Template";
        public InvalidTemplateException() {
            super(MESSAGE);
        }
        public InvalidTemplateException(Exception e) {
            super(MESSAGE, e);
        }
    }
}
