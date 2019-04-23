package objective.taskboard.sizingImport;

import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import objective.taskboard.auth.CredentialsHolder;
import objective.taskboard.sizingImport.SizingSheetImporterNotifier.SizingSheetImporterListener;

class SizingImporterSocketStatusEmmiter implements SizingSheetImporterListener {
    
    private static final String TOPIC_DESTINATION = "/topic/sizing-import/status";

    private final SimpMessagingTemplate messagingTemplate;
    
    private int linesToImportCount = 0;
    private int importedLinesCount = 0;
    private int failedLinesCount = 0;

    private SizingImportSheetStatus currentSheetStatus;
    private List<SizingImportSheetStatus> sheetsStatus = new ArrayList<>();

    public SizingImporterSocketStatusEmmiter(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void onImportStarted(int linesToImportCount) {
        this.linesToImportCount = linesToImportCount;
        emmitStatus();
    }

    @Override
    public void onSheetImportStarted(String sheetTitle, int totalLinesCount, int linesToImportCount) {
        currentSheetStatus = new SizingImportSheetStatus(sheetTitle, totalLinesCount, linesToImportCount);
        sheetsStatus.add(currentSheetStatus);
        emmitStatus();
    }

    @Override
    public void onLineImportStarted(SizingImportLine line) {
    }
    
    @Override
    public void onLineImportFinished(SizingImportLine line, String issueKey) {
        importedLinesCount++;
        currentSheetStatus.importedLines++;
        emmitStatus();
    }

    @Override
    public void onLineError(SizingImportLine line, List<String> errorMessages) {
        failedLinesCount++;
        currentSheetStatus.failedLines++;
        emmitStatus();
    }

    @Override
    public void onSheetImportFinished() {
    }

    private void emmitStatus() {
        String receiver = CredentialsHolder.defineUsername();
        SizingImportStatusDto status = new SizingImportStatusDto(linesToImportCount, importedLinesCount, failedLinesCount, ofNullable(currentSheetStatus), sheetsStatus);
        
        messagingTemplate.convertAndSendToUser(receiver, TOPIC_DESTINATION, status);
    }

    protected static class SizingImportStatusDto {
        public final int totalLines;
        public final int importedLines;
        public final int failedLines;
        public final SizingImportSheetStatusDto currentSheetStatus;
        public final List<SizingImportSheetStatusDto> sheetsStatus;

        public SizingImportStatusDto(
                int totalLines,
                int importedLines,
                int failedLines,
                Optional<SizingImportSheetStatus> currentSheetStatus,
                List<SizingImportSheetStatus> sheetsStatus) {

            this.totalLines = totalLines;
            this.importedLines = importedLines;
            this.failedLines = failedLines;
            this.currentSheetStatus = currentSheetStatus.map(sheetStatus -> new SizingImportSheetStatusDto(sheetStatus)).orElse(null);
            this.sheetsStatus = sheetsStatus.stream()
                .map(sheet -> new SizingImportSheetStatusDto(sheet))
                .collect(Collectors.toList());
        }
    }

    protected static class SizingImportSheetStatusDto {
        public final String sheetTitle;
        public final int totalLines;
        public final int linesToImport;
        public final int importedLines;
        public final int failedLines;

        public SizingImportSheetStatusDto(SizingImportSheetStatus object) {
            this.sheetTitle = object.sheetTitle;
            this.totalLines = object.totalLines;
            this.linesToImport = object.linesToImport;
            this.importedLines = object.importedLines;
            this.failedLines = object.failedLines;
        }
    }

    private static class SizingImportSheetStatus {
        private final String sheetTitle;
        private final int totalLines;
        private final int linesToImport;
        private int importedLines;
        private int failedLines;

        public SizingImportSheetStatus(String sheetTitle, int totalLines, int linesToImport) {
            this.sheetTitle = sheetTitle;
            this.totalLines = totalLines;
            this.linesToImport = linesToImport;
            this.importedLines = 0;
            this.failedLines = 0;
        }
    }
}
