package objective.taskboard.sizingImport;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static objective.taskboard.sizingImport.SheetColumnDefinitionProvider.EXTRA_FIELD_ID_TAG;
import static objective.taskboard.sizingImport.SheetColumnDefinitionProvider.SIZING_FIELD_ID_TAG;
import static objective.taskboard.testUtils.AssertUtils.collectionToString;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.CimFieldInfo;
import com.atlassian.jira.rest.client.api.domain.CimIssueType;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.Version;

import objective.taskboard.sizingImport.JiraFacade.IssueCustomFieldOptionValue;
import objective.taskboard.sizingImport.JiraFacade.IssueFieldObjectValue;
import objective.taskboard.sizingImport.SheetColumnDefinition.ColumnTag;
import objective.taskboard.sizingImport.SizingImportConfig.SheetMap.ExtraField;
import objective.taskboard.sizingImport.SizingImportLine.ImportValue;
import objective.taskboard.sizingImport.SizingImporter.SizingImporterListener;

public class SizingImporterTest {

    private static final String PROJECT_X_KEY = "PX";
    private static final Version VERSION_ONE = jiraVersion("One");
    
    private static final SheetColumn PHASE_COLUMN = new SheetColumn(SheetColumnDefinitionProvider.PHASE, "A");
    private static final SheetColumn DEMAND_COLUMN = new SheetColumn(SheetColumnDefinitionProvider.DEMAND, "B");
    private static final SheetColumn FEATURE_COLUMN = new SheetColumn(SheetColumnDefinitionProvider.FEATURE, "C");
    private static final SheetColumn KEY_COLUMN = new SheetColumn(SheetColumnDefinitionProvider.KEY, "D");

    private final SizingImportConfig importConfig = new SizingImportConfig();
    private final JiraFacade jiraFacade = mock(JiraFacade.class);
    private final SizingImporterRecorder recorder = new SizingImporterRecorder();
    private final Map<String, CimFieldInfo> featureMetadataFields = new HashMap<>();
    
    private final SizingImporter subject = new SizingImporter(importConfig, jiraFacade);

    @Before
    public void setup() {
        when(jiraFacade.createVersion(PROJECT_X_KEY, "One")).thenReturn(VERSION_ONE);
        when(jiraFacade.getProject(PROJECT_X_KEY)).thenReturn(jiraProject(PROJECT_X_KEY, "Project X"));
        when(jiraFacade.getDemandKeyGivenFeature(any())).thenReturn(Optional.of("PX-1"));

        when(jiraFacade.requestFeatureCreateIssueMetadata(PROJECT_X_KEY)).thenReturn(
                new CimIssueType(null, 0L, "Feature", false, null, null, featureMetadataFields));

        subject.addListener(recorder);
    }
    
    @Test
    public void importEmptyLines() {
        List<SizingImportLine> lines = emptyList();

        subject.executeImport(PROJECT_X_KEY, lines);
        
        assertEvents(
                "Import started - Total lines count: 0 | lines to import: 0",
                "Import finished");
        
        verifyJiraFacadeNeverCreateItems();
    }
    
    @Test
    public void importLinesAlreadyImported() {
        List<SizingImportLine> lines = asList(
                new SizingImportLine(0, asList(
                        new ImportValue(PHASE_COLUMN, "One"), 
                        new ImportValue(DEMAND_COLUMN, "Blue"), 
                        new ImportValue(FEATURE_COLUMN, "Banana"),
                        new ImportValue(KEY_COLUMN, "PX-10"))));
        
        subject.executeImport(PROJECT_X_KEY, lines);
        
        assertEvents(
                "Import started - Total lines count: 1 | lines to import: 0",
                "Import finished");
        
        verifyJiraFacadeNeverCreateItems();
    }
    
    @Test
    public void importLineWithMissingRegularColumn() {
        List<SizingImportLine> lines = asList(
                new SizingImportLine(0, asList(
                        new ImportValue(PHASE_COLUMN, "One"), 
                        new ImportValue(DEMAND_COLUMN, "Blue"))));
        
        subject.executeImport(PROJECT_X_KEY, lines);
        
        assertEvents(
                "Import started - Total lines count: 1 | lines to import: 1",
                "Line import started - Row index: 0",
                "Line error - Row index: 0 | errors: Feature should be informed;",
                "Import finished");

        verifyJiraFacadeNeverCreateItems();
    }

    @Test
    public void importLineWithEmptyRequiredSizingField() {
        when(jiraFacade.getSizingFields(any())).thenReturn(asList(
                jiraRequiredField("f1", "Dev TSize"),
                jiraOptionalField("f2", "UAT TSize")));
        
        
        List<SizingImportLine> lines = asList(
                new SizingImportLine(0, asList(
                        new ImportValue(PHASE_COLUMN, "One"), 
                        new ImportValue(DEMAND_COLUMN, "Blue"),
                        new ImportValue(FEATURE_COLUMN, "Banana"))));
        
        subject.executeImport(PROJECT_X_KEY, lines);
        
        assertEvents(
                "Import started - Total lines count: 1 | lines to import: 1",
                "Line import started - Row index: 0",
                "Line error - Row index: 0 | errors: Dev TSize should be informed;",
                "Import finished");
        
        verifyJiraFacadeNeverCreateItems();
    }

    @Test
    public void importLineWithEmptyRequiredExtraField() {
        featureMetadataFields.put("f9", jiraRequiredField("f9", "Use Cases"));
        
        importConfig.getSheetMap().getExtraFields().add(new ExtraField("f9", "Use Cases", "D"));

        List<SizingImportLine> lines = asList(
                new SizingImportLine(0, asList(
                        new ImportValue(PHASE_COLUMN, "One"), 
                        new ImportValue(DEMAND_COLUMN, "Blue"),
                        new ImportValue(FEATURE_COLUMN, "Banana"))));
        
        subject.executeImport(PROJECT_X_KEY, lines);
        
        assertEvents(
                "Import started - Total lines count: 1 | lines to import: 1",
                "Line import started - Row index: 0",
                "Line error - Row index: 0 | errors: Use Cases should be informed;",
                "Import finished");
        
        verifyJiraFacadeNeverCreateItems();
    }

    @Test
    public void importLineWithoutSizingAndExtraFields() {
        when(jiraFacade.createDemand(any(), eq("Blue"), any())).thenReturn(new BasicIssue(null, "PX-1", 0L));
        when(jiraFacade.createFeature(any(), any(), eq("Banana"), any(), any())).thenReturn(new BasicIssue(null, "PX-15", 0L));

        List<SizingImportLine> lines = asList(
                new SizingImportLine(0, asList(
                        new ImportValue(PHASE_COLUMN,   "One"), 
                        new ImportValue(DEMAND_COLUMN,  "Blue"), 
                        new ImportValue(FEATURE_COLUMN, "Banana"),
                        new ImportValue(KEY_COLUMN,     ""))));

        subject.executeImport(PROJECT_X_KEY, lines);
        
        assertEvents(
                "Import started - Total lines count: 1 | lines to import: 1",
                "Line import started - Row index: 0",
                "Line import finished - Row index: 0 | feature issue key: PX-15",
                "Import finished");
        
        verify(jiraFacade).createVersion(PROJECT_X_KEY, "One");
        verify(jiraFacade).createDemand(PROJECT_X_KEY, "Blue", VERSION_ONE);
        verify(jiraFacade).createFeature(PROJECT_X_KEY, "PX-1", "Banana", VERSION_ONE, emptyList());
    }

    @Test
    public void importHappyDay() {
        CimFieldInfo devTSizeField = jiraOptionalField("f1", "Dev TSize");
        CimFieldInfo uatTSizeField = jiraOptionalField("f2", "UAT TSize");
        CimFieldInfo useCasesField = jiraOptionalField("f3", "Use Cases");
        
        featureMetadataFields.put(devTSizeField.getId(), devTSizeField);
        featureMetadataFields.put(uatTSizeField.getId(), uatTSizeField);
        featureMetadataFields.put(useCasesField.getId(), useCasesField);
        
        when(jiraFacade.getSizingFields(any())).thenReturn(asList(devTSizeField, uatTSizeField));
        
        when(jiraFacade.createDemand(any(), eq("Blue"), any())).thenReturn(new BasicIssue(null, "PX-2", 0L));
        when(jiraFacade.createDemand(any(), eq("Red"), any())).thenReturn(new BasicIssue(null, "PX-3", 0L));
        when(jiraFacade.createFeature(any(), any(), eq("Banana"), any(), any())).thenReturn(new BasicIssue(null, "PX-15", 0L));
        when(jiraFacade.createFeature(any(), any(), eq("Lemon"), any(), any())).thenReturn(new BasicIssue(null, "PX-16", 0L));
        when(jiraFacade.createFeature(any(), any(), eq("Grape"), any(), any())).thenReturn(new BasicIssue(null, "PX-17", 0L));
        
        SheetColumn devTSizeCol = new SheetColumn(new SheetColumnDefinition("Dev TSize", new ColumnTag(SIZING_FIELD_ID_TAG, "f1")), "E");
        SheetColumn uatTSizeCol = new SheetColumn(new SheetColumnDefinition("UAT TSize", new ColumnTag(SIZING_FIELD_ID_TAG, "f2")), "F");
        SheetColumn useCasesCol = new SheetColumn(new SheetColumnDefinition("Use Cases", new ColumnTag(EXTRA_FIELD_ID_TAG,  "f3")), "G");
        
        List<SizingImportLine> lines = asList(
                new SizingImportLine(0, asList(
                        new ImportValue(PHASE_COLUMN,   "One"), 
                        new ImportValue(DEMAND_COLUMN,  "Blue"), 
                        new ImportValue(FEATURE_COLUMN, "Banana"),
                        new ImportValue(KEY_COLUMN,     ""))),
                new SizingImportLine(1, asList(
                        new ImportValue(PHASE_COLUMN,   "One"), 
                        new ImportValue(DEMAND_COLUMN,  "Blue"), 
                        new ImportValue(FEATURE_COLUMN, "Lemon"),
                        new ImportValue(KEY_COLUMN,     ""))),
                new SizingImportLine(2, asList(
                        new ImportValue(PHASE_COLUMN,   "One"), 
                        new ImportValue(DEMAND_COLUMN,  "Red"), 
                        new ImportValue(FEATURE_COLUMN, "Grape"),
                        new ImportValue(KEY_COLUMN,     ""),
                        new ImportValue(devTSizeCol,    "X"),
                        new ImportValue(uatTSizeCol,    "S"),
                        new ImportValue(useCasesCol,    "User picks and eats"))),
                new SizingImportLine(3, asList(
                        new ImportValue(PHASE_COLUMN,   "One"), 
                        new ImportValue(DEMAND_COLUMN,  "White"), 
                        new ImportValue(FEATURE_COLUMN, "Jackfruit"),
                        new ImportValue(KEY_COLUMN,     "PX-1"))));

        subject.executeImport(PROJECT_X_KEY, lines);
        
        assertEvents(
                "Import started - Total lines count: 4 | lines to import: 3",
                "Line import started - Row index: 0",
                "Line import finished - Row index: 0 | feature issue key: PX-15",
                "Line import started - Row index: 1",
                "Line import finished - Row index: 1 | feature issue key: PX-16",
                "Line import started - Row index: 2",
                "Line import finished - Row index: 2 | feature issue key: PX-17",
                "Import finished");
        
        verify(jiraFacade).createVersion(PROJECT_X_KEY, "One");
        verify(jiraFacade).createDemand(PROJECT_X_KEY, "Blue", VERSION_ONE);
        verify(jiraFacade).createDemand(PROJECT_X_KEY, "Red", VERSION_ONE);
        verify(jiraFacade).createFeature(PROJECT_X_KEY, "PX-2", "Banana", VERSION_ONE, emptyList());
        verify(jiraFacade).createFeature(PROJECT_X_KEY, "PX-2", "Lemon", VERSION_ONE, emptyList());
        verify(jiraFacade).createFeature(PROJECT_X_KEY, "PX-3", "Grape", VERSION_ONE, asList(
                new IssueCustomFieldOptionValue("f1", "X", null),
                new IssueCustomFieldOptionValue("f2", "S", null),
                new IssueFieldObjectValue("f3", "User picks and eats")));
    }

    @Test
    public void importUsingAlreadyCreatedVersion() {
        when(jiraFacade.getProject("PY")).thenReturn(jiraProject("PY", "Project Y", asList(jiraVersion("Two"))));
        when(jiraFacade.createDemand(any(), eq("Blue"), any())).thenReturn(new BasicIssue(null, "PY-1", 0L));
        when(jiraFacade.createFeature(any(), any(), eq("Banana"), any(), any())).thenReturn(new BasicIssue(null, "PY-15", 0L));

        List<SizingImportLine> lines = asList(
                new SizingImportLine(0, asList(
                        new ImportValue(PHASE_COLUMN,   "Two"), 
                        new ImportValue(DEMAND_COLUMN,  "Blue"), 
                        new ImportValue(FEATURE_COLUMN, "Banana"),
                        new ImportValue(KEY_COLUMN,     ""))));

        subject.executeImport("PY", lines);
        
        assertEvents(
                "Import started - Total lines count: 1 | lines to import: 1",
                "Line import started - Row index: 0",
                "Line import finished - Row index: 0 | feature issue key: PY-15",
                "Import finished");
        
        verify(jiraFacade, never()).createVersion(any(), eq("Two"));
    }

    @Test
    public void recoverDemandFromPreviouslyImportedFeature() {
        Issue bananaIssue = mock(Issue.class);
        when(jiraFacade.getIssue("PX-10")).thenReturn(bananaIssue);
        when(jiraFacade.getDemandKeyGivenFeature(bananaIssue)).thenReturn(Optional.of("PX-1"));
        when(jiraFacade.createFeature(any(), any(), eq("Lemon"), any(), any())).thenReturn(new BasicIssue(null, "PX-15", 0L));

        List<SizingImportLine> lines = asList(
                new SizingImportLine(0, asList(
                        new ImportValue(PHASE_COLUMN,   "One"), 
                        new ImportValue(DEMAND_COLUMN,  "Blue"), 
                        new ImportValue(FEATURE_COLUMN, "Banana"),
                        new ImportValue(KEY_COLUMN,     "PX-10"))),
                new SizingImportLine(0, asList(
                        new ImportValue(PHASE_COLUMN,   "One"), 
                        new ImportValue(DEMAND_COLUMN,  "Blue"), 
                        new ImportValue(FEATURE_COLUMN, "Lemon"),
                        new ImportValue(KEY_COLUMN,     ""))));

        subject.executeImport(PROJECT_X_KEY, lines);
        
        assertEvents(
                "Import started - Total lines count: 2 | lines to import: 1",
                "Line import started - Row index: 0",
                "Line import finished - Row index: 0 | feature issue key: PX-15",
                "Import finished");
        
        verify(jiraFacade, never()).createDemand(any(), eq("Blue"), any());
        verify(jiraFacade).createFeature(PROJECT_X_KEY, "PX-1", "Lemon", VERSION_ONE, emptyList());
    }

    private static Project jiraProject(String key, String name, List<Version> versions) {
        return new Project(emptyList(), null, key, 0L, name, null, null, null, versions, null, null, null);
    }
    
    private static Project jiraProject(String key, String name) {
        return jiraProject(key, name, emptyList());
    }

    private static CimFieldInfo jiraRequiredField(String id, String name) {
        return new CimFieldInfo(id, true, name, null, null, null, null);
    }
    
    private static CimFieldInfo jiraOptionalField(String id, String name) {
        return new CimFieldInfo(id, false, name, null, null, null, null);
    }
    
    private static Version jiraVersion(String name) {
        return new Version(null, 0L, name, null, false, false, null);
    }
    
    private void verifyJiraFacadeNeverCreateItems() {
        verify(jiraFacade, never()).createVersion(any(), any());
        verify(jiraFacade, never()).createDemand(any(), any(), any());
        verify(jiraFacade, never()).createFeature(any(), any(), any(), any(), any());
    }
    
    private void assertEvents(String... expected) {
        assertEquals(StringUtils.join(expected, "\n"), StringUtils.join(recorder.getEvents(), "\n"));
    }
    
    private static class SizingImporterRecorder implements SizingImporterListener {
        private final List<String> events = new ArrayList<>();

        @Override
        public void onImportStarted(int totalLinesCount, int linesToImportCount) {
            events.add("Import started - Total lines count: " + totalLinesCount + " | lines to import: " + linesToImportCount);
        }

        @Override
        public void onLineImportStarted(SizingImportLine line) {
            events.add("Line import started - Row index: " + line.getRowIndex());
        }

        @Override
        public void onLineImportFinished(SizingImportLine line, String featureIssueKey) {
            events.add("Line import finished - Row index: " + line.getRowIndex() + " | feature issue key: " + featureIssueKey);
        }

        @Override
        public void onLineError(SizingImportLine line, List<String> errorMessages) {
            events.add("Line error - Row index: " + line.getRowIndex() + " | errors: " + collectionToString(errorMessages, " "));
        }

        @Override
        public void onImportFinished() {
            events.add("Import finished");
        }
        
        public List<String> getEvents() {
            return events;
        }
    }
}
