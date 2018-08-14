package objective.taskboard.sizingImport;

import static objective.taskboard.testUtils.AssertUtils.collectionToString;

import java.util.ArrayList;
import java.util.List;

import objective.taskboard.sizingImport.SizingSheetImporterNotifier.SizingSheetImporterListener;

public class SizingImporterRecorder implements SizingSheetImporterListener {
    private final List<String> events = new ArrayList<>();

    @Override
    public void onSheetImportStarted(String sheetTitle, int totalLinesCount, int linesToImportCount) {
        events.add("Import started - Total lines count: " + totalLinesCount + " | lines to import: " + linesToImportCount);
    }

    @Override
    public void onLineImportStarted(SizingImportLine line) {
        events.add("Line import started - Row index: " + line.getRowIndex());
    }

    @Override
    public void onLineImportFinished(SizingImportLine line, String issueKey) {
        events.add("Line import finished - Row index: " + line.getRowIndex() + " | issue key: " + issueKey);
    }

    @Override
    public void onLineError(SizingImportLine line, List<String> errorMessages) {
        events.add("Line error - Row index: " + line.getRowIndex() + " | errors: " + collectionToString(errorMessages, "; "));
    }

    @Override
    public void onSheetImportFinished() {
        events.add("Import finished");
    }

    public List<String> getEvents() {
        return events;
    }

}
