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
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.followup.FollowUpGenerator;

@RunWith(MockitoJUnitRunner.class)
public class FollowUpGeneratorTest {

    @InjectMocks
    private FollowUpGenerator subject;

    @Mock
    private FollowupDataProvider provider;

    @Test
    public void getSharedStringsTemplateSuccessTest() {
        Map<String, Long> sharedStrings = subject.getSharedStringsTemplate("followup-template/sharedStrings-template.xml");
        assertEquals("Shared strings size", 457, sharedStrings.size());
        assertEquals("First shared string", 0L, sharedStrings.get("project").longValue());
        assertEquals("Any shared string", 223L, sharedStrings.get("Feature | 00236 - Global Solutions").longValue());
        assertEquals("Last shared string", 456L, sharedStrings.get("AllocatedHours").longValue());
    }

    @Test
    public void getSharedStringsTemplateErrorTest() {
        Map<String, Long> sharedStrings = subject.getSharedStringsTemplate("aaaaa");
        assertTrue("Shared strings should be empty", sharedStrings.isEmpty());
    }

//    @Test
//    public void generateJiraDataSheetSuccesTest() {
//        FollowUpData jiraData = new FollowUpData();
//        
//        when(provider.getJiraData()).thenReturn(asList(jiraData));
//        String jiraDataSheet = subject.generateJiraDataSheet();
//        assertEquals("Jira data sheet", )
//    }

}
