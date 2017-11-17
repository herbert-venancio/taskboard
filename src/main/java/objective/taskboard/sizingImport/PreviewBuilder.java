package objective.taskboard.sizingImport;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.List;

import objective.taskboard.google.SpreadsheetUtils;

class PreviewBuilder {
    
    private List<SizingImportLine> data = Collections.emptyList();
    private int linesLimit = 10;

    public PreviewBuilder setData(List<SizingImportLine> data) {
        this.data = data;
        return this;
    }

    public PreviewBuilder setLinesLimit(int linesLimit) {
        this.linesLimit = linesLimit;
        return this;
    }
    
    public ImportPreview build() {
        List<SheetColumn> allColumns = data.stream()
                .flatMap(line -> line.getImportValues().stream().map(value -> value.getColumn()))
                .distinct()
                .filter(c -> c.getDefinition().isVisibleInPreview())
                .sorted(comparing(SheetColumn::getLetter, SpreadsheetUtils.COLUMN_LETTER_COMPARATOR))
                .collect(toList());
        
        List<String> headers = allColumns.stream().map(c -> c.getDefinition().getName()).collect(toList());

        List<List<String>> rows = data.stream()
                .limit(linesLimit)
                .map(line -> {
                    return allColumns.stream()
                            .map(col -> line.getValue(col.getDefinition(), ""))
                            .collect(toList());
                })
                .collect(toList());


        return new ImportPreview(headers, rows, data.size());
    }
    
    static class ImportPreview {
        private final List<String> headers;
        private final List<List<String>> rows;
        private final int totalLinesCount;

        public ImportPreview(List<String> headers, List<List<String>> rows, int totalLinesCount) {
            this.headers = headers;
            this.rows = rows;
            this.totalLinesCount = totalLinesCount;
        }
        
        public List<String> getHeaders() {
            return headers;
        }
        
        public List<List<String>> getRows() {
            return rows;
        }
        
        public int getTotalLinesCount() {
            return totalLinesCount;
        }
    }
}
