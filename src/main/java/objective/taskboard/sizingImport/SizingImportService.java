package objective.taskboard.sizingImport;

import static objective.taskboard.sizingImport.SizingImportConfig.SHEET_TITLE;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.atlassian.jira.rest.client.api.domain.CimFieldInfo;
import com.atlassian.jira.rest.client.api.domain.CimIssueType;

import objective.taskboard.google.GoogleApiService;
import objective.taskboard.google.SpreadsheetsManager;
import objective.taskboard.sizingImport.PreviewBuilder.ImportPreview;
import objective.taskboard.sizingImport.SizingImportValidator.ValidationResult;

@Component
class SizingImportService {

    private static final int PREVIEW_LINES_LIMIT = 5;

    private final SizingImportConfig importConfig;
    private final GoogleApiService googleApiService;
    private final JiraFacade jiraFacade;
    private final SizingSheetParser sheetParser;
    private final SheetColumnDefinitionProvider columnDefinitionProvider;
    private final SizingImportValidator importValidator;
    private final SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    public SizingImportService(
            SizingImportConfig importConfig,
            GoogleApiService googleApiService, 
            JiraFacade jiraFacade, 
            SizingSheetParser sheetParser,
            SheetColumnDefinitionProvider columnDefinitionProvider,
            SizingImportValidator importValidator,
            SimpMessagingTemplate messagingTemplate){

        this.importConfig = importConfig;
        this.googleApiService = googleApiService;
        this.jiraFacade = jiraFacade;
        this.sheetParser = sheetParser;
        this.columnDefinitionProvider = columnDefinitionProvider;
        this.importValidator = importValidator;
        this.messagingTemplate = messagingTemplate;
    }

    public ValidationResult validateSpreadsheet(String projectKey, String spreadsheetId) {
        return importValidator.validate(projectKey, spreadsheetId);
    }
    
    public String getSheetLastColumn(String spreadsheetId) {
        SpreadsheetsManager spreadsheetsManager = googleApiService.buildSpreadsheetsManager();
        return spreadsheetsManager.getLastColumnLetter(spreadsheetId, SHEET_TITLE);
    }
    
    public SheetDefinition getSheetDefinition(String projectKey) {
        CimIssueType featureCreateIssueMetadata = jiraFacade.requestFeatureCreateIssueMetadata(projectKey);
        List<CimFieldInfo> featureSizingFields = jiraFacade.getSizingFields(featureCreateIssueMetadata);

        List<StaticMappingDefinition> staticMappings = columnDefinitionProvider.getStaticMappings();
        List<DynamicMappingDefinition> dynamicMappings = columnDefinitionProvider.getDynamicMappings(featureSizingFields);
        
        return new SheetDefinition(staticMappings, dynamicMappings);
    }

    public ImportPreview getPreview(String projectKey, String spreadsheetId, List<SheetColumnMapping> columnsMapping) {
        SpreadsheetsManager spreadsheetsManager = googleApiService.buildSpreadsheetsManager();
        List<SizingImportLine> data = parseSizingSheet(projectKey, spreadsheetId, columnsMapping, spreadsheetsManager);
        
        return new PreviewBuilder()
                .setData(data)
                .setLinesLimit(PREVIEW_LINES_LIMIT)
                .build();
    }

    public void importSpreadsheet(String projectKey, String spreadsheetId, List<SheetColumnMapping> columnsMapping) {
        SpreadsheetsManager spreadsheetsManager = googleApiService.buildSpreadsheetsManager();
        List<SizingImportLine> spreedsheetData = parseSizingSheet(projectKey, spreadsheetId, columnsMapping, spreadsheetsManager);

        SizingImporter importer = new SizingImporter(importConfig, jiraFacade);
        
        importer.addListener(new SizingImporterSheetUpdater(spreadsheetId, spreadsheetsManager, importConfig, jiraFacade.getJiraUrl()));
        importer.addListener(new SizingImporterSocketStatusEmmiter(messagingTemplate));
        
        importer.executeImport(projectKey, spreedsheetData);
    }

    private List<SizingImportLine> parseSizingSheet(
            String projectKey, 
            String spreadsheetId, 
            List<SheetColumnMapping> columnsMapping, 
            SpreadsheetsManager spreadsheetsManager) {

        List<List<Object>> rows = spreadsheetsManager.readRange(spreadsheetId, "'" + SHEET_TITLE + "'");
        SheetDefinition sheetDefinition = getSheetDefinition(projectKey);
        return sheetParser.parse(rows, sheetDefinition, columnsMapping);
    }
}
