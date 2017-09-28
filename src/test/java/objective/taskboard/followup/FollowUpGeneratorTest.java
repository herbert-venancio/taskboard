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
import static objective.taskboard.followup.FollowUpHelper.getDefaultAnalyticsTransitionsDataSet;
import static objective.taskboard.followup.FollowUpHelper.getDefaultFromJiraDataRow;
import static objective.taskboard.followup.FollowUpHelper.getDefaultFromJiraDataRowList;
import static objective.taskboard.followup.FollowUpHelper.getDefaultSyntheticTransitionsDataSet;
import static objective.taskboard.followup.FollowUpHelper.getEmptyAnalyticsTransitionsDataSet;
import static objective.taskboard.followup.FollowUpHelper.getEmptyFollowupData;
import static objective.taskboard.followup.FollowUpHelper.getEmptySyntheticTransitionsDataSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.ZoneId;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.springframework.core.io.Resource;

import objective.taskboard.followup.impl.FollowUpTemplateStorage;
import objective.taskboard.spreadsheet.SimpleSpreadsheetEditor;
import objective.taskboard.spreadsheet.SimpleSpreadsheetEditor.Sheet;
import objective.taskboard.utils.IOUtilities;
import objective.taskboard.utils.XmlUtils;

public class FollowUpGeneratorTest {

    private static final String PATH_FOLLOWUP_TEMPLATE = "followup/Followup-template.xlsm";

    @Test
    public void generateJiraDataSheetTest() {
        FollowupDataProvider provider = getMockFollowupDataProvider(getDefaultFromJiraDataRowList(), emptyList(), emptyList());
        FollowUpGenerator subject = getFollowUpGeneratorUsingTestTemplates(provider);

        subject.getEditor().open();
        String jiraDataSheet = subject.generateFromJiraSheet(provider.getJiraData(emptyArray(), ZoneId.systemDefault())).stringValue();

        String jiraDataSheetExpected = normalizedXmlResource("followup/generateJiraDataSheetTest.xml");
        assertEquals("Jira data sheet", jiraDataSheetExpected, jiraDataSheet);
    }

    @Test
    public void whenGeneratingWithoutData_generatesFromJiraKeepingOnlyHeaders() {
        FollowupDataProvider provider = getMockFollowupDataProvider(emptyList(), emptyList(), emptyList());
        FollowUpGenerator subject = getFollowUpGeneratorUsingTestTemplates(provider);

        subject.getEditor().open();
        String jiraDataSheet = subject.generateFromJiraSheet(getEmptyFollowupData()).stringValue();

        String jiraDataSheetExpected = normalizedXmlResource("followup/emptyFromJiraOnlyWithHeaders.xml");
        assertEquals("Jira data sheet", jiraDataSheetExpected, jiraDataSheet);
    }

    @Test
    public void generateJiraDataSheetWithSomeEmptyAndNullAttributesJiraDataTest() {
        FromJiraDataRow fromJiraDefault = getDefaultFromJiraDataRow();
        fromJiraDefault.project = "";
        fromJiraDefault.demandType = null;
        fromJiraDefault.taskId = 0L;
        fromJiraDefault.subtaskId = null;
        fromJiraDefault.worklog = 0.0;
        fromJiraDefault.wrongWorklog = null;
        FollowupDataProvider provider = getMockFollowupDataProvider(asList(fromJiraDefault), emptyList(), emptyList());
        FollowUpGenerator subject = getFollowUpGeneratorUsingTestTemplates(provider);

        subject.getEditor().open();
        String jiraDataSheet = subject.generateFromJiraSheet(provider.getJiraData(emptyArray(), ZoneId.systemDefault())).stringValue();

        String jiraDataSheetExpected = normalizedXmlResource("followup/generateJiraDataSheetWithSomeEmptyAndNullAttributesJiraDataTest.xml");
        assertEquals("Jira data sheet", jiraDataSheetExpected, jiraDataSheet);
    }

    @Test
    public void generateTest() {
        FollowupDataProvider provider = getMockFollowupDataProvider(getDefaultFromJiraDataRowList(), getDefaultAnalyticsTransitionsDataSet(),
                getDefaultSyntheticTransitionsDataSet());
        FollowUpGenerator subject = getFollowUpGeneratorUsingTestTemplates(provider);
        Resource resource = subject.generate(emptyArray(), ZoneId.systemDefault());
        assertNotNull("Resource shouldn't be null", resource);
    }

    @Test
    public void generateUsingDefaultTemplatesTest() {
        FollowupDataProvider provider = getMockFollowupDataProvider(getDefaultFromJiraDataRowList(), getDefaultAnalyticsTransitionsDataSet(),
                getDefaultSyntheticTransitionsDataSet());
        FollowUpGenerator subject = getDefaultFollowUpGenerator(provider);
        Resource resource = subject.generate(emptyArray(), ZoneId.systemDefault());
        assertNotNull("Resource shouldn't be null", resource);
    }
    
    @Test
    public void generateLotsOfLines() {
        List<FromJiraDataRow> fromJiraDataRowList = new LinkedList<>();
        for (int i=0; i < 5000; i++) 
            fromJiraDataRowList.add(getDefaultFromJiraDataRow());

        FollowupDataProvider provider = getMockFollowupDataProvider(fromJiraDataRowList, getDefaultAnalyticsTransitionsDataSet(),
                getDefaultSyntheticTransitionsDataSet());
        FollowUpGenerator subject = getFollowUpGeneratorUsingTestTemplates(provider);
        Resource resource = subject.generate(emptyArray(), ZoneId.systemDefault());
        assertNotNull("Resource shouldn't be null", resource);
    }

    @Test
    public void givenOneIssue_whenGenerateTransitionsSheets_thenSheetsShouldBeGenerated() {
        FollowupDataProvider provider = getMockFollowupDataProvider(null, getDefaultAnalyticsTransitionsDataSet(),
                getDefaultSyntheticTransitionsDataSet());
        FollowUpGenerator subject = getFollowUpGeneratorUsingTestTemplates(provider);

        subject.getEditor().open();
        List<Sheet> transitionsSheets = subject.generateTransitionsSheets(provider.getJiraData(emptyArray(), ZoneId.systemDefault()));
        assertEquals("Transitions sheets quantity", 2, transitionsSheets.size());

        String analyticTransitionsSheetExpected = normalizedXmlResource("followup/analyticTransitionsWithOneIssue.xml");
        assertEquals("Analytic transitions sheet", analyticTransitionsSheetExpected, transitionsSheets.get(0).stringValue());
        String syntheticTransitionsSheetExpected = normalizedXmlResource("followup/syntheticTransitionsWithOneIssue.xml");
        assertEquals("Synthetic transitions sheet", syntheticTransitionsSheetExpected, transitionsSheets.get(1).stringValue());
    }

    @Test
    public void givenNoIssue_whenGenerateTransitionsSheets_thenSheetsShouldNotBeGenerated() {
        FollowupDataProvider provider = getMockFollowupDataProvider(null, emptyList(), emptyList());
        FollowUpGenerator subject = getFollowUpGeneratorUsingTestTemplates(provider);

        subject.getEditor().open();
        List<Sheet> transitionsSheets = subject.generateTransitionsSheets(provider.getJiraData(emptyArray(), ZoneId.systemDefault()));
        assertEquals("Transitions sheets quantity", 0, transitionsSheets.size());

        provider = getMockFollowupDataProvider(null, getEmptyAnalyticsTransitionsDataSet(), getEmptySyntheticTransitionsDataSet());
        subject = getFollowUpGeneratorUsingTestTemplates(provider);

        subject.getEditor().open();
        transitionsSheets = subject.generateTransitionsSheets(provider.getJiraData(emptyArray(), ZoneId.systemDefault()));
        assertEquals("Transitions sheets quantity", 0, transitionsSheets.size());
    }

    private FollowUpGenerator getDefaultFollowUpGenerator(FollowupDataProvider provider) {
        FollowUpTemplate template = new FollowUpTemplate(resolve("followup-template/Followup-template.xlsm"));
        return new FollowUpGenerator(provider, new SimpleSpreadsheetEditor(template));
    }

    private FollowUpGenerator getFollowUpGeneratorUsingTestTemplates(FollowupDataProvider provider) {
        return new FollowUpGenerator(provider, new SimpleSpreadsheetEditor(getBasicTemplate()));
    }

    private FollowUpTemplate getBasicTemplate() {
        return new FollowUpTemplate(resolve(PATH_FOLLOWUP_TEMPLATE));
    }

    private FollowupDataProvider getMockFollowupDataProvider(List<FromJiraDataRow> fromJiraDataRowList,
            List<AnalyticsTransitionsDataSet> analyticTransitionsDataSets,
            List<SyntheticTransitionsDataSet> syntheticTransitionsDataSets) {
        FromJiraDataSet fromJiraDs = new FromJiraDataSet(FROMJIRA_HEADERS, fromJiraDataRowList);
        FollowupData followupData = new FollowupData(fromJiraDs, analyticTransitionsDataSets, syntheticTransitionsDataSets);
        FollowupDataProvider provider = mock(FollowupDataProvider.class);
        when(provider.getJiraData(emptyArray(), ZoneId.systemDefault())).thenReturn(followupData);
        return provider;
    }

    private String normalizedXmlResource(String pathResource) {
        return XmlUtils.normalizeXml(IOUtilities.resourceToString(pathResource));
    }

    private static Resource resolve(String resourceName) {
        return IOUtilities.asResource(FollowUpTemplateStorage.class.getClassLoader().getResource(resourceName));
    }

    private String[] emptyArray() {
        return new String[0];
    }
}
