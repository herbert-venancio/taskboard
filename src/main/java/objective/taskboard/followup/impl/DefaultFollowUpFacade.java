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
    public void updateTemplate(MultipartFile file) {
        try {
            Path followUpTemplateCandidate = updateFollowUpService.decompressTemplate(file.getInputStream());
            try {
                updateFollowUpService.validateTemplate(followUpTemplateCandidate);
                Path jiraTab = Files.createTempFile("sheet-template", ".xml");
                updateFollowUpService.updateFromJiraTemplate(followUpTemplateCandidate, jiraTab);
                Path sharedStrings = Files.createTempFile("shared-strings", ".xml");
                updateFollowUpService.updateSharedStrings(followUpTemplateCandidate, sharedStrings);
                updateFollowUpService.deleteFilesThatAreGenerated(followUpTemplateCandidate);
                Path pathFollowupXLSM = Files.createTempFile("Followup", ".xlsm");
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
                FileUtils.deleteQuietly(followUpTemplateCandidate.toFile());
                throw e;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
