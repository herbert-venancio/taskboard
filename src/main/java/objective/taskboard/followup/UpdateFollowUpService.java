package objective.taskboard.followup;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * 1. Ensure 'From Jira' tab has no data at all
 * 2. unzip the template
 * 3. run the following command:
 *   - xmllint --format xl/worksheets/sheet7.xml > sheet7-reformatted.xml
 * 4. open sheet7-reformatted.xml and copy the contents of tag <row r="1"..></row> over the same row on template src/main/resources/followup-template/sheet7-template.xml
 * 5. remove xl/worksheets/sheet7.xml and the reformatted file
 * 6. copy the contents of ./xl/sharedStrings.xml and execute the following command:
 *   - xmllint --format sharedStrings.xml > ./src/main/resources/followup-template/sharedStrings-initial.xml
 * 7. remove sharedStrings.xml
 * 8. zip the contents again into ./src/main/resources/followup-template/Followup-template.xlsm
 * 9. And you're done.
 *
 * Created by herbert on 30/06/17.
 */
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
     * Validates that template can be used as Follo-up Spreadsheet template
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
    void deleteFilesThatAreGenerated(Path decompressed) throws IOException;

    /**
     * Compressed files to Follow-up template xlsm file
     * @param decompressed
     * @param pathFollowupXLSM
     * @return
     */
    Path compressTemplate(Path decompressed, Path pathFollowupXLSM) throws IOException;

    class InvalidTemplateException extends RuntimeException {
        public InvalidTemplateException() {
        }
        public InvalidTemplateException(Exception e) {
            super(e);
        }
    }
}
