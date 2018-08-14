package objective.taskboard.sizingImport.cost;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static objective.taskboard.sizingImport.SizingImportConfig.SHEET_COST;
import static org.apache.commons.lang3.StringUtils.join;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import objective.taskboard.google.SpreadsheetUtils;
import objective.taskboard.sizingImport.JiraFacade;
import objective.taskboard.sizingImport.SizingImportConfig;
import objective.taskboard.sizingImport.SizingImportValidator.ValidationContextWithSpreadsheet;
import objective.taskboard.sizingImport.SizingImportValidator.ValidationResult;
import objective.taskboard.sizingImport.cost.ColumnMappingDefinition.ColumnMappingDefinitionRow;

@Component
public class CostValidator {

    private final SizingImportConfig config;
    private final JiraFacade jiraFacade;
    private final CostColumnMappingDefinitionProvider costColumnProvider;
    private final CostSheetSkipper costSheetSkipper;

    @Autowired
    public CostValidator(
            SizingImportConfig config,
            JiraFacade jiraFacade,
            CostColumnMappingDefinitionProvider costColumnProvider,
            CostSheetSkipper costSheetSkipper) {

        this.config = config;
        this.jiraFacade = jiraFacade;
        this.costColumnProvider = costColumnProvider;
        this.costSheetSkipper = costSheetSkipper;
    }

    public ValidationResult validate(ValidationContextWithSpreadsheet context) {
        if (costSheetSkipper.shouldSkip(context.spreadsheetId))
            return ValidationResult.success();

        if (!context.sheetsTitles.contains(SHEET_COST))
            return ValidationResult.fail(
                    format("Invalid spreadsheet format: Specified URL should contain a sheet with title “%s”.", SHEET_COST), 
                    format("Found sheets: %s.", join(context.sheetsTitles, ", ")));

        return validateConfiguredIssueTypeIds(context);
    }

    private ValidationResult validateConfiguredIssueTypeIds(ValidationContextWithSpreadsheet context) {
        try {
            jiraFacade.getIssueTypeById(config.getIndirectCosts().getParentTypeId());
            jiraFacade.getIssueTypeById(config.getIndirectCosts().getSubtaskTypeId());
        } catch (IllegalArgumentException ex) {//NOSONAR
            return ValidationResult.fail(format("Invalid configured issue type ids for “%s“ sheet.", SHEET_COST), ex.getMessage());
        }
        return validateHeaderFormat(context);
    }

    private ValidationResult validateHeaderFormat(ValidationContextWithSpreadsheet context) {
        List<List<Object>> rows = context.spreadsheetsManager.readRange(context.spreadsheetId, format("'%s'", SHEET_COST));

        List<ColumnMappingDefinition> headerMappings = costColumnProvider.getHeaderMappings();
        List<ColumnMappingDefinition> headersMissing = headerMappings.stream()
                .filter(headerMapping -> headerMapping.findRow(rows) == null)
                .collect(toList());

        if (!headersMissing.isEmpty())
            return ValidationResult.fail(
                    format("Invalid spreadsheet format: Missing required header columns in “%s“ sheet.", SHEET_COST),
                    headersMissing.stream()
                        .map(h -> format("“%s” column should be placed at position “%s”", h.getColumnDefinition().getName(), h.getColumnLetter()))
                        .collect(joining(", ")) + ". And all the columns should be placed at the same row.");

        ColumnMappingDefinitionRow firstHeaderRow = headerMappings.get(0).findRow(rows);

        List<ColumnOccurrenceReport> occurrenceReports = headerMappings.stream()
                .map(headerMapping -> new ColumnOccurrenceReport(headerMapping, firstHeaderRow.getRow()))
                .collect(toList());

        ValidationContextWithReport contextWithHeader = new ValidationContextWithReport(context, firstHeaderRow.getRowIndex(), occurrenceReports);
        ValidationResult result = validateColumnsExistence(contextWithHeader);
        if (result.failed())
            return result;

        return validateFooterFormat(contextWithHeader);
    }

    private ValidationResult validateFooterFormat(ValidationContextWithReport contextWithHeader) {
        List<List<Object>> rows = contextWithHeader.spreadsheetsManager.readRange(contextWithHeader.spreadsheetId, format("'%s'", SHEET_COST));

        List<ColumnMappingDefinition> footerMappings = costColumnProvider.getFooterMappings();
        List<ColumnMappingDefinition> footersMissing = footerMappings.stream()
                .filter(footerMapping -> footerMapping.findRow(rows) == null)
                .collect(toList());

        if (!footersMissing.isEmpty())
            return ValidationResult.fail(
                    format("Invalid spreadsheet format: Missing required footer columns in “%s“ sheet.", SHEET_COST),
                    footersMissing.stream()
                        .map(f -> format("“%s” column should be placed at position “%s”", f.getColumnDefinition().getName(), f.getColumnLetter()))
                        .collect(joining(", ")) + format(". And all the columns should be placed at the same row after the header row %s.", contextWithHeader.rowIndex + 1));

        ColumnMappingDefinitionRow firstFooterRow = footerMappings.get(0).findRow(rows);

        List<ColumnOccurrenceReport> occurrenceReports = footerMappings.stream()
                .map(footerMapping -> new ColumnOccurrenceReport(footerMapping, firstFooterRow.getRow()))
                .collect(toList());

        ValidationContextWithReport contextWithFooter = new ValidationContextWithReport(contextWithHeader, firstFooterRow.getRowIndex(), occurrenceReports);
        ValidationResult result = validateColumnsExistence(contextWithFooter);
        if (result.failed())
            return result;

        return validateHeaderAndFooterOrder(contextWithHeader, contextWithFooter);
    }

    private ValidationResult validateHeaderAndFooterOrder(ValidationContextWithReport contextWithHeader, ValidationContextWithReport contextWithFooter) {
        int headerRowNumber = contextWithHeader.rowIndex + 1;
        int footerRowNumber = contextWithFooter.rowIndex + 1;
        if (headerRowNumber >= footerRowNumber)
            return ValidationResult.fail(
                    format("Invalid spreadsheet format: Incorrect order for header and footer in “%s“ sheet.", SHEET_COST),
                    format("Footer row %s should be moved to after the header row %s.", footerRowNumber, headerRowNumber));

        return validateDataExistence(contextWithHeader, contextWithFooter);
    }

    private ValidationResult validateDataExistence(ValidationContextWithReport contextWithHeader, ValidationContextWithReport contextWithFooter) {
        int headerRowNumber = contextWithHeader.rowIndex + 1;
        int dataStartingRowNumber = headerRowNumber + 1;
        int footerRowNumber = contextWithFooter.rowIndex + 1;
        if (dataStartingRowNumber >= footerRowNumber)
            return ValidationResult.fail(
                    format("Invalid spreadsheet format: Missing Indirect Costs in “%s“ sheet.", SHEET_COST),
                    format("Indirect Costs to import should start after the header row %s and should end before the footer row %s.", headerRowNumber, footerRowNumber));

        return ValidationResult.success();
    }

    private ValidationResult validateColumnsExistence(ValidationContextWithReport context) {
        List<ColumnOccurrenceReport> missingColumnsReport = context.occurenceReport.stream()
                .filter(ColumnOccurrenceReport::isMissing)
                .collect(toList());

        if (!missingColumnsReport.isEmpty()) {
            return ValidationResult.fail(
                    format("Invalid spreadsheet format: Missing required columns in “%s“ sheet (row %s).", SHEET_COST, context.rowIndex + 1),
                    missingColumnsReport.stream()
                        .map(cr -> format("“%s” column should be placed at position “%s”", cr.getColumnName(), cr.getColumnLetter()))
                        .collect(joining(", ")) + ".");
        }

        return validateColumnsPosition(context);
    }

    private ValidationResult validateColumnsPosition(ValidationContextWithReport context) {
        List<ColumnOccurrenceReport> wronglyPositionedColumnsReport = context.occurenceReport.stream()
                .filter(ColumnOccurrenceReport::isWronglyPositioned)
                .collect(toList());

        if (!wronglyPositionedColumnsReport.isEmpty()) {
            return ValidationResult.fail(
                    format("Invalid spreadsheet format: Incorrectly positioned columns in “%s“ sheet (row %s).", SHEET_COST, context.rowIndex + 1),
                    wronglyPositionedColumnsReport.stream()
                        .map(cr -> format("“%s” column should be moved to position “%s”", cr.getColumnName(), cr.getColumnLetter()))
                        .collect(joining(", ")) + ".");
        }

        return ValidationResult.success();
    }

    private static class ColumnOccurrenceReport {
        private final ColumnMappingDefinition mappingDefinition;
        private String firstOccurrenceLetter;

        public ColumnOccurrenceReport(ColumnMappingDefinition mappingDefinition, List<String> row) {
            this.mappingDefinition = mappingDefinition;

            for (int i = 0; i < row.size(); i++) {
                String cell = row.get(i);

                if (mappingDefinition.getColumnDefinition().getName().equalsIgnoreCase(cell)) {
                    firstOccurrenceLetter = SpreadsheetUtils.columnIndexToLetter(i);
                    break;
                }
            }
        }

        public String getColumnName() {
            return mappingDefinition.getColumnDefinition().getName();
        }

        public String getColumnLetter() {
            return mappingDefinition.getColumnLetter();
        }

        public boolean isWronglyPositioned() {
            return !mappingDefinition.getColumnLetter().equals(this.firstOccurrenceLetter);
        }

        public boolean isMissing() {
            return StringUtils.isBlank(firstOccurrenceLetter);
        }
    }

    private static class ValidationContextWithReport extends ValidationContextWithSpreadsheet {
        protected final int rowIndex;
        protected final List<ColumnOccurrenceReport> occurenceReport;

        public ValidationContextWithReport(ValidationContextWithSpreadsheet context, int rowIndex, List<ColumnOccurrenceReport> occurenceReport) {
            super(context, context.sheetsTitles);
            this.rowIndex = rowIndex;
            this.occurenceReport = occurenceReport;
        }
    }

}
