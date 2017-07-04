package objective.taskboard.followup.impl;

import objective.taskboard.followup.*;
import objective.taskboard.issueBuffer.IssueBufferState;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by herbert on 03/07/17.
 */
@Service
public class DefaultFollowUpFacade implements FollowUpFacade {

    @Autowired
    private FollowUpTemplateStorage followUpTemplateStorage;

    @Autowired
    private FollowupDataProvider provider;

    @Autowired
    private UpdateFollowUpService updateFollowUpService;

    @Override
    public FollowUpGenerator getGenerator() {
        return new FollowUpGenerator(provider);
    }

    @Override
    public IssueBufferState getFollowupState() {
        return provider.getFollowupState();
    }

    @Override
    public void updateTemplate(MultipartFile file) throws IOException {
        Path followUpTemplateCandidate = updateFollowUpService.decompressTemplate(file.getInputStream());
        Path jiraTab = Files.createTempFile("sheet-template", ".xml");
        Path sharedStrings = Files.createTempFile("shared-strings", ".xml");
        Path pathFollowupXLSM = Files.createTempFile("Followup", ".xlsm");
        try {
            updateFollowUpService.validateTemplate(followUpTemplateCandidate);
            updateFollowUpService.updateFromJiraTemplate(followUpTemplateCandidate, jiraTab);
            updateFollowUpService.updateSharedStrings(followUpTemplateCandidate, sharedStrings);
            updateFollowUpService.deleteFilesThatAreGenerated(followUpTemplateCandidate);
            updateFollowUpService.compressTemplate(followUpTemplateCandidate, pathFollowupXLSM);

            FollowUpTemplate defaultTemplate = followUpTemplateStorage.getDefaultTemplate();
            FollowUpTemplate template = new FollowUpTemplate(
                    defaultTemplate.getPathSharedStringsInitial()
                    , sharedStrings.toString()
                    , defaultTemplate.getPathSISharedStringsTemplate()
                    , jiraTab.toString()
                    , defaultTemplate.getPathSheet7RowTemplate()
                    , pathFollowupXLSM.toString()
            );
            followUpTemplateStorage.updateTemplate(template);
        } catch (Exception e) {
            FileUtils.deleteQuietly(jiraTab.toFile());
            FileUtils.deleteQuietly(sharedStrings.toFile());
            FileUtils.deleteQuietly(pathFollowupXLSM.toFile());
            throw e;
        } finally {
            FileUtils.deleteQuietly(followUpTemplateCandidate.toFile());
        }
    }
}
