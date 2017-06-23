package objective.taskboard.followup;

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

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ByteArrayResource;
import org.xml.sax.SAXException;

@RunWith(MockitoJUnitRunner.class)
public class FollowUpGeneratorTest {

    private static final String PATH_SHARED_STRINGS_INITIAL = "followup/sharedStrings-initial.xml";
    private static final String PATH_SHARED_STRINGS_TEMPLATE = "followup/sharedStrings-template.xml";
    private static final String PATH_SHARED_STRINGS_SI_TEMPLATE = "followup/sharedStrings-si-template.xml";
    private static final String PATH_SHEET7_TEMPLATE = "followup/sheet7-template.xml";
    private static final String PATH_SHEET7_ROW_TEMPLATE = "followup/sheet7-row-template.xml";
    private static final String PATH_FOLLOWUP_TEMPLATE = "followup/Followup-template.xlsm";

    private static final String MSG_ASSERT_SHARED_STRINGS_SIZE = "Shared strings size";

    @Test
    public void getSharedStringsInitialTest() throws ParserConfigurationException, SAXException, IOException {
        FollowUpGenerator subject = getFollowUpGeneratorUsingTestTemplates(null);
        Map<String, Long> sharedStrings = subject.getSharedStringsInitial();
        assertEquals(MSG_ASSERT_SHARED_STRINGS_SIZE, 248, sharedStrings.size());
        assertEquals("First shared string", 0, sharedStrings.get("project").longValue());
        assertEquals("Some special character shared string", 48, sharedStrings.get("Demand Status > Demand > Task Status > Task > Subtask").longValue());
        assertEquals("Any shared string", 131, sharedStrings.get("Group %").longValue());
        assertEquals("Last shared string", 247, sharedStrings.get("Column Labels").longValue());
    }

    @Test
    public void addNewSharedStringsInTheEndTest() throws ParserConfigurationException, SAXException, IOException {
        FollowupDataProvider provider = getFollowupDataProvider(asList(getFollowUpDataDefault()));
        FollowUpGenerator subject = getFollowUpGeneratorUsingTestTemplates(provider);

        Map<String, Long> sharedStrings = subject.getSharedStringsInitial();
        assertEquals(MSG_ASSERT_SHARED_STRINGS_SIZE, 248, sharedStrings.size());
        assertEquals("First shared string", 0, sharedStrings.get("project").longValue());
        assertEquals("Last shared string", 247, sharedStrings.get("Column Labels").longValue());

        subject.generateJiraDataSheet(sharedStrings);

        assertEquals(MSG_ASSERT_SHARED_STRINGS_SIZE, 262, sharedStrings.size());
        assertEquals("First new shared string", 248, sharedStrings.get("PROJECT TEST").longValue());
        assertEquals("Any new shared string", 254, sharedStrings.get("Summary Feature").longValue());
        assertEquals("Last new shared string", 261, sharedStrings.get("Full Description Sub-task").longValue());
    }

    @Test
    public void generateSharedStringsInOrderTest() throws ParserConfigurationException, SAXException, IOException {
        FollowUpGenerator subject = getFollowUpGeneratorUsingTestTemplates(null);

        Map<String, Long> sharedStrings = subject.getSharedStringsInitial();
        String sharedStringsGenerated = subject.generateSharedStrings(sharedStrings);

        String sharedStringsExpected = getStringExpected("followup/generateSharedStringsInOrderTest.xml");
        assertEquals("Shared strings", sharedStringsExpected, sharedStringsGenerated);
    }

    @Test
    public void generateSharedStringsInOrderAfterAddNewSharedStringTest() throws ParserConfigurationException, SAXException, IOException {
        FollowupDataProvider provider = getFollowupDataProvider(asList(getFollowUpDataDefault()));
        FollowUpGenerator subject = getFollowUpGeneratorUsingTestTemplates(provider);

        Map<String, Long> sharedStrings = subject.getSharedStringsInitial();

        assertEquals(MSG_ASSERT_SHARED_STRINGS_SIZE, 248, sharedStrings.size());
        subject.generateJiraDataSheet(sharedStrings);
        assertEquals(MSG_ASSERT_SHARED_STRINGS_SIZE, 262, sharedStrings.size());

        String sharedStringsGenerated = subject.generateSharedStrings(sharedStrings);

        String sharedStringsExpected = getStringExpected("followup/generateSharedStringsInOrderAfterAddNewSharedStringTest.xml");
        assertEquals("Shared strings", sharedStringsExpected, sharedStringsGenerated);
    }

    @Test
    public void generateJiraDataSheetTest() throws ParserConfigurationException, SAXException, IOException {
        FollowupDataProvider provider = getFollowupDataProvider(asList(getFollowUpDataDefault()));
        FollowUpGenerator subject = getFollowUpGeneratorUsingTestTemplates(provider);

        Map<String, Long> sharedStrings = subject.getSharedStringsInitial();
        String jiraDataSheet = subject.generateJiraDataSheet(sharedStrings);

        String jiraDataSheetExpected = getStringExpected("followup/generateJiraDataSheetTest.xml");
        assertEquals("Jira data sheet", jiraDataSheetExpected, jiraDataSheet);
    }

    @Test
    public void generateJiraDataSheetWithEmptyDataTest() throws ParserConfigurationException, SAXException, IOException {
        FollowupDataProvider provider = getFollowupDataProvider(asList());
        FollowUpGenerator subject = getFollowUpGeneratorUsingTestTemplates(provider);

        Map<String, Long> sharedStrings = subject.getSharedStringsInitial();
        String jiraDataSheet = subject.generateJiraDataSheet(sharedStrings);

        String jiraDataSheetExpected = getStringExpected("followup/generateJiraDataSheetWithEmptyDataTest.xml");
        assertEquals("Jira data sheet", jiraDataSheetExpected, jiraDataSheet);
    }

    @Test
    public void generateJiraDataSheetWithSomeEmptyAndNullAttributesJiraDataTest() throws ParserConfigurationException, SAXException, IOException {
        FollowUpData followupDefault = getFollowUpDataDefault();
        followupDefault.project = "";
        followupDefault.demandType = null;
        followupDefault.taskId = 0L;
        followupDefault.subtaskId = null;
        followupDefault.worklog = 0.0;
        followupDefault.wrongWorklog = null;
        FollowupDataProvider provider = getFollowupDataProvider(asList(followupDefault));
        FollowUpGenerator subject = getFollowUpGeneratorUsingTestTemplates(provider);

        Map<String, Long> sharedStrings = subject.getSharedStringsInitial();
        String jiraDataSheet = subject.generateJiraDataSheet(sharedStrings);

        String jiraDataSheetExpected = getStringExpected("followup/generateJiraDataSheetWithSomeEmptyAndNullAttributesJiraDataTest.xml");
        assertEquals("Jira data sheet", jiraDataSheetExpected, jiraDataSheet);
    }

    @Test
    public void generateTest() throws Exception {
        FollowupDataProvider provider = getFollowupDataProvider(asList(getFollowUpDataDefault()));
        FollowUpGenerator subject = getFollowUpGeneratorUsingTestTemplates(provider);
        ByteArrayResource resource = subject.generate();
        assertNotNull("Resource shouldn't be null", resource);
    }

    @Test
    public void generateUsingDefaultTemplatesTest() throws Exception {
        FollowupDataProvider provider = getFollowupDataProvider(asList(getFollowUpDataDefault()));
        FollowUpGenerator subject = new FollowUpGenerator(provider);
        ByteArrayResource resource = subject.generate();
        assertNotNull("Resource shouldn't be null", resource);
    }

    private FollowUpGenerator getFollowUpGeneratorUsingTestTemplates(FollowupDataProvider provider) {
        return new FollowUpGenerator(provider, PATH_SHARED_STRINGS_INITIAL, PATH_SHARED_STRINGS_TEMPLATE,
                PATH_SHARED_STRINGS_SI_TEMPLATE, PATH_SHEET7_TEMPLATE, PATH_SHEET7_ROW_TEMPLATE,
                PATH_FOLLOWUP_TEMPLATE);
    }

    private FollowupDataProvider getFollowupDataProvider(List<FollowUpData> jiraData) {
        FollowupDataProvider provider = mock(FollowupDataProvider.class);
        when(provider.getJiraData()).thenReturn(jiraData);
        return provider;
    }

    private FollowUpData getFollowUpDataDefault() {
        FollowUpData followUpData = new FollowUpData();
        followUpData.planningType = "Ballpark";
        followUpData.project = "PROJECT TEST";
        followUpData.demandType = "Demand";
        followUpData.demandStatus = "Doing";
        followUpData.demandId = 1L;
        followUpData.demandNum = "I-1";
        followUpData.demandSummary = "Summary Demand";
        followUpData.demandDescription = "Description Demand";
        followUpData.taskType = "Feature";
        followUpData.taskStatus = "Doing";
        followUpData.taskId = 2L;
        followUpData.taskNum = "I-2";
        followUpData.taskSummary = "Summary Feature";
        followUpData.taskDescription = "Description Feature";
        followUpData.taskFullDescription = "Full Description Feature";
        followUpData.taskRelease = "Release";
        followUpData.subtaskType = "Sub-task";
        followUpData.subtaskStatus = "Doing";
        followUpData.subtaskId = 3L;
        followUpData.subtaskNum = "I-3";
        followUpData.subtaskSummary = "Summary Sub-task";
        followUpData.subtaskDescription = "Description Sub-task";
        followUpData.subtaskFullDescription = "Full Description Sub-task";
        followUpData.tshirtSize = "M";
        followUpData.worklog = 1D;
        followUpData.wrongWorklog = 1D;
        followUpData.demandBallpark = 1D;
        followUpData.taskBallpark = 1D;
        followUpData.queryType = "Type";
        return followUpData;
    }

    private String getStringExpected(String pathResource) throws IOException {
        InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream(pathResource);
        return IOUtils.toString(inputStream, "UTF-8");
    }
}
