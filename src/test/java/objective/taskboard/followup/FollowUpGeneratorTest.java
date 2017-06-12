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
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ByteArrayResource;
import org.xml.sax.SAXException;

@RunWith(MockitoJUnitRunner.class)
public class FollowUpGeneratorTest {

    private static final String MSG_ASSERT_SHARED_STRINGS_SIZE = "Shared strings size";

    @InjectMocks
    private FollowUpGenerator subject;

    @Mock
    private FollowupDataProvider provider;

    @Test
    public void getSharedStringsInitialTest() throws ParserConfigurationException, SAXException, IOException {
        Map<String, Long> sharedStrings = subject.getSharedStringsInitial();
        assertEquals(MSG_ASSERT_SHARED_STRINGS_SIZE, 248, sharedStrings.size());
        assertEquals("First shared string", 0, sharedStrings.get("project").longValue());
        assertEquals("Some special character shared string", 48, sharedStrings.get("Demand Status > Demand > Task Status > Task > Subtask").longValue());
        assertEquals("Any shared string", 133, sharedStrings.get("Group %").longValue());
        assertEquals("Last shared string", 247, sharedStrings.get("Rótulos de Coluna").longValue());
    }

    @Test
    public void addNewSharedStringsInTheEndTest() throws ParserConfigurationException, SAXException, IOException {
        when(provider.getJiraData()).thenReturn(asList(getFollowUpDataDefault()));

        Map<String, Long> sharedStrings = subject.getSharedStringsInitial();
        assertEquals(MSG_ASSERT_SHARED_STRINGS_SIZE, 248, sharedStrings.size());
        assertEquals("First shared string", 0, sharedStrings.get("project").longValue());
        assertEquals("Last shared string", 247, sharedStrings.get("Rótulos de Coluna").longValue());

        subject.generateJiraDataSheet(sharedStrings);

        assertEquals(MSG_ASSERT_SHARED_STRINGS_SIZE, 262, sharedStrings.size());
        assertEquals("First new shared string", 248, sharedStrings.get("PROJECT TEST").longValue());
        assertEquals("Any new shared string", 254, sharedStrings.get("Summary Feature").longValue());
        assertEquals("Last new shared string", 261, sharedStrings.get("Full Description Sub-task").longValue());
    }

    @Test
    public void generateSharedStringsInOrderTest() throws ParserConfigurationException, SAXException, IOException {
        Map<String, Long> sharedStrings = subject.getSharedStringsInitial();
        String sharedStringsGenerated = subject.generateSharedStrings(sharedStrings);

        String sharedStringsExpected = getStringExpected("followup/generateSharedStringsInOrderTest.xml");
        assertEquals("Shared strings", sharedStringsExpected, sharedStringsGenerated);
    }

    @Test
    public void generateSharedStringsInOrderAfterAddNewSharedStringTest() throws ParserConfigurationException, SAXException, IOException {
        when(provider.getJiraData()).thenReturn(asList(getFollowUpDataDefault()));

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
        when(provider.getJiraData()).thenReturn(asList(getFollowUpDataDefault()));

        Map<String, Long> sharedStrings = subject.getSharedStringsInitial();
        String jiraDataSheet = subject.generateJiraDataSheet(sharedStrings);

        String jiraDataSheetExpected = getStringExpected("followup/generateJiraDataSheetTest.xml");
        assertEquals("Jira data sheet", jiraDataSheetExpected, jiraDataSheet);
    }

    @Test
    public void generateJiraDataSheetWithEmptyDataTest() throws ParserConfigurationException, SAXException, IOException {
        when(provider.getJiraData()).thenReturn(asList());

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
        when(provider.getJiraData()).thenReturn(asList(followupDefault));

        Map<String, Long> sharedStrings = subject.getSharedStringsInitial();
        String jiraDataSheet = subject.generateJiraDataSheet(sharedStrings);

        String jiraDataSheetExpected = getStringExpected("followup/generateJiraDataSheetWithSomeEmptyAndNullAttributesJiraDataTest.xml");
        assertEquals("Jira data sheet", jiraDataSheetExpected, jiraDataSheet);
    }

    @Test
    public void generateTest() throws Exception {
        when(provider.getJiraData()).thenReturn(asList(getFollowUpDataDefault()));
        ByteArrayResource resource = subject.generate();
        assertNotNull("Resource shouldn't be null", resource);
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
