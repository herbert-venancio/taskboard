package objective.taskboard.sizingImport.cost;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import objective.taskboard.jira.FrontEndMessageException;
import objective.taskboard.jira.data.JiraIssue;
import objective.taskboard.sizingImport.JiraFacade;
import objective.taskboard.sizingImport.SheetColumn;
import objective.taskboard.sizingImport.SizingImportConfig;
import objective.taskboard.sizingImport.SizingSheetImporterNotifier;
import objective.taskboard.sizingImport.SizingImporterRecorder;
import objective.taskboard.sizingImport.SizingImportConfig.IndirectCosts;
import objective.taskboard.sizingImport.SizingImportLine.ImportValue;
import objective.taskboard.sizingImport.cost.SizingImportLineCost;
import objective.taskboard.sizingImport.cost.CostImporter;

public class CostImporterTest {

    private static final String PROJECT_X_KEY = "PX";
    private static final Long PARENT_TYPE_ID = 30L;
    private static final Long SUBTASK_TYPE_ID = 31L;

    private static final SheetColumn INDIRECT_COSTS_COLUMN = new SheetColumn(CostColumnMappingDefinitionProvider.INDIRECT_COSTS, "A");
    private static final SheetColumn INDIRECT_COSTS_KEY_COLUMN = new SheetColumn(CostColumnMappingDefinitionProvider.INDIRECT_COSTS_KEY, "B");
    private static final SheetColumn EFFORT_COLUMN = new SheetColumn(CostColumnMappingDefinitionProvider.EFFORT, "I");

    private final SizingImportConfig importConfig = new SizingImportConfig();
    private final JiraFacade jiraFacade = mock(JiraFacade.class);
    private final SizingSheetImporterNotifier importerNotifier = new SizingSheetImporterNotifier();
    private final SizingImporterRecorder recorder = new SizingImporterRecorder();

    private final CostImporter subject = new CostImporter(importConfig, jiraFacade, importerNotifier);

    @Before
    public void setup() {
        IndirectCosts indirectCosts = new IndirectCosts();
        indirectCosts.setParentTypeId(PARENT_TYPE_ID);
        indirectCosts.setSubtaskTypeId(SUBTASK_TYPE_ID);
        importConfig.setIndirectCosts(indirectCosts);

        importerNotifier.addListener(recorder);
    }

    @Test
    public void importEmptyLines() {
        List<SizingImportLineCost> lines = emptyList();

        subject.executeImport(PROJECT_X_KEY, lines);

        assertEvents(
                "Import started - Total lines count: 0 | lines to import: 0",
                "Import finished");

        verifyJiraFacadeNeverCreateItems();
    }

    @Test
    public void importLinesAlreadyImported() {
        List<SizingImportLineCost> lines = asList(
                new SizingImportLineCost(0, asList(
                        new ImportValue(INDIRECT_COSTS_COLUMN, "Management"),
                        new ImportValue(INDIRECT_COSTS_KEY_COLUMN, "PX-10"),
                        new ImportValue(EFFORT_COLUMN, "1,446"))));

        subject.executeImport(PROJECT_X_KEY, lines);

        assertEvents(
                "Import started - Total lines count: 1 | lines to import: 0",
                "Import finished");

        verifyJiraFacadeNeverCreateItems();
    }

    @Test
    public void importLineWithMissingRequiredColumns() {
        List<SizingImportLineCost> lines = asList(
                new SizingImportLineCost(0, asList(
                        new ImportValue(EFFORT_COLUMN, "1,446"))),
                new SizingImportLineCost(1, asList(
                        new ImportValue(INDIRECT_COSTS_COLUMN, "Management"))),
                new SizingImportLineCost(2, asList(
                        new ImportValue(INDIRECT_COSTS_COLUMN, ""),
                        new ImportValue(EFFORT_COLUMN, "1,446"))),
                new SizingImportLineCost(3, asList(
                        new ImportValue(INDIRECT_COSTS_COLUMN, "Management"),
                        new ImportValue(EFFORT_COLUMN, ""))));

        subject.executeImport(PROJECT_X_KEY, lines);

        assertEvents(
                "Import started - Total lines count: 4 | lines to import: 4",
                "Line import started - Row index: 0",
                "Line error - Row index: 0 | errors: Indirect Costs should be informed",
                "Line import started - Row index: 1",
                "Line error - Row index: 1 | errors: Effort should be informed",
                "Line import started - Row index: 2",
                "Line error - Row index: 2 | errors: Indirect Costs should be informed",
                "Line import started - Row index: 3",
                "Line error - Row index: 3 | errors: Effort should be informed",
                "Import finished");

        verifyJiraFacadeNeverCreateItems();
    }

    @Test
    public void importLineWithRequiredFieldInJira() {
        when(jiraFacade.createIndirectCost(any(), any(), any(), any(), any())).thenThrow(new FrontEndMessageException("Assignee is required"));

        List<SizingImportLineCost> lines = asList(
                new SizingImportLineCost(0, asList(
                        new ImportValue(INDIRECT_COSTS_COLUMN, "Management"),
                        new ImportValue(EFFORT_COLUMN, "1,446"))));

        subject.executeImport(PROJECT_X_KEY, lines);

        assertEvents(
                "Import started - Total lines count: 1 | lines to import: 1",
                "Line import started - Row index: 0",
                "Line error - Row index: 0 | errors: Assignee is required",
                "Import finished");

        verify(jiraFacade).createIndirectCost(PROJECT_X_KEY, PARENT_TYPE_ID, SUBTASK_TYPE_ID, "Management", "1,446h");
    }

    @Test
    public void importHappyDay() {
        when(jiraFacade.createIndirectCost(any(), any(), any(), eq("Management"), eq("1,446h"))).thenReturn(new JiraIssue("PX-15"));
        when(jiraFacade.createIndirectCost(any(), any(), any(), eq("Dev Support"), eq("1.033h"))).thenReturn(new JiraIssue("PX-16"));
        when(jiraFacade.createIndirectCost(any(), any(), any(), eq("Coach"), eq("723h"))).thenReturn(new JiraIssue("PX-17"));

        List<SizingImportLineCost> lines = asList(
                new SizingImportLineCost(0, asList(
                        new ImportValue(INDIRECT_COSTS_COLUMN,     "Management"),
                        new ImportValue(INDIRECT_COSTS_KEY_COLUMN, ""),
                        new ImportValue(EFFORT_COLUMN,             "1,446"))),
                new SizingImportLineCost(1, asList(
                        new ImportValue(INDIRECT_COSTS_COLUMN,     "Dev Support"),
                        new ImportValue(INDIRECT_COSTS_KEY_COLUMN, ""),
                        new ImportValue(EFFORT_COLUMN,             "1.033"))),
                new SizingImportLineCost(2, asList(
                        new ImportValue(INDIRECT_COSTS_COLUMN,     "Coach"),
                        new ImportValue(INDIRECT_COSTS_KEY_COLUMN, ""),
                        new ImportValue(EFFORT_COLUMN,             "723"))),
                new SizingImportLineCost(3, asList(
                        new ImportValue(INDIRECT_COSTS_COLUMN,     "Practice Manager"),
                        new ImportValue(INDIRECT_COSTS_KEY_COLUMN, "PX-1"),
                        new ImportValue(EFFORT_COLUMN,             "7"))));

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

        verify(jiraFacade).createIndirectCost(PROJECT_X_KEY, PARENT_TYPE_ID, SUBTASK_TYPE_ID, "Management", "1,446h");
        verify(jiraFacade).createIndirectCost(PROJECT_X_KEY, PARENT_TYPE_ID, SUBTASK_TYPE_ID, "Dev Support", "1.033h");
        verify(jiraFacade).createIndirectCost(PROJECT_X_KEY, PARENT_TYPE_ID, SUBTASK_TYPE_ID, "Coach", "723h");
    }

    private void verifyJiraFacadeNeverCreateItems() {
        verify(jiraFacade, never()).createIndirectCost(any(), any(), any(), any(), any());
    }

    private void assertEvents(String... expected) {
        assertEquals(StringUtils.join(expected, "\n"), StringUtils.join(recorder.getEvents(), "\n"));
    }

}
