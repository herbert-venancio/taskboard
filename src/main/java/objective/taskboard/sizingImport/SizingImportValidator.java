package objective.taskboard.sizingImport;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static objective.taskboard.sizingImport.SizingImportConfig.SHEET_TITLE;
import static org.apache.commons.lang3.StringUtils.join;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import objective.taskboard.google.GoogleApiService;
import objective.taskboard.google.SpreadsheetUtils;
import objective.taskboard.google.SpreadsheetsManager;
import objective.taskboard.google.SpreadsheetsManager.SpreadsheeNotFoundException;

@Component
class SizingImportValidator {
    
    private final SizingImportConfig config;
    private final GoogleApiService googleApiService;
    private final SheetColumnDefinitionProvider columnDefinitionProvider;

    @Autowired
    public SizingImportValidator(
            SizingImportConfig config, 
            GoogleApiService googleApiService, 
            JiraFacade jiraFacade, 
            SheetColumnDefinitionProvider columnDefinitionProvider) {

        this.config = config;
        this.googleApiService = googleApiService;
        this.columnDefinitionProvider = columnDefinitionProvider;
    }

    public ValidationResult validate(String projectKey, String spreadsheetId) {
        int headersRowIndex = config.getDataStartingRowIndex() - 1;
        SpreadsheetsManager spreadsheetsManager = googleApiService.buildSpreadsheetsManager();
        ValidationContext context = new ValidationContext(projectKey, spreadsheetId, headersRowIndex, spreadsheetsManager);

        return validateSpreadsheetExistence(context);
    }

    private ValidationResult validateSpreadsheetExistence(ValidationContext context) {
        List<String> sheetsTitles;

        try {
            sheetsTitles = context.spreadsheetsManager.getSheetsTitles(context.spreadsheetId);
        } catch (SpreadsheeNotFoundException ex) {//NOSONAR
            return ValidationResult.fail("Spreadsheet canot be found. Please check the URL.");
        }

        return validateSheetScopeExistence(new ValidationContextWithSpreadsheet(context, sheetsTitles));
    }
    
    private ValidationResult validateSheetScopeExistence(ValidationContextWithSpreadsheet context) {
        if (!context.sheetsTitles.contains(SHEET_TITLE)) {
            return ValidationResult.fail(
                    "Invalid spreadsheet format: Specified URL should contain a sheet with title “" + SHEET_TITLE + "”.", 
                    "Found sheets: " + join(context.sheetsTitles, ", ")  + ".");
        }
        
        return validateDataStartingRow(context);
    }
    
    private ValidationResult validateDataStartingRow(ValidationContextWithSpreadsheet context) {
        List<List<Object>> rows = context.spreadsheetsManager.readRange(context.spreadsheetId, "'" + SHEET_TITLE + "'");

        if (config.getDataStartingRowIndex() >= rows.size()) 
            return invalidDataStartingRow(context);
        
        List<Object> headersCells = rows.get(context.headersRowIndex);
        if (headersCells == null || headersCells.isEmpty())
            return invalidDataStartingRow(context);

        List<String> spreadsheetHeaders = headersCells.stream()
                .map(h -> Objects.toString(h, ""))
                .map(StringUtils::trim)
                .map(String::toLowerCase)
                .collect(toList());
        
        List<String> staticColumns = columnDefinitionProvider.getStaticMappings().stream()
                .map(md -> md.getColumnDefinition().getName().toLowerCase())
                .collect(toList());
        
        Optional<String> anyCorrectHeader = spreadsheetHeaders.stream()
                .filter(staticColumns::contains)
                .findAny();
        
        if (!anyCorrectHeader.isPresent())
            return invalidDataStartingRow(context);

        List<StaticColumnOccurrenceReport> occurenceReport = columnDefinitionProvider.getStaticMappings().stream()
                .map(md -> new StaticColumnOccurrenceReport(md, spreadsheetHeaders))
                .collect(toList());
        
        return validateStaticColumnsExistence(new ValidationContextWithReport(context, occurenceReport));
    }

    private ValidationResult invalidDataStartingRow(ValidationContext context) {
        int headerRowNumber = context.headersRowIndex + 1;

        String expectedHeaders = columnDefinitionProvider.getStaticMappings().stream()
                .map(md -> md.getColumnDefinition().getName())
                .limit(3)
                .collect(joining(", "));

        String message = format(
                "Invalid spreadsheet format: Row %s of sheet “%s” should contain the headers (e.g. %s).", 
                headerRowNumber, SHEET_TITLE, expectedHeaders);
        
        String detail = format("Activities to import should start at row %s.", config.getDataStartingRowNumber());
        
        return ValidationResult.fail(message, detail);
    }
    
    private ValidationResult validateStaticColumnsExistence(ValidationContextWithReport context) {
        List<StaticColumnOccurrenceReport> missingColumnsReport = context.occurenceReport.stream()
                .filter(StaticColumnOccurrenceReport::isMissing)
                .collect(toList());

        if (!missingColumnsReport.isEmpty()) {
            return ValidationResult.fail(
                    "Invalid spreadsheet format: Missing required columns.", 
                    missingColumnsReport.stream()
                        .map(cr -> format("“%s” column should be placed at position “%s”", cr.getColumnName(), cr.getColumnLetter()))
                        .collect(joining(", ")) + ".");
        }

        return validateStaticColumnsUniqueness(context);
    }
    
    private ValidationResult validateStaticColumnsUniqueness(ValidationContextWithReport context) {
        List<StaticColumnOccurrenceReport> duplicateColumnsReport = context.occurenceReport.stream()
                .filter(StaticColumnOccurrenceReport::isDuplicate)
                .collect(toList());
        
        if (!duplicateColumnsReport.isEmpty()) {
            return ValidationResult.fail(
                    "Invalid spreadsheet format: Duplicate columns found.", 
                    duplicateColumnsReport.stream()
                        .map(cr -> format("“%s” column is showing up in positions “%s”", cr.getColumnName(), join(cr.getOccurrenceLetters(), "”/“")))
                        .collect(joining(", ")) + ".");
        }
        
        return validateStaticColumnsPosition(context);
    }

    private ValidationResult validateStaticColumnsPosition(ValidationContextWithReport context) {
        List<StaticColumnOccurrenceReport> wronglyPositionedColumnsReport = context.occurenceReport.stream()
                .filter(StaticColumnOccurrenceReport::isWronglyPositioned)
                .collect(toList());
        
        if (!wronglyPositionedColumnsReport.isEmpty()) {
            return ValidationResult.fail(
                    "Invalid spreadsheet format: Incorrectly positioned columns.", 
                    wronglyPositionedColumnsReport.stream()
                        .map(cr -> format("“%s” column should be moved to position “%s”", cr.getColumnName(), cr.getColumnLetter()))
                        .collect(joining(", ")) + ".");
        }
        
        return ValidationResult.success();
    }

    private static class StaticColumnOccurrenceReport {
        private final StaticMappingDefinition mappingDefinition;
        private final List<String> occurrenceLetters = new ArrayList<>();

        public StaticColumnOccurrenceReport(StaticMappingDefinition mappingDefinition, List<String> spreadsheetHeaders) {
            this.mappingDefinition = mappingDefinition;
            
            for (int i = 0; i < spreadsheetHeaders.size(); i++) {
                String header = spreadsheetHeaders.get(i);
                
                if (mappingDefinition.getColumnDefinition().getName().equalsIgnoreCase(header))
                    occurrenceLetters.add(SpreadsheetUtils.columnIndexToLetter(i));
            }
        }
        
        public String getColumnName() {
            return mappingDefinition.getColumnDefinition().getName();
        }
        
        public String getColumnLetter() {
            return mappingDefinition.getColumnLetter();
        }
        
        public List<String> getOccurrenceLetters() {
            return occurrenceLetters;
        }
        
        public boolean isDuplicate() {
            return occurrenceLetters.size() > 1;
        }
        
        public boolean isWronglyPositioned() {
            return !mappingDefinition.getColumnLetter().equals(this.occurrenceLetters.get(0));
        }
        
        public boolean isMissing() {
            return occurrenceLetters.isEmpty();
        }
    }
    
    private static class ValidationContext {
        protected final String projectKey;
        protected final String spreadsheetId;
        protected final int headersRowIndex;
        protected final SpreadsheetsManager spreadsheetsManager;

        public ValidationContext(String projectKey, String spreadsheetId, int headersRowIndex, SpreadsheetsManager spreadsheetsManager) {
            this.projectKey = projectKey;
            this.spreadsheetId = spreadsheetId;
            this.headersRowIndex = headersRowIndex;
            this.spreadsheetsManager = spreadsheetsManager;
        }
    }
    
    private static class ValidationContextWithSpreadsheet extends ValidationContext {
        protected final List<String> sheetsTitles;
        
        public ValidationContextWithSpreadsheet(ValidationContext context, List<String> sheetsTitles) {
            super(context.projectKey,context. spreadsheetId, context.headersRowIndex, context.spreadsheetsManager);
            this.sheetsTitles = sheetsTitles;
        }
    }

    private static class ValidationContextWithReport extends ValidationContextWithSpreadsheet {
        protected final List<StaticColumnOccurrenceReport> occurenceReport;
        
        public ValidationContextWithReport(ValidationContextWithSpreadsheet context, List<StaticColumnOccurrenceReport> occurenceReport) {
            super(context, context.sheetsTitles);
            this.occurenceReport = occurenceReport;
        }
    }

    public static class ValidationResult {
        public final boolean success;
        public final String errorMessage;
        public final String errorDetail;

        private ValidationResult(String errorMessage, String errorDetail) {
            this.success = errorMessage == null;
            this.errorMessage = errorMessage;
            this.errorDetail = errorDetail;
        }
        
        public boolean failed() {
            return !success;
        }
        
        public static ValidationResult success() {
            return new ValidationResult(null, null);
        }
        
        public static ValidationResult fail(String errorMessage, String errorDetail) {
            return new ValidationResult(errorMessage, errorDetail);
        }
        
        public static ValidationResult fail(String errorMessage) {
            return new ValidationResult(errorMessage, null);
        }
    }
}
