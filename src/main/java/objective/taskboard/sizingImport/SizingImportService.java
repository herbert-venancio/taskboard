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
import objective.taskboard.jira.JiraProperties;
import objective.taskboard.sizingImport.PreviewBuilder.ImportPreview;
import objective.taskboard.sizingImport.SizingImportValidator.ValidationResult;

@Component
class SizingImportService {

    private static final int PREVIEW_LINES_LIMIT = 5;

    private final SizingImportConfig importConfig;
    private final JiraProperties jiraProperties;
    private final GoogleApiService googleApiService;
    private final JiraUtils jiraUtils;
    private final SizingSheetParser sheetParser;
    private final SheetColumnDefinitionProvider columnDefinitionProvider;
    private final SizingImportValidator importValidator;
    private final SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    public SizingImportService(
            SizingImportConfig importConfig,
            JiraProperties jiraProperties,
            GoogleApiService googleApiService, 
            JiraUtils jiraUtils, 
            SizingSheetParser sheetParser,
            SheetColumnDefinitionProvider columnDefinitionProvider,
            SizingImportValidator importValidator,
            SimpMessagingTemplate messagingTemplate){

        this.importConfig = importConfig;
        this.jiraProperties = jiraProperties;
        this.googleApiService = googleApiService;
        this.jiraUtils = jiraUtils;
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
        CimIssueType featureCreateIssueMetadata = jiraUtils.requestFeatureCreateIssueMetadata(projectKey);
        List<CimFieldInfo> featureSizingFields = jiraUtils.getSizingFields(featureCreateIssueMetadata);

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

        SizingImporter importer = new SizingImporter(jiraProperties, jiraUtils);
        
        importer.addListener(new SizingImporterSheetUpdater(spreadsheetId, spreadsheetsManager, importConfig, jiraProperties));
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
