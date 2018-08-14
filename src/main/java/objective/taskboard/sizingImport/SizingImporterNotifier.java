package objective.taskboard.sizingImport;

import java.util.ArrayList;
import java.util.List;

public class SizingImporterNotifier {

    private final List<SizingImporterListener> listeners = new ArrayList<>();

    public void addListener(SizingImporterListener listener) {
        listeners.add(listener);
    }

    public void notifySheetImportStarted(int totalLinesCount, int linesToImportCount) {
        listeners.stream().forEach(l -> l.onSheetImportStarted(totalLinesCount, linesToImportCount));
    }

    public void notifyLineImportStarted(SizingImportLine line) {
        listeners.stream().forEach(l -> l.onLineImportStarted(line));
    }

    public void notifyLineImportFinished(SizingImportLine line, String issueKey) {
        listeners.stream().forEach(l -> l.onLineImportFinished(line, issueKey));
    }

    public void notifyLineError(SizingImportLine line, List<String> errorMessages) {
        listeners.stream().forEach(l -> l.onLineError(line, errorMessages));
    }

    public void notifyImportFinished() {
        listeners.stream().forEach(l -> l.onSheetImportFinished());
    }

    public interface SizingImporterListener {
        void onSheetImportStarted(int totalLinesCount, int linesToImportCount);
        void onLineImportStarted(SizingImportLine line);
        void onLineImportFinished(SizingImportLine line, String issueKey);
        void onLineError(SizingImportLine line, List<String> errorMessages);
        void onSheetImportFinished();
    }
}
