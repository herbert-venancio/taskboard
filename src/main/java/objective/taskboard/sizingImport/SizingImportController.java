package objective.taskboard.sizingImport;

import static java.util.stream.Collectors.toList;
import static objective.taskboard.repository.PermissionRepository.ADMINISTRATIVE;

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

import objective.taskboard.auth.Authorizer;
import objective.taskboard.domain.Project;
import objective.taskboard.google.GoogleApiService;
import objective.taskboard.google.SpreadsheetsManager.SpreadsheetException;
import objective.taskboard.jira.ProjectService;
import objective.taskboard.sizingImport.PreviewBuilder.ImportPreview;
import objective.taskboard.sizingImport.PreviewBuilder.ImportSheetPreview;
import objective.taskboard.sizingImport.SizingImportValidator.ValidationResult;
import retrofit.RetrofitError;

@RestController
@RequestMapping("ws/sizing-import")
public class SizingImportController {

    private final Logger log = LoggerFactory.getLogger(SizingImportController.class);
    private final GoogleApiService googleApiService;
    private final ProjectService projectService;
    private final SizingImportService sizingImportService;
    private final Authorizer authorizer;

    @Autowired
    public SizingImportController(GoogleApiService googleApiService, ProjectService projectService, SizingImportService sizingImportService, Authorizer authorizer) {
        this.googleApiService = googleApiService;
        this.projectService = projectService;
        this.sizingImportService = sizingImportService;
        this.authorizer = authorizer;
    }
    
    @GetMapping("/initial-data")
    public InitialData getInitialData() throws IOException {
        List<ProjectDto> projects = projectService.getNonArchivedJiraProjectsForUser().stream()
                .filter(p -> authorizer.hasPermissionInProject(ADMINISTRATIVE, p.getKey()))
                .map(ProjectDto::new).collect(toList());
        boolean needsGoogleAuthorizarion = !googleApiService.verifyAuthorization();

        return new InitialData(projects, needsGoogleAuthorizarion);
    }

    @GetMapping("/validation/{projectKey}/{spreadsheetId}")
    public ResponseEntity<?> executeValidation(
            @PathVariable String projectKey, 
            @PathVariable String spreadsheetId) {

        if (!authorizer.hasPermissionInProject(ADMINISTRATIVE, projectKey))
            return ResponseEntity.notFound().build();

        ValidationResult validationResult = sizingImportService.validateSpreadsheet(spreadsheetId);
        return ResponseEntity.ok(new SpreadsheetValidationResultDto(validationResult));
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
            if (!authorizer.hasPermissionInProject(ADMINISTRATIVE, projectKey))
                return ResponseEntity.notFound().build();

            List<SheetColumnMapping> dynamicColumnsMapping = dynamicColumnsMappingDto.stream()
                    .map(SheetColumnMappingDto::toObject)
                    .collect(toList());

            ImportPreview preview = sizingImportService.getPreview(projectKey, spreadsheetId, dynamicColumnsMapping);
            return ResponseEntity.ok(new ImportPreviewDto(preview));

        } catch (RetrofitError ex) {
            log.error(null, ex);
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.valueOf(ex.getResponse().getStatus()));
        }
    }

    @GetMapping("/spreadsheet-details/{projectKey}/{spreadsheetId}")
    public ResponseEntity<?> spreadsheetDetails(@PathVariable String projectKey, @PathVariable String spreadsheetId) {
        try {
            if (!authorizer.hasPermissionInProject(ADMINISTRATIVE, projectKey))
                return ResponseEntity.notFound().build();

            SheetDefinition sheetDefinition = sizingImportService.getSheetDefinition(projectKey);
            String lastColumn = sizingImportService.getSheetLastColumn(spreadsheetId);

            return ResponseEntity.ok(new SpreadsheetDetailsDto(sheetDefinition, lastColumn));

        } catch (RetrofitError ex) {
            log.error(null, ex);
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.valueOf(ex.getResponse().getStatus()));
        }
    }

    @PostMapping("/import/{projectKey}/{spreadsheetId}")
    public ResponseEntity<?> importSpreadsheet(
            @PathVariable String projectKey, 
            @PathVariable String spreadsheetId,
            @RequestBody List<SheetColumnMappingDto> dynamicColumnsMappingDto) {

        if (!authorizer.hasPermissionInProject(ADMINISTRATIVE, projectKey))
            return ResponseEntity.notFound().build();

        List<SheetColumnMapping> dynamicColumnsMapping = dynamicColumnsMappingDto.stream()
                .map(SheetColumnMappingDto::toObject)
                .collect(toList());

        try {
            sizingImportService.importSpreadsheet(projectKey, spreadsheetId, dynamicColumnsMapping);
            return ResponseEntity.ok().build();
        } catch (RetrofitError ex) {
            log.error(null, ex);
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.valueOf(ex.getResponse().getStatus()));
        } catch(SpreadsheetException ex) {
            throw ex;
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
    
    protected static class SpreadsheetDetailsDto {
        public final List<SheetStaticColumnDto> staticColumns;
        public final List<SheetDynamicColumnDto> dynamicColumns;
        public final String lastColumn;
        
        public SpreadsheetDetailsDto(SheetDefinition sheetDefinition, String lastColumn) {
            this.staticColumns = sheetDefinition.getStaticColumns().stream().map(SheetStaticColumnDto::new).collect(toList());
            this.dynamicColumns = sheetDefinition.getDynamicColumns().stream().map(SheetDynamicColumnDto::new).collect(toList());
            this.lastColumn = lastColumn;
        }
    }
    
    protected static class SheetDynamicColumnDto {
        public String columnId;
        public String name;
        public String defaultColumnLetter;
        public boolean required;
        
        public SheetDynamicColumnDto(DynamicMappingDefinition mappingDefinition) {
            this.columnId = mappingDefinition.getColumnId();
            this.name = mappingDefinition.getColumnDefinition().getName();
            this.defaultColumnLetter = mappingDefinition.getDefaultColumnLetter().orElse(null);
            this.required = mappingDefinition.isMappingRequired();
        }
    }
    
    protected static class SheetStaticColumnDto {
        public String name;
        public String columnLetter;
        
        public SheetStaticColumnDto(StaticMappingDefinition mappingDefinition) {
            this.name = mappingDefinition.getColumnDefinition().getName();
            this.columnLetter = mappingDefinition.getColumnLetter();
        }
    }
    
    protected static class SheetColumnMappingDto {
        public String columnId;
        public String columnLetter;

        public SheetColumnMapping toObject() {
            return new SheetColumnMapping(columnId, columnLetter);
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
        public final String errorDetail;

        private SpreadsheetValidationResultDto(ValidationResult validationResult) {
            this.success = validationResult.success;
            this.errorMessage = validationResult.errorMessage;
            this.errorDetail = validationResult.errorDetail;
        }
    }
    
    protected static class ImportPreviewDto {
        public final ImportSheetPreviewDto scopePreview;
        public final ImportSheetPreviewDto costPreview;

        public ImportPreviewDto(ImportPreview object) {
            this.scopePreview = new ImportSheetPreviewDto(object.getScopePreview());
            this.costPreview = object.getCostPreview().map(costPreview -> new ImportSheetPreviewDto(costPreview)).orElse(null);
        }
    }

    protected static class ImportSheetPreviewDto {
        public final String sheetTitle;
        public final List<String> headers;
        public final List<List<String>> rows;
        public final int totalLinesCount;

        public ImportSheetPreviewDto(ImportSheetPreview object) {
            this.sheetTitle = object.getSheetTitle();
            this.headers = object.getHeaders();
            this.rows = object.getRows();
            this.totalLinesCount = object.getTotalLinesCount();
        }
    }
}