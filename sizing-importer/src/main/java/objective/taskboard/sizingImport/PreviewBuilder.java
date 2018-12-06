package objective.taskboard.sizingImport;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static objective.taskboard.sizingImport.SizingImportConfig.SHEET_COST;
import static objective.taskboard.sizingImport.SizingImportConfig.SHEET_SCOPE;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import objective.taskboard.google.SpreadsheetUtils;
import objective.taskboard.sizingImport.cost.SizingImportLineCost;

class PreviewBuilder {
    
    private List<SizingImportLineScope> scopeLines = Collections.emptyList();
    private List<SizingImportLineCost> costLines = Collections.emptyList();
    private int linesLimit = 10;

    public PreviewBuilder setScopeLines(List<SizingImportLineScope> scopeLines) {
        this.scopeLines = scopeLines;
        return this;
    }

    public PreviewBuilder setCostLines(List<SizingImportLineCost> costLines) {
        this.costLines = costLines;
        return this;
    }

    public PreviewBuilder setLinesLimit(int linesLimit) {
        this.linesLimit = linesLimit;
        return this;
    }
    
    public ImportPreview build() {
        ImportSheetPreview scopePreview = new ImportSheetPreview(SHEET_SCOPE, scopeLines, linesLimit);

        if (costLines.isEmpty())
            return new ImportPreview(scopePreview, Optional.empty());

        ImportSheetPreview costPreview = new ImportSheetPreview(SHEET_COST, costLines, linesLimit);
        return new ImportPreview(scopePreview, Optional.of(costPreview));
    }
    
    static class ImportPreview {
        private final ImportSheetPreview scopePreview;
        private final Optional<ImportSheetPreview> costPreview;

        public ImportPreview(ImportSheetPreview scopePreview, Optional<ImportSheetPreview> costPreview) {
            this.scopePreview = scopePreview;
            this.costPreview = costPreview;
        }

        public ImportSheetPreview getScopePreview() {
            return scopePreview;
        }

        public Optional<ImportSheetPreview> getCostPreview() {
            return costPreview;
        }
    }

    static class ImportSheetPreview {
        private final String sheetTitle;
        private final List<String> headers;
        private final List<List<String>> rows;
        private final int totalLinesCount;

        public ImportSheetPreview(String sheetTitle, List<? extends SizingImportLine> lines, int linesLimit) {
            this.sheetTitle = sheetTitle;

            List<SheetColumn> allColumns = lines.stream()
                    .flatMap(line -> line.getImportValues().stream().map(value -> value.getColumn()))
                    .distinct()
                    .filter(c -> c.getDefinition().isVisibleInPreview())
                    .sorted(comparing(SheetColumn::getLetter, SpreadsheetUtils.COLUMN_LETTER_COMPARATOR))
                    .collect(toList());

            this.headers = allColumns.stream().map(c -> c.getDefinition().getName()).collect(toList());
            this.rows = lines.stream()
                    .limit(linesLimit)
                    .map(line -> {
                        return allColumns.stream()
                                .map(col -> line.getValue(col.getDefinition(), ""))
                                .collect(toList());
                    })
                    .collect(toList());
            this.totalLinesCount = lines.size();
        }

        public String getSheetTitle() {
            return sheetTitle;
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
