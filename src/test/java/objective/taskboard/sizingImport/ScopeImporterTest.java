package objective.taskboard.sizingImport;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static objective.taskboard.sizingImport.SheetColumnDefinitionProviderScope.EXTRA_FIELD_ID_TAG;
import static objective.taskboard.sizingImport.SheetColumnDefinitionProviderScope.SIZING_FIELD_ID_TAG;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import objective.taskboard.jira.client.JiraCreateIssue;
import objective.taskboard.jira.client.JiraIssueDto;
import objective.taskboard.jira.data.JiraIssue;
import objective.taskboard.jira.data.JiraProject;
import objective.taskboard.jira.data.Version;
import objective.taskboard.sizingImport.JiraFacade.IssueCustomFieldOptionValue;
import objective.taskboard.sizingImport.JiraFacade.IssueFieldObjectValue;
import objective.taskboard.sizingImport.SheetColumnDefinition.ColumnTag;
import objective.taskboard.sizingImport.SizingImportConfig.SheetMap.ExtraField;
import objective.taskboard.sizingImport.SizingImportLine.ImportValue;

public class ScopeImporterTest {

    private static final String PROJECT_X_KEY = "PX";
    private static final Version VERSION_ONE = jiraVersion("One");
    private static final Long FEATURE_TYPE_ID = 30L;
    private static final Long TASK_TYPE_ID = 31L;
    
    private static final SheetColumn PHASE_COLUMN = new SheetColumn(SheetColumnDefinitionProviderScope.PHASE, "A");
    private static final SheetColumn DEMAND_COLUMN = new SheetColumn(SheetColumnDefinitionProviderScope.DEMAND, "B");
    private static final SheetColumn FEATURE_COLUMN = new SheetColumn(SheetColumnDefinitionProviderScope.FEATURE, "C");
    private static final SheetColumn TYPE_COLUMN = new SheetColumn(SheetColumnDefinitionProviderScope.TYPE, "D");
    private static final SheetColumn KEY_COLUMN = new SheetColumn(SheetColumnDefinitionProviderScope.KEY, "C");

    private final SizingImportConfig importConfig = new SizingImportConfig();
    private final JiraFacade jiraFacade = mock(JiraFacade.class);
    private final SizingSheetImporterNotifier importerNotifier = new SizingSheetImporterNotifier();
    private final SizingImporterRecorder recorder = new SizingImporterRecorder();

    private final Map<String, JiraCreateIssue.FieldInfoMetadata> featureMetadataFields = new HashMap<>();
    private final Map<String, JiraCreateIssue.FieldInfoMetadata> taskMetadataFields = new HashMap<>();

    private final ScopeImporter subject = new ScopeImporter(importConfig, jiraFacade, importerNotifier);

    @Before
    public void setup() {
        when(jiraFacade.createVersion(PROJECT_X_KEY, "One")).thenReturn(VERSION_ONE);
        when(jiraFacade.getProject(PROJECT_X_KEY)).thenReturn(jiraProject(PROJECT_X_KEY, "Project X"));
        when(jiraFacade.getDemandKeyGivenFeature(any())).thenReturn(Optional.of("PX-1"));

        when(jiraFacade.requestFeatureTypes(PROJECT_X_KEY)).thenReturn(asList(
                new JiraCreateIssue.IssueTypeMetadata(FEATURE_TYPE_ID, "Feature", false, featureMetadataFields),
                new JiraCreateIssue.IssueTypeMetadata(TASK_TYPE_ID,    "Task",    false, taskMetadataFields)));

        importerNotifier.addListener(recorder);
    }
    
    @Test
    public void importEmptyLines() {
        List<SizingImportLineScope> lines = emptyList();

        subject.executeImport(PROJECT_X_KEY, lines);
        
        assertEvents(
                "Import started - Total lines count: 0 | lines to import: 0",
                "Import finished");
        
        verifyJiraFacadeNeverCreateItems();
    }
    
    @Test
    public void importLinesAlreadyImported() {
        List<SizingImportLineScope> lines = asList(
                new SizingImportLineScope(0, asList(
                        new ImportValue(PHASE_COLUMN, "One"), 
                        new ImportValue(DEMAND_COLUMN, "Blue"), 
                        new ImportValue(FEATURE_COLUMN, "Banana"),
                        new ImportValue(TYPE_COLUMN, "Feature"),
                        new ImportValue(KEY_COLUMN, "PX-10"))));
        
        subject.executeImport(PROJECT_X_KEY, lines);
        
        assertEvents(
                "Import started - Total lines count: 1 | lines to import: 0",
                "Import finished");
        
        verifyJiraFacadeNeverCreateItems();
    }
    
    @Test
    public void importLineWithMissingRegularColumn() {
        List<SizingImportLineScope> lines = asList(
                new SizingImportLineScope(0, asList(
                        new ImportValue(PHASE_COLUMN, "One"), 
                        new ImportValue(DEMAND_COLUMN, "Blue"),
                        new ImportValue(TYPE_COLUMN, "Feature"))));
        
        subject.executeImport(PROJECT_X_KEY, lines);
        
        assertEvents(
                "Import started - Total lines count: 1 | lines to import: 1",
                "Line import started - Row index: 0",
                "Line error - Row index: 0 | errors: Feature should be informed",
                "Import finished");

        verifyJiraFacadeNeverCreateItems();
    }

    @Test
    public void importLineWithInvalidType() {
        List<SizingImportLineScope> lines = asList(
                new SizingImportLineScope(0, asList(
                        new ImportValue(PHASE_COLUMN, "One"), 
                        new ImportValue(DEMAND_COLUMN, "Blue"),
                        new ImportValue(FEATURE_COLUMN, "Banana"),
                        new ImportValue(TYPE_COLUMN, "Bug"))));
        
        subject.executeImport(PROJECT_X_KEY, lines);
        
        assertEvents(
                "Import started - Total lines count: 1 | lines to import: 1",
                "Line import started - Row index: 0",
                "Line error - Row index: 0 | errors: Type should be one of the following: Feature, Task",
                "Import finished");
        
        verifyJiraFacadeNeverCreateItems();
    }

    @Test
    public void importLineWithEmptyRequiredSizingField() {
        featureMetadataFields.put("f1", jiraRequiredField("f1", "Dev TSize"));
        featureMetadataFields.put("f2", jiraOptionalField("f2", "UAT TSize"));

        when(jiraFacade.getSizingFieldIds()).thenReturn(asList("f1", "f2"));

        List<SizingImportLineScope> lines = asList(
                new SizingImportLineScope(0, asList(
                        new ImportValue(PHASE_COLUMN, "One"), 
                        new ImportValue(DEMAND_COLUMN, "Blue"),
                        new ImportValue(FEATURE_COLUMN, "Banana"),
                        new ImportValue(TYPE_COLUMN, "Feature"))));
        
        subject.executeImport(PROJECT_X_KEY, lines);
        
        assertEvents(
                "Import started - Total lines count: 1 | lines to import: 1",
                "Line import started - Row index: 0",
                "Line error - Row index: 0 | errors: Dev TSize should be informed",
                "Import finished");
        
        verifyJiraFacadeNeverCreateItems();
    }

    @Test
    public void importLineWithEmptyRequiredExtraField() {
        featureMetadataFields.put("f9", jiraRequiredField("f9", "Use Cases"));
        
        importConfig.getSheetMap().getExtraFields().add(new ExtraField("f9", "Use Cases", "D"));

        List<SizingImportLineScope> lines = asList(
                new SizingImportLineScope(0, asList(
                        new ImportValue(PHASE_COLUMN, "One"), 
                        new ImportValue(DEMAND_COLUMN, "Blue"),
                        new ImportValue(FEATURE_COLUMN, "Banana"),
                        new ImportValue(TYPE_COLUMN, "Feature"))));
        
        subject.executeImport(PROJECT_X_KEY, lines);
        
        assertEvents(
                "Import started - Total lines count: 1 | lines to import: 1",
                "Line import started - Row index: 0",
                "Line error - Row index: 0 | errors: Use Cases should be informed",
                "Import finished");
        
        verifyJiraFacadeNeverCreateItems();
    }

    @Test
    public void importLineWithoutSizingAndExtraFields() {
        when(jiraFacade.createDemand(any(), eq("Blue"), any())).thenReturn(new JiraIssue("PX-1"));
        when(jiraFacade.createFeature(any(), any(), any(), eq("Banana"), any(), any())).thenReturn(new JiraIssue("PX-15"));

        List<SizingImportLineScope> lines = asList(
                new SizingImportLineScope(0, asList(
                        new ImportValue(PHASE_COLUMN,   "One"), 
                        new ImportValue(DEMAND_COLUMN,  "Blue"), 
                        new ImportValue(FEATURE_COLUMN, "Banana"),
                        new ImportValue(TYPE_COLUMN,    "Feature"),
                        new ImportValue(KEY_COLUMN,     ""))));

        subject.executeImport(PROJECT_X_KEY, lines);
        
        assertEvents(
                "Import started - Total lines count: 1 | lines to import: 1",
                "Line import started - Row index: 0",
                "Line import finished - Row index: 0 | issue key: PX-15",
                "Import finished");
        
        verify(jiraFacade).createVersion(PROJECT_X_KEY, "One");
        verify(jiraFacade).createDemand(PROJECT_X_KEY, "Blue", VERSION_ONE);
        verify(jiraFacade).createFeature(PROJECT_X_KEY, "PX-1", FEATURE_TYPE_ID, "Banana", VERSION_ONE, emptyList());
    }

    @Test
    public void importHappyDay() {
        JiraCreateIssue.FieldInfoMetadata devTSizeField = jiraOptionalField("f1", "Dev TSize");
        JiraCreateIssue.FieldInfoMetadata uatTSizeField = jiraOptionalField("f2", "UAT TSize");
        JiraCreateIssue.FieldInfoMetadata taskTSizeField = jiraOptionalField("f5", "Task TSize");
        JiraCreateIssue.FieldInfoMetadata useCasesField = jiraOptionalField("f3", "Use Cases");
        
        featureMetadataFields.put(devTSizeField.id, devTSizeField);
        featureMetadataFields.put(uatTSizeField.id, uatTSizeField);
        featureMetadataFields.put(useCasesField.id, useCasesField);
        taskMetadataFields.put(taskTSizeField.id, taskTSizeField);
        
        when(jiraFacade.getSizingFieldIds()).thenReturn(asList(devTSizeField.id, uatTSizeField.id, taskTSizeField.id));
        importConfig.getSheetMap().getExtraFields().add(new ExtraField(useCasesField.id, "Use Cases", "H"));
        
        when(jiraFacade.createDemand(any(), eq("Blue"), any())).thenReturn(new JiraIssue("PX-2"));
        when(jiraFacade.createDemand(any(), eq("Red"), any())).thenReturn(new JiraIssue("PX-3"));
        when(jiraFacade.createFeature(any(), any(), any(), eq("Banana"), any(), any())).thenReturn(new JiraIssue("PX-15"));
        when(jiraFacade.createFeature(any(), any(), any(), eq("Lemon"), any(), any())).thenReturn(new JiraIssue("PX-16"));
        when(jiraFacade.createFeature(any(), any(), any(), eq("Grape"), any(), any())).thenReturn(new JiraIssue("PX-17"));
        
        SheetColumn devTSizeCol = new SheetColumn(new SheetColumnDefinition("Dev TSize", new ColumnTag(SIZING_FIELD_ID_TAG, "f1")), "E");
        SheetColumn uatTSizeCol = new SheetColumn(new SheetColumnDefinition("UAT TSize", new ColumnTag(SIZING_FIELD_ID_TAG, "f2")), "F");
        SheetColumn taskTSizeCol = new SheetColumn(new SheetColumnDefinition("Task TSize", new ColumnTag(SIZING_FIELD_ID_TAG, "f5")), "G");
        SheetColumn useCasesCol = new SheetColumn(new SheetColumnDefinition("Use Cases", new ColumnTag(EXTRA_FIELD_ID_TAG,  "f3")), "H");
        
        List<SizingImportLineScope> lines = asList(
                new SizingImportLineScope(0, asList(
                        new ImportValue(PHASE_COLUMN,   "One"), 
                        new ImportValue(DEMAND_COLUMN,  "Blue"), 
                        new ImportValue(FEATURE_COLUMN, "Banana"),
                        new ImportValue(TYPE_COLUMN,    "Feature"),
                        new ImportValue(KEY_COLUMN,     ""))),
                new SizingImportLineScope(1, asList(
                        new ImportValue(PHASE_COLUMN,   "One"), 
                        new ImportValue(DEMAND_COLUMN,  "Blue"), 
                        new ImportValue(FEATURE_COLUMN, "Lemon"),
                        new ImportValue(TYPE_COLUMN,    "Task"),
                        new ImportValue(KEY_COLUMN,     ""),
                        new ImportValue(taskTSizeCol,   "M"))),
                new SizingImportLineScope(2, asList(
                        new ImportValue(PHASE_COLUMN,   "One"), 
                        new ImportValue(DEMAND_COLUMN,  "Red"), 
                        new ImportValue(FEATURE_COLUMN, "Grape"),
                        new ImportValue(TYPE_COLUMN,    "Feature"),
                        new ImportValue(KEY_COLUMN,     ""),
                        new ImportValue(devTSizeCol,    "X"),
                        new ImportValue(uatTSizeCol,    "S"),
                        new ImportValue(useCasesCol,    "User picks and eats"))),
                new SizingImportLineScope(3, asList(
                        new ImportValue(PHASE_COLUMN,   "One"), 
                        new ImportValue(DEMAND_COLUMN,  "White"), 
                        new ImportValue(FEATURE_COLUMN, "Jackfruit"),
                        new ImportValue(TYPE_COLUMN,    "Feature"),
                        new ImportValue(KEY_COLUMN,     "PX-1"))));

        subject.executeImport(PROJECT_X_KEY, lines);
        
        assertEvents(
                "Import started - Total lines count: 4 | lines to import: 3",
                "Line import started - Row index: 0",
                "Line import finished - Row index: 0 | issue key: PX-15",
                "Line import started - Row index: 1",
                "Line import finished - Row index: 1 | issue key: PX-16",
                "Line import started - Row index: 2",
                "Line import finished - Row index: 2 | issue key: PX-17",
                "Import finished");
        
        verify(jiraFacade).createVersion(PROJECT_X_KEY, "One");
        verify(jiraFacade).createDemand(PROJECT_X_KEY, "Blue", VERSION_ONE);
        verify(jiraFacade).createDemand(PROJECT_X_KEY, "Red", VERSION_ONE);
        verify(jiraFacade).createFeature(PROJECT_X_KEY, "PX-2", FEATURE_TYPE_ID, "Banana", VERSION_ONE, emptyList());
        verify(jiraFacade).createFeature(PROJECT_X_KEY, "PX-2", TASK_TYPE_ID, "Lemon", VERSION_ONE, asList(
                new IssueCustomFieldOptionValue("f5", "M", null)));
        verify(jiraFacade).createFeature(PROJECT_X_KEY, "PX-3", FEATURE_TYPE_ID, "Grape", VERSION_ONE, asList(
                new IssueCustomFieldOptionValue("f1", "X", null),
                new IssueCustomFieldOptionValue("f2", "S", null),
                new IssueFieldObjectValue("f3", "User picks and eats")));
    }

    @Test
    public void importLineWithUnsupportedFields() {
        JiraCreateIssue.FieldInfoMetadata devTSizeField = jiraOptionalField("f1", "Dev TSize");
        JiraCreateIssue.FieldInfoMetadata uatTSizeField = jiraOptionalField("f2", "UAT TSize");
        
        featureMetadataFields.put(devTSizeField.id, devTSizeField);
        
        when(jiraFacade.getSizingFieldIds()).thenReturn(asList(devTSizeField.id, uatTSizeField.id));
        when(jiraFacade.createDemand(any(), eq("Blue"), any())).thenReturn(new JiraIssue("PX-1"));
        when(jiraFacade.createFeature(any(), any(), any(), eq("Banana"), any(), any())).thenReturn(new JiraIssue("PX-15"));
        
        SheetColumn devTSizeCol = new SheetColumn(new SheetColumnDefinition("Dev TSize", new ColumnTag(SIZING_FIELD_ID_TAG, "f1")), "E");
        SheetColumn uatTSizeCol = new SheetColumn(new SheetColumnDefinition("UAT TSize", new ColumnTag(SIZING_FIELD_ID_TAG, "f2")), "F");
        SheetColumn useCasesCol = new SheetColumn(new SheetColumnDefinition("Use Cases", new ColumnTag(EXTRA_FIELD_ID_TAG,  "f3")), "G");

        List<SizingImportLineScope> lines = asList(
                new SizingImportLineScope(0, asList(
                        new ImportValue(PHASE_COLUMN,   "One"), 
                        new ImportValue(DEMAND_COLUMN,  "Blue"), 
                        new ImportValue(FEATURE_COLUMN, "Banana"),
                        new ImportValue(TYPE_COLUMN,    "Feature"),
                        new ImportValue(devTSizeCol,    "X"),
                        new ImportValue(uatTSizeCol,    "S"),
                        new ImportValue(useCasesCol,    "User picks and eats"))));

        subject.executeImport(PROJECT_X_KEY, lines);
        
        assertEvents(
                "Import started - Total lines count: 1 | lines to import: 1",
                "Line import started - Row index: 0",
                "Line error - Row index: 0 | errors: " + 
                        "Column “UAT TSize” is not valid for the type Feature and should be left blank; " + 
                        "Column “Use Cases” is not valid for the type Feature and should be left blank",
                "Import finished");
        
        verifyJiraFacadeNeverCreateItems();
    }
    
    @Test
    public void importUsingAlreadyCreatedVersion() {
        when(jiraFacade.getProject("PY")).thenReturn(jiraProject("PY", "Project Y", asList(jiraVersion("Two"))));
        when(jiraFacade.createDemand(any(), eq("Blue"), any())).thenReturn(new JiraIssue("PY-1"));
        when(jiraFacade.createFeature(any(), any(), any(), eq("Banana"), any(), any())).thenReturn(new JiraIssue("PY-15"));
        when(jiraFacade.requestFeatureTypes("PY")).thenReturn(asList(new JiraCreateIssue.IssueTypeMetadata(FEATURE_TYPE_ID, "Feature", false, emptyMap())));

        List<SizingImportLineScope> lines = asList(
                new SizingImportLineScope(0, asList(
                        new ImportValue(PHASE_COLUMN,   "Two"), 
                        new ImportValue(DEMAND_COLUMN,  "Blue"), 
                        new ImportValue(FEATURE_COLUMN, "Banana"),
                        new ImportValue(TYPE_COLUMN,    "Feature"),
                        new ImportValue(KEY_COLUMN,     ""))));

        subject.executeImport("PY", lines);
        
        assertEvents(
                "Import started - Total lines count: 1 | lines to import: 1",
                "Line import started - Row index: 0",
                "Line import finished - Row index: 0 | issue key: PY-15",
                "Import finished");
        
        verify(jiraFacade, never()).createVersion(any(), eq("Two"));
    }

    @Test
    public void recoverDemandFromPreviouslyImportedFeature() {
        JiraIssueDto bananaIssue = mock(JiraIssueDto.class);
        when(jiraFacade.getIssue("PX-10")).thenReturn(bananaIssue);
        when(jiraFacade.getDemandKeyGivenFeature(bananaIssue)).thenReturn(Optional.of("PX-1"));
        when(jiraFacade.createFeature(any(), any(), any(), eq("Lemon"), any(), any())).thenReturn(new JiraIssue("PX-15"));

        List<SizingImportLineScope> lines = asList(
                new SizingImportLineScope(0, asList(
                        new ImportValue(PHASE_COLUMN,   "One"), 
                        new ImportValue(DEMAND_COLUMN,  "Blue"), 
                        new ImportValue(FEATURE_COLUMN, "Banana"),
                        new ImportValue(TYPE_COLUMN,    "Feature"),
                        new ImportValue(KEY_COLUMN,     "PX-10"))),
                new SizingImportLineScope(0, asList(
                        new ImportValue(PHASE_COLUMN,   "One"), 
                        new ImportValue(DEMAND_COLUMN,  "Blue"), 
                        new ImportValue(FEATURE_COLUMN, "Lemon"),
                        new ImportValue(TYPE_COLUMN,    "Feature"),
                        new ImportValue(KEY_COLUMN,     ""))));

        subject.executeImport(PROJECT_X_KEY, lines);
        
        assertEvents(
                "Import started - Total lines count: 2 | lines to import: 1",
                "Line import started - Row index: 0",
                "Line import finished - Row index: 0 | issue key: PX-15",
                "Import finished");
        
        verify(jiraFacade, never()).createDemand(any(), eq("Blue"), any());
        verify(jiraFacade).createFeature(PROJECT_X_KEY, "PX-1", FEATURE_TYPE_ID, "Lemon", VERSION_ONE, emptyList());
    }

    private static JiraProject jiraProject(String key, String name, List<Version> versions) {
        return new JiraProject("0", key, versions, name);
    }
    
    private static JiraProject jiraProject(String key, String name) {
        return jiraProject(key, name, emptyList());
    }

    private static JiraCreateIssue.FieldInfoMetadata jiraRequiredField(String id, String name) {
        return new JiraCreateIssue.FieldInfoMetadata(id, true, name);
    }
    
    private static JiraCreateIssue.FieldInfoMetadata jiraOptionalField(String id, String name) {
        return new JiraCreateIssue.FieldInfoMetadata(id, false, name);
    }
    
    private static Version jiraVersion(String name) {
        return new Version("0", name);
    }
    
    private void verifyJiraFacadeNeverCreateItems() {
        verify(jiraFacade, never()).createVersion(any(), any());
        verify(jiraFacade, never()).createDemand(any(), any(), any());
        verify(jiraFacade, never()).createFeature(any(), any(), any(), any(), any(), any());
    }

    private void assertEvents(String... expected) {
        assertEquals(StringUtils.join(expected, "\n"), StringUtils.join(recorder.getEvents(), "\n"));
    }

}
