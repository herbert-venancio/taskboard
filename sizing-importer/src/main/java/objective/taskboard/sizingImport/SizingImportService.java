package objective.taskboard.sizingImport;

import static java.lang.String.format;
import static objective.taskboard.sizingImport.SizingImportConfig.SHEET_SCOPE;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import objective.taskboard.google.GoogleApiService;
import objective.taskboard.google.SpreadsheetsManager;
import objective.taskboard.sizingImport.PreviewBuilder.ImportPreview;
import objective.taskboard.sizingImport.SizingImportValidator.ValidationResult;
import objective.taskboard.sizingImport.cost.CostSheetSkipper;
import objective.taskboard.sizingImport.cost.SizingImportLineCost;
import objective.taskboard.sizingImport.cost.CostSheetParser;

@Component
class SizingImportService {

    private static final int PREVIEW_LINES_LIMIT = 5;

    private final GoogleApiService googleApiService;
    private final ScopeSheetParser scopeSheetParser;
    private final CostSheetParser costSheetParser;
    private final SheetColumnDefinitionProviderScope columnDefinitionProviderScope;
    private final SizingImportValidator importValidator;
    private final SizingImporter importer;
    private final CostSheetSkipper costSheetSkipper;
    
    @Autowired
    public SizingImportService(
            GoogleApiService googleApiService, 
            ScopeSheetParser scopeSheetParser,
            CostSheetParser costSheetParser,
            SheetColumnDefinitionProviderScope columnDefinitionProviderScope,
            SizingImportValidator importValidator,
            SizingImporter importer,
            CostSheetSkipper costSheetSkipper) {

        this.googleApiService = googleApiService;
        this.scopeSheetParser = scopeSheetParser;
        this.costSheetParser = costSheetParser;
        this.columnDefinitionProviderScope = columnDefinitionProviderScope;
        this.importValidator = importValidator;
        this.importer = importer;
        this.costSheetSkipper = costSheetSkipper;
    }

    public ValidationResult validateSpreadsheet(String spreadsheetId) {
        return importValidator.validate(spreadsheetId);
    }
    
    public String getSheetLastColumn(String spreadsheetId) {
        SpreadsheetsManager spreadsheetsManager = googleApiService.buildSpreadsheetsManager();
        return spreadsheetsManager.getLastColumnLetter(spreadsheetId, SHEET_SCOPE);
    }
    
    public SheetDefinition getSheetDefinition(String projectKey) {
        List<StaticMappingDefinition> staticMappings = columnDefinitionProviderScope.getStaticMappings();
        List<DynamicMappingDefinition> dynamicMappings = columnDefinitionProviderScope.getDynamicMappings(projectKey);
        
        return new SheetDefinition(staticMappings, dynamicMappings);
    }

    public ImportPreview getPreview(String projectKey, String spreadsheetId, List<SheetColumnMapping> columnsMapping) {
        SpreadsheetsManager spreadsheetsManager = googleApiService.buildSpreadsheetsManager();
        List<SizingImportLineScope> scopeLines = parseScopeSheet(projectKey, spreadsheetId, columnsMapping, spreadsheetsManager);

        PreviewBuilder previewBuilder = new PreviewBuilder()
                .setScopeLines(scopeLines)
                .setLinesLimit(PREVIEW_LINES_LIMIT);

        if (!costSheetSkipper.shouldSkip(spreadsheetId))
            previewBuilder.setCostLines(parseCostSheet(spreadsheetId));

        return previewBuilder.build();
    }

    public void importSpreadsheet(String projectKey, String spreadsheetId, List<SheetColumnMapping> columnsMapping) {
        SpreadsheetsManager spreadsheetsManager = googleApiService.buildSpreadsheetsManager();
        List<SizingImportLineScope> scopeLines = parseScopeSheet(projectKey, spreadsheetId, columnsMapping, spreadsheetsManager);

        if (costSheetSkipper.shouldSkip(spreadsheetId)) {
            importer.executeImport(spreadsheetId, spreadsheetsManager, projectKey, scopeLines, Optional.empty());
            return;
        }

        List<SizingImportLineCost> costLines = parseCostSheet(spreadsheetId);
        importer.executeImport(spreadsheetId, spreadsheetsManager, projectKey, scopeLines, Optional.of(costLines));
    }

    private List<SizingImportLineScope> parseScopeSheet(
            String projectKey, 
            String spreadsheetId, 
            List<SheetColumnMapping> columnsMapping, 
            SpreadsheetsManager spreadsheetsManager) {

        List<List<Object>> rows = spreadsheetsManager.readRange(spreadsheetId, format("'%s'", SHEET_SCOPE));
        SheetDefinition sheetDefinition = getSheetDefinition(projectKey);
        return scopeSheetParser.parse(rows, sheetDefinition, columnsMapping);
    }

    private List<SizingImportLineCost> parseCostSheet(String spreadsheetId) {
        return costSheetParser.parse(spreadsheetId);
    }
}