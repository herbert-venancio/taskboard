package objective.taskboard.sizingImport;

import java.util.List;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import objective.taskboard.auth.CredentialsHolder;
import objective.taskboard.sizingImport.SizingImporter.SizingImporterListener;

class SizingImporterSocketStatusEmmiter implements SizingImporterListener {
    
    private static final String TOPIC_DESTINATION = "/topic/sizing-import/status";

    private final SimpMessagingTemplate messagingTemplate;
    
    private int linesToImportCount = 0;
    private int importedLinesCount = 0;
    private int failedLinesCount = 0;


    public SizingImporterSocketStatusEmmiter(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void onImportStarted(int totalLinesCount, int linesToImportCount) {
        this.linesToImportCount = linesToImportCount;
        emmitStatus();
    }

    @Override
    public void onLineImportStarted(SizingImportLine line) {
    }
    
    @Override
    public void onLineImportFinished(SizingImportLine line) {
        this.importedLinesCount++;
        emmitStatus();
    }

    @Override
    public void onLineError(SizingImportLine line, List<String> errorMessages) {
        this.failedLinesCount++;
        emmitStatus();
    }

    @Override
    public void onImportFinished() {
    }

    private void emmitStatus() {
        String receiver = CredentialsHolder.username();
        SizingImportStatus status = new SizingImportStatus(linesToImportCount, importedLinesCount, failedLinesCount);
        
        messagingTemplate.convertAndSendToUser(receiver, TOPIC_DESTINATION, status);
    }

    protected static class SizingImportStatus {
        private final int totalLines;
        private final int importedLines;
        private final int failedLines;

        public SizingImportStatus(int totalLines, int importedLines, int failedLines) {
            this.totalLines = totalLines;
            this.importedLines = importedLines;
            this.failedLines = failedLines;
        }

        public int getTotalLines() {
            return totalLines;
        }
        
        public int getImportedLines() {
            return importedLines;
        }
        
        public int getFailedLines() {
            return failedLines;
        }
    }
}
