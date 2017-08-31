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
import objective.taskboard.sizingImport.SheetDefinition.SheetColumnDefinition;
import objective.taskboard.sizingImport.SheetDefinition.SheetStaticColumn;
import objective.taskboard.sizingImport.SizingImportValidator.ValidationResult;
import objective.taskboard.sizingImport.SizingSheetParser.SheetColumnMapping;

@Component
public class SizingImportService {

    private static final int PREVIEW_LINES_LIMIT = 5;

    private final SizingImportConfig importConfig;
    private final JiraProperties jiraProperties;
    private final GoogleApiService googleApiService;
    private final JiraUtils jiraUtils;
    private final SizingDataProvider dataProvider;
    private final SheetStaticColumns sheetStaticColumns;
    private final SizingImportValidator importValidator;
    private final SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    public SizingImportService(
            SizingImportConfig importConfig,
            JiraProperties jiraProperties,
            GoogleApiService googleApiService, 
            JiraUtils jiraUtils, 
            SizingDataProvider dataProvider,
            SheetStaticColumns sheetStaticColumns,
            SizingImportValidator importValidator,
            SimpMessagingTemplate messagingTemplate){

        this.importConfig = importConfig;
        this.jiraProperties = jiraProperties;
        this.googleApiService = googleApiService;
        this.jiraUtils = jiraUtils;
        this.dataProvider = dataProvider;
        this.sheetStaticColumns = sheetStaticColumns;
        this.importValidator = importValidator;
        this.messagingTemplate = messagingTemplate;
    }

    public ValidationResult validateSpreadsheet(String projectKey, String spreadsheetId) {
        return importValidator.validate(projectKey, spreadsheetId);
    }
    
    public SheetDefinition getSheetDefinition(String projectKey, String spreadsheetId) {
        SpreadsheetsManager spreadsheetsManager = googleApiService.buildSpreadsheetsManager();
        String lastColumn = spreadsheetsManager.getLastColumnLetter(spreadsheetId, SHEET_TITLE);
        
        List<SheetStaticColumn> staticColumns = sheetStaticColumns.get();
        List<SheetColumnDefinition> dynamicColumns = buildDynamicColumnsDefinition(projectKey);
        
        return new SheetDefinition(lastColumn, staticColumns, dynamicColumns);
    }

    public ImportPreview getPreview(String projectKey, String spreadsheetId, List<SheetColumnMapping> dynamicColumnsMapping) {
        SpreadsheetsManager spreadsheetsManager = googleApiService.buildSpreadsheetsManager();
        List<SizingImportLine> data = dataProvider.getData(spreadsheetsManager, spreadsheetId, dynamicColumnsMapping);
        List<SheetColumnDefinition> dynamicColumnsDefinition = buildDynamicColumnsDefinition(projectKey);
        
        return new PreviewBuilder(dynamicColumnsDefinition, dynamicColumnsMapping)
                .setData(data)
                .setLinesLimit(PREVIEW_LINES_LIMIT)
                .build();
    }

    public void importSpreadsheet(String project, String spreadsheetId, List<SheetColumnMapping> dynamicColumnsMapping) {
        SpreadsheetsManager spreadsheetsManager = googleApiService.buildSpreadsheetsManager();

        List<SizingImportLine> spreedsheetData = dataProvider.getData(
               spreadsheetsManager, 
               spreadsheetId, 
               dynamicColumnsMapping);

        SizingImporter importer = new SizingImporter(jiraProperties, jiraUtils);
        
        importer.addListener(new SizingImporterSheetUpdater(spreadsheetId, spreadsheetsManager, importConfig, jiraProperties));
        importer.addListener(new SizingImporterSocketStatusEmmiter(messagingTemplate));
        
        importer.executeImport(project, spreedsheetData);
    }

    private List<SheetColumnDefinition> buildDynamicColumnsDefinition(String projectKey) {
        CimIssueType featureCreateIssueMetadata = jiraUtils.requestFeatureCreateIssueMetadata(projectKey);
        List<CimFieldInfo> tShirtFields = jiraUtils.getSizingFields(featureCreateIssueMetadata);

        return new DynamicColumnsDefinitionBuilder(tShirtFields)
                .setDefaultColumns(importConfig.getSheetMap().getDefaultColumns())
                .build();
    }

    public static class ImportPreview {
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