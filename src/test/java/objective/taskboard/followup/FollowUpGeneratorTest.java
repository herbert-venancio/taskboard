/*-
 * [LICENSE]
 * Taskboard
 * ---
 * Copyright (C) 2015 - 2017 Objective Solutions
 * ---
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * [/LICENSE]
 */
package objective.taskboard.followup;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static objective.taskboard.Constants.FROMJIRA_HEADERS;
import static objective.taskboard.followup.FollowUpHelper.getAnalyticsTransitionsDataSetWitNoRow;
import static objective.taskboard.followup.FollowUpHelper.getDefaultAnalyticsTransitionsDataSet;
import static objective.taskboard.followup.FollowUpHelper.getDefaultFollowupData;
import static objective.taskboard.followup.FollowUpHelper.getDefaultFromJiraDataRow;
import static objective.taskboard.followup.FollowUpHelper.getDefaultFromJiraDataRowList;
import static objective.taskboard.followup.FollowUpHelper.getDefaultSyntheticTransitionsDataSet;
import static objective.taskboard.followup.FollowUpHelper.getEmptyAnalyticsTransitionsDataSet;
import static objective.taskboard.followup.FollowUpHelper.getEmptyFollowupData;
import static objective.taskboard.followup.FollowUpHelper.getEmptySyntheticTransitionsDataSet;
import static objective.taskboard.followup.FollowUpHelper.getSyntheticTransitionsDataSetWithNoRow;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.Resource;

import objective.taskboard.followup.FromJiraRowCalculator.FromJiraRowCalculation;
import objective.taskboard.spreadsheet.Sheet;
import objective.taskboard.spreadsheet.SimpleSpreadsheetEditor;
import objective.taskboard.spreadsheet.SimpleSpreadsheetEditorMock;
import objective.taskboard.utils.IOUtilities;

@RunWith(MockitoJUnitRunner.class)
public class FollowUpGeneratorTest {

    private FollowUpGenerator subject;

    @Mock
    private FollowupDataProvider provider;
    
    @Mock
    private FromJiraRowCalculator rowCalculator;

    private SimpleSpreadsheetEditorMock editor = new SimpleSpreadsheetEditorMock();
    private LocalDate followUpDate = LocalDate.parse("2007-12-03");

    @Before
    public void setup() {
        when(rowCalculator.calculate(any())).thenReturn(new FromJiraRowCalculation(0, 0, 0));
    }
    
    @Test
    public void generateJiraDataSheetTest() throws IOException {
        subject = new FollowUpGenerator(provider, editor, rowCalculator);

        subject.getEditor().open();
        subject.generateFromJiraSheet(getDefaultFollowupData());
        subject.getEditor().close();

        String fromJiraSheetExpected = txtResourceAsString("followup/generateJiraDataSheetTest.txt");
        assertEquals("From Jira sheet", fromJiraSheetExpected, editor.loggerString());
    }

    @Test
    public void whenGeneratingWithoutData_generatesFromJiraKeepingOnlyHeaders() throws IOException {
        subject = new FollowUpGenerator(provider, editor, rowCalculator);

        subject.getEditor().open();
        subject.generateFromJiraSheet(getEmptyFollowupData());
        subject.getEditor().close();

        String fromJiraSheetExpected = txtResourceAsString("followup/emptyFromJiraWithGeneratedHeaders.txt");
        assertEquals("From Jira sheet", fromJiraSheetExpected, editor.loggerString());
    }

    @Test
    public void givenEmptyTransitions_whenGenerateFromJiraSheet_thenShouldNotGenerateTransitions() throws IOException {
        subject = new FollowUpGenerator(provider, editor, rowCalculator);

        FromJiraDataSet fromJiraDS = new FromJiraDataSet(FROMJIRA_HEADERS, getDefaultFromJiraDataRowList());
        FollowupData followupData = new FollowupData(fromJiraDS, getEmptyAnalyticsTransitionsDataSet(),
                getEmptySyntheticTransitionsDataSet());

        subject.getEditor().open();
        subject.generateFromJiraSheet(followupData);
        subject.getEditor().close();

        String fromJiraSheetExpected = txtResourceAsString("followup/fromJiraWithNoTransitions.txt");
        assertEquals("From Jira sheet", fromJiraSheetExpected, editor.loggerString());
    }

    @Test
    public void givenNoTransitions_whenGenerateFromJiraSheet_thenShouldNotGenerateTransitions() throws IOException {
        subject = new FollowUpGenerator(provider, editor, rowCalculator);

        FromJiraDataSet fromJiraDS = new FromJiraDataSet(FROMJIRA_HEADERS, getDefaultFromJiraDataRowList());
        FollowupData followupData = new FollowupData(fromJiraDS, null, null);

        subject.getEditor().open();
        subject.generateFromJiraSheet(followupData);
        subject.getEditor().close();

        String fromJiraSheetExpected = txtResourceAsString("followup/fromJiraWithNoTransitions.txt");
        assertEquals("From Jira sheet", fromJiraSheetExpected, editor.loggerString());
    }

    @Test
    public void givenTransitionsDatesOfOtherIssueKey_whenGenerateFromJiraSheet_thenShouldGenerateEmptyTransitionsDates() throws IOException {
        subject = new FollowUpGenerator(provider, editor, rowCalculator);

        FromJiraDataSet fromJiraDS = new FromJiraDataSet(FROMJIRA_HEADERS, getDefaultFromJiraDataRowList());
        FollowupData followupData = new FollowupData(fromJiraDS, getAnalyticsTransitionsDataSetWitNoRow(), getSyntheticTransitionsDataSetWithNoRow());

        subject.getEditor().open();
        subject.generateFromJiraSheet(followupData);
        subject.getEditor().close();

        String fromJiraSheetExpected = txtResourceAsString("followup/fromJiraWithEmptyTransitions.txt");
        assertEquals("From Jira sheet", fromJiraSheetExpected, editor.loggerString());
    }

    @Test
    public void generateJiraDataSheetWithSomeEmptyAndNullAttributesJiraDataTest() throws IOException {
        subject = new FollowUpGenerator(provider, editor, rowCalculator);

        FromJiraDataRow fromJiraDefault = getDefaultFromJiraDataRow();
        fromJiraDefault.project = "";
        fromJiraDefault.demandType = null;
        fromJiraDefault.taskId = 0L;
        fromJiraDefault.subtaskId = null;
        fromJiraDefault.worklog = 0.0;
        fromJiraDefault.wrongWorklog = null;

        FromJiraDataSet fromJiraDs = new FromJiraDataSet(FROMJIRA_HEADERS, asList(fromJiraDefault));
        FollowupData followupData = new FollowupData(fromJiraDs, emptyList(), emptyList());

        subject.getEditor().open();
        subject.generateFromJiraSheet(followupData);
        subject.getEditor().close();

        String fromJiraSheetExpected = txtResourceAsString("followup/generateJiraDataSheetWithSomeEmptyAndNullAttributesJiraDataTest.txt");
        assertEquals("From Jira sheet", fromJiraSheetExpected, editor.loggerString());
    }

    @Test
    public void generateTest() throws IOException {
        FollowUpTemplate testTemplate = new FollowUpTemplate(resolve("followup/Followup-template.xlsm"));
        subject = new FollowUpGenerator(provider, new SimpleSpreadsheetEditor(testTemplate), rowCalculator);

        when(provider.getJiraData(emptyArray(), ZoneId.systemDefault())).thenReturn(new FollowUpDataEntry(followUpDate, getDefaultFollowupData()));

        Resource resource = subject.generate(emptyArray(), ZoneId.systemDefault());
        assertNotNull("Resource shouldn't be null", resource);
    }

    @Test
    public void generateUsingGenericTemplateTest() throws IOException {
        FollowUpTemplate testTemplate = new FollowUpTemplate(resolve("followup/Followup-template.xlsm"));
        subject = new FollowUpGenerator(provider, new SimpleSpreadsheetEditor(testTemplate), rowCalculator);

        when(provider.getJiraData(emptyArray(), ZoneId.systemDefault())).thenReturn(new FollowUpDataEntry(followUpDate, getDefaultFollowupData()));

        Resource resource = subject.generate(emptyArray(), ZoneId.systemDefault());
        assertNotNull("Resource shouldn't be null", resource);
    }

    @Test
    public void generateLotsOfLines() throws IOException {
        FollowUpTemplate testTemplate = new FollowUpTemplate(resolve("followup/Followup-template.xlsm"));
        subject = new FollowUpGenerator(provider, new SimpleSpreadsheetEditor(testTemplate), rowCalculator);

        List<FromJiraDataRow> fromJiraDataRowList = new LinkedList<>();
        for (int i=0; i < 5000; i++)
            fromJiraDataRowList.add(getDefaultFromJiraDataRow());

        FromJiraDataSet fromJiraDs = new FromJiraDataSet(FROMJIRA_HEADERS, fromJiraDataRowList);
        FollowupData followupData = new FollowupData(fromJiraDs, getDefaultAnalyticsTransitionsDataSet(), getDefaultSyntheticTransitionsDataSet());
        FollowUpDataEntry followUpDataEntry = new FollowUpDataEntry(followUpDate, followupData);

        when(provider.getJiraData(emptyArray(), ZoneId.systemDefault())).thenReturn(followUpDataEntry);
        Resource resource = subject.generate(emptyArray(), ZoneId.systemDefault());
        assertNotNull("Resource shouldn't be null", resource);
    }

    @Test
    public void givenIssues_whenGenerateTransitionsSheets_thenSheetsShouldBeGenerated() throws IOException {
        subject = new FollowUpGenerator(provider, editor, rowCalculator);

        subject.getEditor().open();
        subject.generateTransitionsSheets(getDefaultFollowupData());
        subject.getEditor().close();

        String transitionsSheetsExpected = txtResourceAsString("followup/transitionsSheets.txt");
        assertEquals("From Jira sheet", transitionsSheetsExpected, editor.loggerString());
    }

    @Test
    public void givenNoIssue_whenGenerateTransitionsSheets_thenSheetsShouldNotBeGenerated() throws IOException {
        FollowUpTemplate testTemplate = new FollowUpTemplate(resolve("followup/Followup-template.xlsm"));
        subject = new FollowUpGenerator(provider, new SimpleSpreadsheetEditor(testTemplate), rowCalculator);

        List<Sheet> transitionsSheets = subject.generateTransitionsSheets(getEmptyFollowupData());
        assertEquals("Transitions sheets quantity", 0, transitionsSheets.size());

        FollowupData followupData = new FollowupData(null, getEmptyAnalyticsTransitionsDataSet(), getEmptySyntheticTransitionsDataSet());
        transitionsSheets = subject.generateTransitionsSheets(followupData);
        assertEquals("Transitions sheets quantity", 0, transitionsSheets.size());

        followupData = new FollowupData(null, null, null);
        transitionsSheets = subject.generateTransitionsSheets(followupData);
        assertEquals("Transitions sheets quantity", 0, transitionsSheets.size());

        AnalyticsTransitionsDataSet analytic = new AnalyticsTransitionsDataSet("", emptyList(), null);
        SyntheticTransitionsDataSet synthetic = new SyntheticTransitionsDataSet("", emptyList(), null);
        followupData = new FollowupData(null, asList(analytic), asList(synthetic));
        transitionsSheets = subject.generateTransitionsSheets(followupData);
        assertEquals("Transitions sheets quantity", 0, transitionsSheets.size());

        followupData = new FollowupData(null, getAnalyticsTransitionsDataSetWitNoRow(), getSyntheticTransitionsDataSetWithNoRow());
        subject.getEditor().open();
        transitionsSheets = subject.generateTransitionsSheets(followupData);
        assertEquals("Transitions sheets quantity", 2, transitionsSheets.size());
        subject.getEditor().close();
    }

    @Test
    public void generateJiraDataSheetTest2() throws IOException {
        
        //Data 1
        FromJiraDataRow data1row1 = getDefaultFromJiraDataRow();
        FromJiraDataRow data1row2 = getDefaultFromJiraDataRow();
        FromJiraDataRow data1row3 = getDefaultFromJiraDataRow();
        
        when(rowCalculator.calculate(data1row1)).thenReturn(new FromJiraRowCalculation(0, 2, 3));
        when(rowCalculator.calculate(data1row2)).thenReturn(new FromJiraRowCalculation(0, 2, 3));

        FollowUpDataEntry followUpDataEntry1 = followUpDataEntry(LocalDate.of(2017, 10, 1), asList(data1row1, data1row2, data1row3));
        
        //Data 2
        FromJiraDataRow data2row1 = getDefaultFromJiraDataRow();
        FromJiraDataRow data2row2 = getDefaultFromJiraDataRow();
        FromJiraDataRow data2row3 = getDefaultFromJiraDataRow();
        
        when(rowCalculator.calculate(data2row1)).thenReturn(new FromJiraRowCalculation(0, 5, 8.1));
        when(rowCalculator.calculate(data2row2)).thenReturn(new FromJiraRowCalculation(0, 6, 8.2));
        when(rowCalculator.calculate(data2row3)).thenReturn(new FromJiraRowCalculation(0, 2, 4.1));

        FollowUpDataEntry followUpDataEntry2 = followUpDataEntry(LocalDate.of(2017, 10, 2), asList(data2row1, data2row2, data2row3));

        //Data 3
        FromJiraDataRow data3row1 = getDefaultFromJiraDataRow();
        FromJiraDataRow data3row2 = getDefaultFromJiraDataRow();
        FromJiraDataRow data3row3 = getDefaultFromJiraDataRow();
        
        when(rowCalculator.calculate(data3row1)).thenReturn(new FromJiraRowCalculation(0, 1.9, 1.5));
        when(rowCalculator.calculate(data3row2)).thenReturn(new FromJiraRowCalculation(0,   0,   3));
        when(rowCalculator.calculate(data3row3)).thenReturn(new FromJiraRowCalculation(0,   2,   0));

        FollowUpDataEntry followUpDataEntry3 = followUpDataEntry(LocalDate.of(2017, 10, 3), asList(data3row1, data3row2, data3row3));
        
        List<String> projects = asList("P1", "P2");
        List<FollowUpDataEntry> entries = asList(followUpDataEntry1, followUpDataEntry2);
        mockProviderForEachHistoryEntry(projects, entries);

        subject = new FollowUpGenerator(provider, editor, rowCalculator);

        subject.getEditor().open();
        subject.generateEffortHistory(followUpDataEntry3, projects, ZoneId.of("Z"));
        subject.getEditor().close();

        String expectedEditorLogger = 
                "Spreadsheet Open\n" + 
                "Sheet Create: Effort History\n" + 
                "Sheet \"Effort History\" Row Create: 1\n" + 
                "Sheet \"Effort History\" Row \"1\" AddColumn \"A1\": Date\n" + 
                "Sheet \"Effort History\" Row \"1\" AddColumn \"B1\": SumEffortDone\n" + 
                "Sheet \"Effort History\" Row \"1\" AddColumn \"C1\": SumEffortBacklog\n" + 
                "Sheet \"Effort History\" Row \"1\" Save\n" + 
                "Sheet \"Effort History\" Row Create: 2\n" + 
                "Sheet \"Effort History\" Row \"2\" AddColumn \"A2\": 2017-10-01T00:00Z\n" + 
                "Sheet \"Effort History\" Row \"2\" AddColumn \"B2\": 4.0\n" + 
                "Sheet \"Effort History\" Row \"2\" AddColumn \"C2\": 6.0\n" + 
                "Sheet \"Effort History\" Row \"2\" Save\n" + 
                "Sheet \"Effort History\" Row Create: 3\n" + 
                "Sheet \"Effort History\" Row \"3\" AddColumn \"A3\": 2017-10-02T00:00Z\n" + 
                "Sheet \"Effort History\" Row \"3\" AddColumn \"B3\": 13.0\n" + 
                "Sheet \"Effort History\" Row \"3\" AddColumn \"C3\": 20.4\n" + 
                "Sheet \"Effort History\" Row \"3\" Save\n" + 
                "Sheet \"Effort History\" Row Create: 4\n" + 
                "Sheet \"Effort History\" Row \"4\" AddColumn \"A4\": 2017-10-03T00:00Z\n" + 
                "Sheet \"Effort History\" Row \"4\" AddColumn \"B4\": 3.9\n" + 
                "Sheet \"Effort History\" Row \"4\" AddColumn \"C4\": 4.5\n" + 
                "Sheet \"Effort History\" Row \"4\" Save\n" + 
                "Sheet \"Effort History\" Save\n" + 
                "Spreadsheet Close\n";

        assertEquals(expectedEditorLogger, editor.loggerString());
    }

    private FollowUpDataEntry followUpDataEntry(LocalDate data, List<FromJiraDataRow> rows) {
        FromJiraDataSet dataSet = new FromJiraDataSet(FROMJIRA_HEADERS, rows);
        FollowupData followupData = new FollowupData(dataSet, emptyList(), emptyList());
        return new FollowUpDataEntry(data, followupData);
    }

    private void mockProviderForEachHistoryEntry(List<String> projects, List<FollowUpDataEntry> entriesToReturn) {
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Consumer<FollowUpDataEntry> action = (Consumer<FollowUpDataEntry>) invocation.getArguments()[2];
            entriesToReturn.stream().forEach(action);
            return null;
        })
        .when(provider).forEachHistoryEntry(eq(projects), any(), any());
    }
    
    private String txtResourceAsString(String pathResource) {
        return IOUtilities.resourceToString(pathResource);
    }

    private static Resource resolve(String resourceName) {
        return IOUtilities.asResource(FollowUpGenerator.class.getClassLoader().getResource(resourceName));
    }

    private String[] emptyArray() {
        return new String[0];
    }

}
