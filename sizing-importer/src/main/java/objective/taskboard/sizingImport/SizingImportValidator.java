package objective.taskboard.sizingImport;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static objective.taskboard.sizingImport.SheetColumnDefinitionProviderScope.TIMEBOX;
import static objective.taskboard.sizingImport.SizingImportConfig.SHEET_SCOPE;
import static org.apache.commons.lang3.StringUtils.join;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import objective.taskboard.google.GoogleApiService;
import objective.taskboard.google.SpreadsheetUtils;
import objective.taskboard.google.SpreadsheetsManager;
import objective.taskboard.google.SpreadsheetsManager.GoogleApiPermissionDeniedException;
import objective.taskboard.google.SpreadsheetsManager.SpreadsheeNotFoundException;
import objective.taskboard.sizingImport.cost.CostValidator;

@Component
public class SizingImportValidator {

    private final SizingImportConfig config;
    private final GoogleApiService googleApiService;
    private final SheetColumnDefinitionProviderScope columnDefinitionProvider;
    private final TimeboxSkipper timeboxSkipper;
    private final CostValidator costValidator;

    @Autowired
    public SizingImportValidator(
            SizingImportConfig config, 
            GoogleApiService googleApiService, 
            SheetColumnDefinitionProviderScope columnDefinitionProvider,
            CostValidator costValidator,
            TimeboxSkipper timeboxSkipper) {
        this.config = config;
        this.googleApiService = googleApiService;
        this.columnDefinitionProvider = columnDefinitionProvider;
        this.costValidator = costValidator;
        this.timeboxSkipper = timeboxSkipper;
    }

    public ValidationResult validate(String spreadsheetId) {
        SpreadsheetsManager spreadsheetsManager = googleApiService.buildSpreadsheetsManager();
        ValidationContext context = new ValidationContext(spreadsheetId, spreadsheetsManager);

        try {
            return validateSpreadsheetExistence(context);
        }catch(GoogleApiPermissionDeniedException ex) {
            googleApiService.removeCredential();
            throw ex;
        }
    }

    private ValidationResult validateSpreadsheetExistence(ValidationContext context) {
        List<String> sheetsTitles;

        try {
            sheetsTitles = context.spreadsheetsManager.getSheetsTitles(context.spreadsheetId);
        } catch (SpreadsheeNotFoundException ex) {//NOSONAR
            return ValidationResult.fail("Spreadsheet canot be found. Please check the URL.");
        }

        ValidationContextWithSpreadsheet contextWithSpreadsheet = new ValidationContextWithSpreadsheet(context, sheetsTitles);
        ValidationResult result = validateSheetScopeExistence(contextWithSpreadsheet);
        if (result.failed())
            return result;

        result = costValidator.validate(contextWithSpreadsheet);
        if (result.failed())
            return result;

        return ValidationResult.success();
    }

    private ValidationResult validateSheetScopeExistence(ValidationContextWithSpreadsheet context) {
        if (!context.sheetsTitles.contains(SHEET_SCOPE))
            return ValidationResult.fail(
                    format("Invalid spreadsheet format: Specified URL should contain a sheet with title “%s”.", SHEET_SCOPE),
                    format("Found sheets: %s.", join(context.sheetsTitles, ", ")));

        return validateDataStartingRow(context);
    }

    private ValidationResult validateDataStartingRow(ValidationContextWithSpreadsheet context) {
        List<List<Object>> rows = context.spreadsheetsManager.readRange(context.spreadsheetId, format("'%s'", SHEET_SCOPE));

        if (config.getDataStartingRowIndex() >= rows.size()) 
            return invalidDataStartingRow();
        
        int headerRowIndex = config.getTabHeadersRowNumber() - 1;
        List<Object> headersCells = rows.get(headerRowIndex);
        if (headersCells == null || headersCells.isEmpty())
            return invalidDataStartingRow();

        List<String> spreadsheetHeaders = headersCells.stream()
                .map(h -> Objects.toString(h, ""))
                .map(StringUtils::trim)
                .map(String::toLowerCase)
                .collect(toList());

        Predicate<StaticMappingDefinition> filter = generateFilter(context.spreadsheetId);

        List<String> staticColumns = columnDefinitionProvider.getStaticMappings().stream()
                .filter(filter)
                .map(md -> md.getColumnDefinition().getName().toLowerCase())
                .collect(toList());
        
        Optional<String> anyCorrectHeader = spreadsheetHeaders.stream()
                .filter(staticColumns::contains)
                .findAny();
        
        if (!anyCorrectHeader.isPresent())
            return invalidDataStartingRow();

        List<StaticColumnOccurrenceReport> occurenceReport = columnDefinitionProvider.getStaticMappings().stream()
                .filter(filter)
                .map(md -> new StaticColumnOccurrenceReport(md, spreadsheetHeaders))
                .collect(toList());
        
        return validateStaticColumnsExistence(new ValidationContextWithStaticReport(context, headerRowIndex, occurenceReport));
    }

    private Predicate<StaticMappingDefinition> generateFilter(final String spreadsheetId) {
        boolean shouldFilterTimebox = timeboxSkipper.shouldSkip(spreadsheetId);

        Predicate<StaticMappingDefinition> filter = shouldFilterTimebox ?
            md -> !TIMEBOX.equals(md.getColumnDefinition()) : md -> true;

        return filter;
    }

    private ValidationResult invalidDataStartingRow() {
        int headerRowNumber = config.getDataStartingRowIndex();

        String expectedHeaders = columnDefinitionProvider.getStaticMappings().stream()
                .map(md -> md.getColumnDefinition().getName())
                .limit(3)
                .collect(joining(", "));

        String message = format(
                "Invalid spreadsheet format: Row %s of sheet “%s” should contain the headers (e.g. %s).", 
                headerRowNumber, SHEET_SCOPE, expectedHeaders);
        
        String detail = format("Activities to import should start at row %s.", config.getDataStartingRowNumber());
        
        return ValidationResult.fail(message, detail);
    }
    
    private ValidationResult validateStaticColumnsExistence(ValidationContextWithStaticReport context) {
        List<StaticColumnOccurrenceReport> missingColumnsReport = context.occurenceReport.stream()
                .filter(StaticColumnOccurrenceReport::isMissing)
                .collect(toList());

        if (!missingColumnsReport.isEmpty()) {
            return ValidationResult.fail(
                    format("Invalid spreadsheet format: Missing required columns in “%s“ sheet (row %s).", SHEET_SCOPE, context.staticRowIndex + 1),
                    missingColumnsReport.stream()
                        .map(cr -> format("“%s” column should be placed at position “%s”", cr.getColumnName(), cr.getColumnLetter()))
                        .collect(joining(", ")) + ".");
        }

        return validateStaticColumnsUniqueness(context);
    }
    
    private ValidationResult validateStaticColumnsUniqueness(ValidationContextWithStaticReport context) {
        List<StaticColumnOccurrenceReport> duplicateColumnsReport = context.occurenceReport.stream()
                .filter(StaticColumnOccurrenceReport::isDuplicate)
                .collect(toList());
        
        if (!duplicateColumnsReport.isEmpty()) {
            return ValidationResult.fail(
                    format("Invalid spreadsheet format: Duplicate columns found in “%s“ sheet (row %s).", SHEET_SCOPE, context.staticRowIndex + 1),
                    duplicateColumnsReport.stream()
                        .map(cr -> format("“%s” column is showing up in positions “%s”", cr.getColumnName(), join(cr.getOccurrenceLetters(), "”/“")))
                        .collect(joining(", ")) + ".");
        }
        
        return validateStaticColumnsPosition(context);
    }

    private ValidationResult validateStaticColumnsPosition(ValidationContextWithStaticReport context) {
        List<StaticColumnOccurrenceReport> wronglyPositionedColumnsReport = context.occurenceReport.stream()
                .filter(StaticColumnOccurrenceReport::isWronglyPositioned)
                .collect(toList());
        
        if (!wronglyPositionedColumnsReport.isEmpty()) {
            return ValidationResult.fail(
                    format("Invalid spreadsheet format: Incorrectly positioned columns in “%s“ sheet (row %s).", SHEET_SCOPE, context.staticRowIndex + 1),
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
    
    public static class ValidationContext {
        public final String spreadsheetId;
        public final SpreadsheetsManager spreadsheetsManager;

        public ValidationContext(String spreadsheetId, SpreadsheetsManager spreadsheetsManager) {
            this.spreadsheetId = spreadsheetId;
            this.spreadsheetsManager = spreadsheetsManager;
        }
    }
    
    public static class ValidationContextWithSpreadsheet extends ValidationContext {
        public final List<String> sheetsTitles;
        
        public ValidationContextWithSpreadsheet(ValidationContext context, List<String> sheetsTitles) {
            super(context.spreadsheetId, context.spreadsheetsManager);
            this.sheetsTitles = sheetsTitles;
        }
    }

    private static class ValidationContextWithStaticReport extends ValidationContextWithSpreadsheet {
        protected final int staticRowIndex;
        protected final List<StaticColumnOccurrenceReport> occurenceReport;
        
        public ValidationContextWithStaticReport(ValidationContextWithSpreadsheet context, int staticRowIndex, List<StaticColumnOccurrenceReport> occurenceReport) {
            super(context, context.sheetsTitles);
            this.staticRowIndex = staticRowIndex;
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
