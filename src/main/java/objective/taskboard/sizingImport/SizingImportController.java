package objective.taskboard.sizingImport;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atlassian.jira.rest.client.api.RestClientException;

import objective.taskboard.domain.Project;
import objective.taskboard.google.GoogleApiService;
import objective.taskboard.jira.JiraServiceException;
import objective.taskboard.jira.ProjectService;
import objective.taskboard.sizingImport.SheetDefinition.SheetColumnDefinition;
import objective.taskboard.sizingImport.SheetDefinition.SheetStaticColumn;
import objective.taskboard.sizingImport.SizingImportService.ImportPreview;
import objective.taskboard.sizingImport.SizingImportService.SpreadsheetValidationResult;
import objective.taskboard.sizingImport.SizingSheetParser.SheetColumnMapping;

@RestController
@RequestMapping("ws/sizing-import")
public class SizingImportController {

    private final Logger log = LoggerFactory.getLogger(SizingImportController.class);
    private final GoogleApiService googleApiService;
    private final ProjectService projectService;
    private final SizingImportService sizingImportService;
    
    @Autowired
    public SizingImportController(GoogleApiService googleApiService,  ProjectService projectService, SizingImportService sizingImportService) {
        this.googleApiService = googleApiService;
        this.projectService = projectService;
        this.sizingImportService = sizingImportService;
    }
    
    @GetMapping("/initial-data")
    public InitialData getInitialData() throws IOException {
        List<ProjectDto> projects = projectService.getVisibleProjects().stream().map(ProjectDto::new).collect(toList());
        boolean needsGoogleAuthorizarion = !googleApiService.verifyAuthorization();

        return new InitialData(projects, needsGoogleAuthorizarion);
    }

    @GetMapping("/validation/{projectKey}/{spreadsheetId}")
    public SpreadsheetValidationResultDto executeValidation(
            @PathVariable String projectKey, 
            @PathVariable String spreadsheetId) {

        SpreadsheetValidationResult validationResult = sizingImportService.validateSpreadsheet(projectKey, spreadsheetId);
        return new SpreadsheetValidationResultDto(validationResult);
    }

    @PostMapping("/google-authorization")
    public void postGoogleAuthorizarionCode(@RequestBody String authorizationCode) {
        googleApiService.createAndStoreCredential(authorizationCode);
    }

    @PostMapping("/confirmation/{projectKey}/{spreadsheetId}")
    public ResponseEntity<?> confirmation(
            @PathVariable String projectKey, 
            @PathVariable String spreadsheetId, 
            @RequestBody List<SheetColumnMappingDto> dynamicColumnsMappingDto) {

        try {
            List<SheetColumnMapping> dynamicColumnsMapping = dynamicColumnsMappingDto.stream()
                    .map(SheetColumnMappingDto::toObject)
                    .collect(toList());

            ImportPreview preview = sizingImportService.getPreview(projectKey, spreadsheetId, dynamicColumnsMapping);
            return ResponseEntity.ok(new ImportPreviewDto(preview));

        } catch (JiraServiceException | RestClientException ex) {
            log.error(null, ex);
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception ex) {
            log.error(null, ex);
            return new ResponseEntity<>("Internarl Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/spreadsheet-details/{projectKey}/{spreadsheetId}")
    public ResponseEntity<?> spreadsheetDetails(@PathVariable String projectKey, @PathVariable String spreadsheetId) {
        try {
            SheetDefinition sheetDefinition = sizingImportService.getSheetDefinition(projectKey, spreadsheetId);

            return ResponseEntity.ok(new SpreadsheetDefinitionDto(sheetDefinition));

        } catch (JiraServiceException | RestClientException ex) {
            log.error(null, ex);
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception ex) {
            log.error(null, ex);
            return new ResponseEntity<>("Internarl Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/import/{projectKey}/{spreadsheetId}")
    public ResponseEntity<?> importSpreadsheet(
            @PathVariable String projectKey, 
            @PathVariable String spreadsheetId,
            @RequestBody List<SheetColumnMappingDto> dynamicColumnsMappingDto) {

        List<SheetColumnMapping> dynamicColumnsMapping = dynamicColumnsMappingDto.stream()
                .map(SheetColumnMappingDto::toObject)
                .collect(toList());

        try {
            sizingImportService.importSpreadsheet(projectKey, spreadsheetId, dynamicColumnsMapping);
            return ResponseEntity.ok().build();
            
        } catch (JiraServiceException | RestClientException ex) {
            log.error(null, ex);
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception ex) {
            log.error(null, ex);
            return new ResponseEntity<>("Internarl Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    protected static class InitialData {
        public List<ProjectDto> projects;
        public boolean needsGoogleAuthorizarion;
        
        public InitialData(List<ProjectDto> projects, boolean needsGoogleAuthorizarion) {
            this.projects = projects;
            this.needsGoogleAuthorizarion = needsGoogleAuthorizarion;
        }
    }
    
    protected static class SpreadsheetDefinitionDto {
        public final List<SheetStaticColumnDto> staticColumns;
        public final List<SheetColumnDefinitionDto> dynamicColumns;
        public final String lastColumn;
        
        public SpreadsheetDefinitionDto(SheetDefinition object) {
            this.staticColumns = object.getStaticColumns().stream().map(SheetStaticColumnDto::new).collect(toList());
            this.dynamicColumns = object.getDynamicColumns().stream().map(SheetColumnDefinitionDto::new).collect(toList());
            this.lastColumn = object.getLastColumnLetter();
        }
    }
    
    protected static class SheetColumnDefinitionDto {
        public String fieldId;
        public String name;
        public String defaultColumnLetter;
        public boolean required;
        
        public SheetColumnDefinitionDto(SheetColumnDefinition object) {
            this.fieldId = object.getFieldId();
            this.name = object.getName();
            this.defaultColumnLetter = object.getDefaultColumnLetter();
            this.required = object.isRequired();
        }
    }
    
    protected static class SheetStaticColumnDto {
        public String name;
        public String columnLetter;
        
        public SheetStaticColumnDto(SheetStaticColumn staticColumn) {
            this.name = staticColumn.getName();
            this.columnLetter = staticColumn.getColumnLetter();
        }
    }
    
    protected static class SheetColumnMappingDto {
        public String fieldId;
        public String columnLetter;

        public SheetColumnMapping toObject() {
            return new SheetColumnMapping(fieldId, columnLetter);
        }
    }
    protected static class ProjectDto {
        public final String key;
        public final String name;
        
        public ProjectDto(Project project) {
            this.key = project.getKey();
            this.name = project.getName();
        }
    }
    
    protected static class SpreadsheetValidationResultDto {
        public final boolean success;
        public final String errorMessage;

        private SpreadsheetValidationResultDto(SpreadsheetValidationResult validationResult) {
            this.success = validationResult.success;
            this.errorMessage = validationResult.errorMessage;
        }
    }
    
    protected static class ImportPreviewDto {
        public final List<String> headers;
        public final List<List<String>> rows;
        public final int totalLinesCount;

        public ImportPreviewDto(ImportPreview object) {
            this.headers = object.getHeaders();
            this.rows = object.getRows();
            this.totalLinesCount = object.getTotalLinesCount();
        }
    }
}