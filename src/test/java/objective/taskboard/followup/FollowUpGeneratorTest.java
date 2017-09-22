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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.springframework.core.io.Resource;
import org.xml.sax.SAXException;

import objective.taskboard.followup.impl.FollowUpTemplateStorage;
import objective.taskboard.spreadsheet.SimpleSpreadsheetEditor;
import objective.taskboard.utils.IOUtilities;
import objective.taskboard.utils.XmlUtils;

public class FollowUpGeneratorTest {

    private static final String PATH_FOLLOWUP_TEMPLATE = "followup/Followup-template.xlsm";

    @Test
    public void generateJiraDataSheetTest() throws ParserConfigurationException, SAXException, IOException {
        FollowupDataProvider provider = getFollowupDataProvider(FollowUpHelper.getFollowUpDataDefaultList());
        FollowUpGenerator subject = getFollowUpGeneratorUsingTestTemplates(provider);

        subject.getEditor().open();
        String jiraDataSheet = subject.generateJiraDataSheet(subject.getEditor(), provider.getJiraData(new String[0])).stringValue();

        String jiraDataSheetExpected = normalizedXmlResource("followup/generateJiraDataSheetTest.xml");
        assertEquals("Jira data sheet", jiraDataSheetExpected, jiraDataSheet);
    }

    @Test
    public void whenGeneratingWithoutData_generatesFromJiraKeepingOnlyHeaders() throws ParserConfigurationException, SAXException, IOException {
        FollowupDataProvider provider = getFollowupDataProvider(Collections.emptyList());
        FollowUpGenerator subject = getFollowUpGeneratorUsingTestTemplates(provider);

        subject.getEditor().open();
        String jiraDataSheet = subject.generateJiraDataSheet(subject.getEditor(), emptyFollowupData()).stringValue();

        String jiraDataSheetExpected = normalizedXmlResource("followup/emptyFromJiraOnlyWithHeaders.xml");
        assertEquals("Jira data sheet", jiraDataSheetExpected, jiraDataSheet);
    }

    @Test
    public void generateJiraDataSheetWithSomeEmptyAndNullAttributesJiraDataTest() throws ParserConfigurationException, SAXException, IOException {
        FromJiraDataRow followupDefault = FollowUpHelper.getDefaultFromJiraDataRow();
        followupDefault.project = "";
        followupDefault.demandType = null;
        followupDefault.taskId = 0L;
        followupDefault.subtaskId = null;
        followupDefault.worklog = 0.0;
        followupDefault.wrongWorklog = null;
        FollowupDataProvider provider = getFollowupDataProvider(asList(followupDefault));
        FollowUpGenerator subject = getFollowUpGeneratorUsingTestTemplates(provider);

        subject.getEditor().open();
        String jiraDataSheet = subject.generateJiraDataSheet(subject.getEditor(), provider.getJiraData(new String[0])).stringValue();

        String jiraDataSheetExpected = normalizedXmlResource("followup/generateJiraDataSheetWithSomeEmptyAndNullAttributesJiraDataTest.xml");
        assertEquals("Jira data sheet", jiraDataSheetExpected, jiraDataSheet);
    }

    @Test
    public void generateTest() throws Exception {
        FollowupDataProvider provider = getFollowupDataProvider(FollowUpHelper.getFollowUpDataDefaultList());
        FollowUpGenerator subject = getFollowUpGeneratorUsingTestTemplates(provider);
        Resource resource = subject.generate(new String[0]);
        assertNotNull("Resource shouldn't be null", resource);
    }

    @Test
    public void generateUsingDefaultTemplatesTest() throws Exception {
        FollowupDataProvider provider = getFollowupDataProvider(FollowUpHelper.getFollowUpDataDefaultList());
        FollowUpGenerator subject = getDefaultFollowUpGenerator(provider);
        Resource resource = subject.generate(new String[0]);
        assertNotNull("Resource shouldn't be null", resource);
    }
    
    @Test
    public void generateLotsOfLines() throws Exception {
        List<FromJiraDataRow> l = new LinkedList<>();
        for (int i=0; i < 5000; i++) 
            l.add(FollowUpHelper.getDefaultFromJiraDataRow());
        
        FollowupDataProvider provider = getFollowupDataProvider(l);
        FollowUpGenerator subject = getFollowUpGeneratorUsingTestTemplates(provider);
        Resource resource = subject.generate(new String[0]);
        assertNotNull("Resource shouldn't be null", resource);
    }

    private FollowUpGenerator getDefaultFollowUpGenerator(FollowupDataProvider provider) {
        return new FollowUpGenerator(provider, new SimpleSpreadsheetEditor(new FollowUpTemplate(resolve("followup-template/Followup-template.xlsm"))
        ));
    }

    private FollowUpGenerator getFollowUpGeneratorUsingTestTemplates(FollowupDataProvider provider) {
        return new FollowUpGenerator(provider, new SimpleSpreadsheetEditor(getBasicTemplate()));
    }

    private FollowUpTemplate getBasicTemplate() {
        return new FollowUpTemplate(resolve(PATH_FOLLOWUP_TEMPLATE));
    }

    private FollowupDataProvider getFollowupDataProvider(List<FromJiraDataRow> jiraData) {
        FollowupData followupData = new FollowupData(new FromJiraDataSet(FROMJIRA_HEADERS, jiraData), emptyList(), emptyList());
        FollowupDataProvider provider = mock(FollowupDataProvider.class);
        when(provider.getJiraData(new String[0])).thenReturn(followupData);
        return provider;
    }

    private String normalizedXmlResource(String pathResource) {
        return XmlUtils.normalizeXml(IOUtilities.resourceToString(pathResource));
    }

    private FollowupData emptyFollowupData() {
        FromJiraDataSet dataSet = new FromJiraDataSet(FROMJIRA_HEADERS, Collections.emptyList());
        
        return new FollowupData(dataSet, emptyList(), emptyList());
    }

    private static Resource resolve(String resourceName) {
        return IOUtilities.asResource(FollowUpTemplateStorage.class.getClassLoader().getResource(resourceName));
    }
}
