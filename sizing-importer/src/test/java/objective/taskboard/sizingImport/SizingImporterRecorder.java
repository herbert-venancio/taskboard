package objective.taskboard.sizingImport;

import static java.util.Arrays.asList;
import static objective.taskboard.testUtils.AssertUtils.collectionToString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.springframework.util.StringUtils.hasText;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

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

    public AssertImportEvents then() {
        return new AssertImportEvents(this);
    }

    public static class AssertImportEvents {

        private final SizingImporterRecorder recorder;

        private AssertImportEvents(SizingImporterRecorder recorder) {
            this.recorder = recorder;
        }

        public AssertImportEvents assertThatLinesToImportIs(int count) {
            assertThat(recorder.events)
                    .first()
                    .satisfies(line ->
                            assertThat(line).containsIgnoringCase("lines to import: " + count));
            return this;
        }

        public AssertImportEvents assertThatImportFinished() {
            assertThat(recorder.events)
                    .last()
                    .satisfies(line ->
                            assertThat(line).containsIgnoringCase("Import finished"));
            return this;
        }

        public AssertImportEvents assertThatContainsError(String error) {
            assertThat(recorder.events)
                    .anySatisfy(line ->
                            assertThat(line).containsIgnoringCase(error));
            return this;
        }

        public AssertImportEvents assertThatLineWithErrorAt(int expectedLineError) {
            String errorLine = recorder.events
                    .stream()
                    .map(f -> StringUtils.substringAfter(f, "Line error - Row index: "))
                    .map(f -> StringUtils.substringBefore(f, " |"))
                    .map(String::trim)
                    .filter(f -> !f.isEmpty())
                    .findFirst().orElse("");

            int errorNumber = 0;
            if (hasText(errorLine)) {
                int errorNumberIndex = Integer.parseInt(errorLine);
                errorNumber = errorNumberIndex + 1;
            }
            assertEquals(expectedLineError, errorNumber);
            return this;
        }

        public AssertImportEvents assertThatImportedIssue(String issueKey) {
            return assertThatImportedIssues(issueKey);
        }

        public AssertImportEvents assertThatImportedIssues(String... issueKey) {
            assertThat(recorder.events
                    .stream()
                    .map(e -> StringUtils.substringAfter(e, "issue key: "))
                    .filter(e -> !e.isEmpty())
            ).containsAll(asList(issueKey));
            return this;
        }
    }
}
