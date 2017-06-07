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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import objective.taskboard.data.Issue;
import objective.taskboard.issueBuffer.IssueBufferService;
import objective.taskboard.jira.JiraProperties;
import objective.taskboard.jira.JiraProperties.BallparkMapping;
import objective.taskboard.jira.JiraProperties.CustomField;
import objective.taskboard.jira.JiraProperties.CustomField.CustomFieldDetails;
import objective.taskboard.jira.JiraProperties.CustomField.TShirtSize;
import objective.taskboard.jira.JiraProperties.IssueLink;
import objective.taskboard.jira.JiraProperties.IssueLink.LinkDetails;
import objective.taskboard.jira.JiraProperties.IssueType.IssueTypeDetails;
import objective.taskboard.jira.MetadataService;

@RunWith(MockitoJUnitRunner.class)
public class FollowupDataProviderImplTest {
    @Mock
    private JiraProperties jiraProperties;
    
    @Mock
    private MetadataService metadataService;
    
    @Mock
    private IssueBufferService issueBufferService;
    
    CustomField propertiesCustomField;
    private TShirtSize tshirtSizeInfo;
    
    private JiraProperties.Followup followup = new JiraProperties.Followup();
    
    @InjectMocks
    FollowupDataProviderImpl subject;
    
    private static final long demandIssueType  = 13;
    private static final long taskIssueType    = 12; 
    private static final long devIssueType     = 14;
    private static final long alphaIssueType   = 15;
    private static final long reviewIssueType  = 16;
    private static final long deployIssueType  = 17;
    private static final long frontEndIssueType     = 18;
    
    private static final long statusOpen = 11L;
    private static final long statusToDo = 13L;
    private static final long statusInProgress = 15L;
    private static final long statusCancelled = 16L;
    private static final long statusDone = 17L;
    
    @Before
    public void before() throws InterruptedException, ExecutionException {
        Map<Long, Status> statusMap = new LinkedHashMap<>();
        statusMap.put(statusOpen,       new Status(null, statusOpen,       "Open", null, null));
        statusMap.put(statusToDo,       new Status(null, statusToDo,       "To Do", null, null));
        statusMap.put(statusInProgress, new Status(null, statusInProgress, "In Progress", null, null));
        statusMap.put(statusCancelled,  new Status(null, statusCancelled,  "Cancelled", null, null));
        statusMap.put(statusDone,       new Status(null, statusDone,       "Done", null, null));
        when(metadataService.getStatusesMetadata()).thenReturn(statusMap );
        
        Map<Long, IssueType> issueTypeMap = new LinkedHashMap<>();
        issueTypeMap.put(demandIssueType, new IssueType(null, demandIssueType, "Demand", false, null,null));
        issueTypeMap.put(taskIssueType,   new IssueType(null, taskIssueType,   "Task", false, null,null));
        issueTypeMap.put(devIssueType,    new IssueType(null, devIssueType,    "Dev", false, null,null));
        issueTypeMap.put(alphaIssueType,  new IssueType(null, alphaIssueType,  "Alpha", false, null,null));
        when(metadataService.getIssueTypeMetadata()).thenReturn(issueTypeMap);
        
        // tshirt size information
        tshirtSizeInfo = new TShirtSize();
        tshirtSizeInfo.setMainTShirtSizeFieldId("MAINID");
        propertiesCustomField = new CustomField();
        propertiesCustomField.setTShirtSize(tshirtSizeInfo);
        when(jiraProperties.getCustomfield()).thenReturn(propertiesCustomField);
        
        IssueLink issueLink = new IssueLink(new LinkDetails("Demand"));
        when(jiraProperties.getIssuelink()).thenReturn(issueLink);
        
        JiraProperties.IssueType issueType = new JiraProperties.IssueType();
        issueType.setFeatures(Arrays.asList(new IssueTypeDetails(taskIssueType)));
        
        when(jiraProperties.getIssuetype()).thenReturn(issueType);
        when(jiraProperties.getFollowup()).thenReturn(followup);
        propertiesCustomField.setRelease(new CustomFieldDetails("RELEASE_CF_ID"));

    }
    
    @Test
    public void demandWithoutChildFeatures_shouldCreateASingleBallpark() {
        issues( 
            demand().id(1).key("PROJ-1").summary("Smry 1").originalEstimateInHours(1).timeSpentInHours(10)
        );
        
        assertFollowupsForIssuesEquals(
            " planningType           : Ballpark\n" + 
            " project                : A Project\n" + 
            " demandType             : Demand\n" + 
            " demandStatus           : To Do\n" + 
            " demandId               : 1\n" + 
            " demandNum              : PROJ-1\n" + 
            " demandSummary          : Smry 1\n" + 
            " demandDescription      : M | 00001 - Smry 1\n" + 
            " taskType               : BALLPARK - Demand\n" + 
            " taskStatus             : To Do\n" + 
            " taskId                 : 0\n" + 
            " taskNum                : PROJ-1\n" + 
            " taskSummary            : Dummy Feature\n" + 
            " taskDescription        : 00000 - Smry 1\n" + 
            " taskFullDescription    : BALLPARK - Demand | M | 00000 - Smry 1\n" + 
            " taskRelease            : No release set\n" + 
            " subtaskType            : BALLPARK - Demand\n" + 
            " subtaskStatus          : To Do\n" + 
            " subtaskId              : 0\n" + 
            " subtaskNum             : PROJ-0\n" + 
            " subtaskSummary         : Smry 1\n" + 
            " subtaskDescription     : M | 00000 - Smry 1\n" + 
            " subtaskFullDescription : BALLPARK - Demand | M | 00000 - Smry 1\n" + 
            " tshirtSize             : M\n" + 
            " worklog                : 0.0\n" + 
            " wrongWorklog           : 10.0\n" + 
            " demandBallpark         : 1.0\n" + 
            " taskBallpark           : 0.0\n" + 
            " queryType              : DEMAND BALLPARK"
        );
    }
    
    @Test
    public void demandWithRelease_shouldCreateABallparkWithReleaseInfo() {
        issues( 
            demand().id(1).key("PROJ-1").summary("Smry 1").originalEstimateInHours(1).timeSpentInHours(10).release("Release 42")
        );
        
        assertFollowupsForIssuesEquals(
            " planningType           : Ballpark\n" + 
            " project                : A Project\n" + 
            " demandType             : Demand\n" + 
            " demandStatus           : To Do\n" + 
            " demandId               : 1\n" + 
            " demandNum              : PROJ-1\n" + 
            " demandSummary          : Smry 1\n" + 
            " demandDescription      : M | 00001 - Smry 1\n" + 
            " taskType               : BALLPARK - Demand\n" + 
            " taskStatus             : To Do\n" + 
            " taskId                 : 0\n" + 
            " taskNum                : PROJ-1\n" + 
            " taskSummary            : Dummy Feature\n" + 
            " taskDescription        : 00000 - Smry 1\n" + 
            " taskFullDescription    : BALLPARK - Demand | M | 00000 - Smry 1\n" + 
            " taskRelease            : Release 42\n" + 
            " subtaskType            : BALLPARK - Demand\n" + 
            " subtaskStatus          : To Do\n" + 
            " subtaskId              : 0\n" + 
            " subtaskNum             : PROJ-0\n" + 
            " subtaskSummary         : Smry 1\n" + 
            " subtaskDescription     : M | 00000 - Smry 1\n" + 
            " subtaskFullDescription : BALLPARK - Demand | M | 00000 - Smry 1\n" + 
            " tshirtSize             : M\n" + 
            " worklog                : 0.0\n" + 
            " wrongWorklog           : 10.0\n" + 
            " demandBallpark         : 1.0\n" + 
            " taskBallpark           : 0.0\n" + 
            " queryType              : DEMAND BALLPARK"
        );
    }
    
    @Test
    public void subtaskWithDemandAndSubtask_shouldCreateOnlyOneSubTaskAndNoBallparks() {
        configureBallparkMappings(
                taskIssueType + " : \n" +
                "  - issueType : BALLPARK - Development\n" + 
                "    tshirtCustomFieldId: Dev_Tshirt\n" + 
                "    jiraIssueTypes:\n" + 
                "      - " + devIssueType + "\n"
                );
        issues( 
                demand().id(1).key("PROJ-1").summary("Smry 1").originalEstimateInHours(1).timeSpentInHours(10),
                demand().id(2).key("PROJ-2").summary("Smry 2").originalEstimateInHours(1),
                task().id(3).key("PROJ-3").summary("Smry 3").originalEstimateInHours(2).parent("PROJ-2"),
                subtask().id(4).key("PROJ-4").summary("Smry 4").timeSpentInHours(5).parent("PROJ-3").issueType(devIssueType).tshirtSize("XL")
        );
        
        assertFollowupsForIssuesEquals(
            " planningType           : Ballpark\n" + 
            " project                : A Project\n" + 
            " demandType             : Demand\n" + 
            " demandStatus           : To Do\n" + 
            " demandId               : 1\n" + 
            " demandNum              : PROJ-1\n" + 
            " demandSummary          : Smry 1\n" + 
            " demandDescription      : M | 00001 - Smry 1\n" + 
            " taskType               : BALLPARK - Demand\n" + 
            " taskStatus             : To Do\n" + 
            " taskId                 : 0\n" + 
            " taskNum                : PROJ-1\n" + 
            " taskSummary            : Dummy Feature\n" + 
            " taskDescription        : 00000 - Smry 1\n" + 
            " taskFullDescription    : BALLPARK - Demand | M | 00000 - Smry 1\n" + 
            " taskRelease            : No release set\n" + 
            " subtaskType            : BALLPARK - Demand\n" + 
            " subtaskStatus          : To Do\n" + 
            " subtaskId              : 0\n" + 
            " subtaskNum             : PROJ-0\n" + 
            " subtaskSummary         : Smry 1\n" + 
            " subtaskDescription     : M | 00000 - Smry 1\n" + 
            " subtaskFullDescription : BALLPARK - Demand | M | 00000 - Smry 1\n" + 
            " tshirtSize             : M\n" + 
            " worklog                : 0.0\n" + 
            " wrongWorklog           : 10.0\n" + 
            " demandBallpark         : 1.0\n" + 
            " taskBallpark           : 0.0\n" + 
            " queryType              : DEMAND BALLPARK"+
            
            "\n\n"+
                
            " planningType           : Plan\n" + 
            " project                : A Project\n" + 
            " demandType             : Demand\n" + 
            " demandStatus           : To Do\n" + 
            " demandId               : 2\n" + 
            " demandNum              : PROJ-2\n" + 
            " demandSummary          : Smry 2\n" + 
            " demandDescription      : 00002 - Smry 2\n" + 
            " taskType               : Task\n" + 
            " taskStatus             : To Do\n" + 
            " taskId                 : 3\n" + 
            " taskNum                : PROJ-3\n" + 
            " taskSummary            : Smry 3\n" + 
            " taskDescription        : 00003 - Smry 3\n" + 
            " taskFullDescription    : Task | 00003 - Smry 3\n" + 
            " taskRelease            : No release set\n" + 
            " subtaskType            : Dev\n" + 
            " subtaskStatus          : To Do\n" + 
            " subtaskId              : 4\n" + 
            " subtaskNum             : PROJ-4\n" + 
            " subtaskSummary         : Smry 4\n" + 
            " subtaskDescription     : XL | 00004 - Smry 4\n" + 
            " subtaskFullDescription : Dev | XL | 00004 - Smry 4\n" + 
            " tshirtSize             : XL\n" + 
            " worklog                : 5.0\n" + 
            " wrongWorklog           : 0.0\n" + 
            " demandBallpark         : 1.0\n" + 
            " taskBallpark           : 2.0\n" + 
            " queryType              : SUBTASK PLAN");
    }

    @Test
    public void featureWithoutAnySubtasks_ShouldCreateDummyFeaturesForAllTShirts() {
        configureBallparkMappings(
            taskIssueType + " : \n" +
            "  - issueType : BALLPARK - Development\n" + 
            "    tshirtCustomFieldId: Dev_Tshirt\n" + 
            "    jiraIssueTypes:\n" + 
            "      - "+ devIssueType + "\n" + 
    
            "  - issueType : BALLPARK - Alpha\n" + 
            "    tshirtCustomFieldId: Alpha_TestTshirt\n" + 
            "    jiraIssueTypes:\n" + 
            "      - " + alphaIssueType + " # UX\n");
        
        tshirtSizeInfo.setIds(Arrays.asList("Dev_Tshirt","Alpha_TestTshirt"));
        
        issues( 
            demand().id(2).key("PROJ-2").summary("Smry 2").originalEstimateInHours(1),
            
            task()  .id(3).key("PROJ-3").parent("PROJ-2").summary("Smry 3").originalEstimateInHours(2).timeSpentInHours(1)
                .tshirt("Dev_Tshirt","L")
                .tshirt("Alpha_TestTshirt","S")
        );
        
        assertFollowupsForIssuesEquals(
            " planningType           : Ballpark\n" + 
            " project                : A Project\n" + 
            " demandType             : Demand\n" + 
            " demandStatus           : To Do\n" + 
            " demandId               : 2\n" + 
            " demandNum              : PROJ-2\n" + 
            " demandSummary          : Smry 2\n" + 
            " demandDescription      : 00002 - Smry 2\n" + 
            " taskType               : Task\n" + 
            " taskStatus             : To Do\n" + 
            " taskId                 : 3\n" + 
            " taskNum                : PROJ-3\n" + 
            " taskSummary            : Smry 3\n" + 
            " taskDescription        : 00003 - Smry 3\n" + 
            " taskFullDescription    : Task | 00003 - Smry 3\n" + 
            " taskRelease            : No release set\n" + 
            " subtaskType            : BALLPARK - Development\n" + 
            " subtaskStatus          : To Do\n" + 
            " subtaskId              : 0\n" + 
            " subtaskNum             : PROJ-0\n" + 
            " subtaskSummary         : BALLPARK - Development\n" + 
            " subtaskDescription     : 00000 - Smry 3\n" + 
            " subtaskFullDescription : BALLPARK - Development | 00000 - Smry 3\n" + 
            " tshirtSize             : L\n" + 
            " worklog                : 0.0\n" + 
            " wrongWorklog           : 1.0\n" + 
            " demandBallpark         : 1.0\n" + 
            " taskBallpark           : 2.0\n" + 
            " queryType              : FEATURE BALLPARK"+
            
            "\n\n"+
            
            " planningType           : Ballpark\n" + 
            " project                : A Project\n" + 
            " demandType             : Demand\n" + 
            " demandStatus           : To Do\n" + 
            " demandId               : 2\n" + 
            " demandNum              : PROJ-2\n" + 
            " demandSummary          : Smry 2\n" + 
            " demandDescription      : 00002 - Smry 2\n" + 
            " taskType               : Task\n" + 
            " taskStatus             : To Do\n" + 
            " taskId                 : 3\n" + 
            " taskNum                : PROJ-3\n" + 
            " taskSummary            : Smry 3\n" + 
            " taskDescription        : 00003 - Smry 3\n" + 
            " taskFullDescription    : Task | 00003 - Smry 3\n" + 
            " taskRelease            : No release set\n" + 
            " subtaskType            : BALLPARK - Alpha\n" + 
            " subtaskStatus          : To Do\n" + 
            " subtaskId              : 0\n" + 
            " subtaskNum             : PROJ-0\n" + 
            " subtaskSummary         : BALLPARK - Alpha\n" + 
            " subtaskDescription     : 00000 - Smry 3\n" + 
            " subtaskFullDescription : BALLPARK - Alpha | 00000 - Smry 3\n" + 
            " tshirtSize             : S\n" + 
            
            " worklog                : 0.0\n" + 
            " wrongWorklog           : 1.0\n" + 
            " demandBallpark         : 1.0\n" + 
            " taskBallpark           : 2.0\n" + 
            " queryType              : FEATURE BALLPARK");
    }

    @Test
    public void featureWithOneSubtask_ShouldCreateOnlyDummyFeatureForMissingSubtask() {
        configureBallparkMappings(
                taskIssueType + " : \n" +
                "  - issueType : BALLPARK - Development\n" + 
                "    tshirtCustomFieldId: Dev_Tshirt\n" + 
                "    jiraIssueTypes:\n" + 
                "      - "+ frontEndIssueType + "\n" +
                "      - "+ devIssueType + "\n" +
        
                "  - issueType : BALLPARK - Alpha\n" + 
                "    tshirtCustomFieldId: Alpha_TestTshirt\n" + 
                "    jiraIssueTypes:\n" + 
                "      - " + alphaIssueType + "\n" +
                
                "  - issueType : BALLPARK - Deploy\n" + 
                "    tshirtCustomFieldId: Deploy_TestTshirt\n" + 
                "    jiraIssueTypes:\n" + 
                "      - " + deployIssueType + "\n" +
                
                reviewIssueType + " : \n" +
                "  - issueType : BALLPARK - Review\n" + 
                "    tshirtCustomFieldId: Review_Tshirt\n" + 
                "    jiraIssueTypes:\n" + 
                "      - "+ devIssueType + "\n"
                );
            
        tshirtSizeInfo.setIds(Arrays.asList("Dev_Tshirt","Alpha_TestTshirt","Review_Tshirt"));
        
        issues( 
            demand().id(2).key("PROJ-2").summary("Smry 2").originalEstimateInHours(1),
            
            task()  .id(3).key("PROJ-3").parent("PROJ-2").summary("Smry 3").originalEstimateInHours(2).timeSpentInHours(1)
                    .tshirt("Dev_Tshirt", "L")
                    .tshirt("Alpha_TestTshirt", "S"),
                    
            subtask().id(4).key("PROJ-4").summary("Smry 4").timeSpentInHours(5).parent("PROJ-3").issueType(devIssueType).tshirtSize("XL")
        );
        
        assertFollowupsForIssuesEquals(
            " planningType           : Ballpark\n" + 
            " project                : A Project\n" + 
            " demandType             : Demand\n" + 
            " demandStatus           : To Do\n" + 
            " demandId               : 2\n" + 
            " demandNum              : PROJ-2\n" + 
            " demandSummary          : Smry 2\n" + 
            " demandDescription      : 00002 - Smry 2\n" + 
            " taskType               : Task\n" + 
            " taskStatus             : To Do\n" + 
            " taskId                 : 3\n" + 
            " taskNum                : PROJ-3\n" + 
            " taskSummary            : Smry 3\n" + 
            " taskDescription        : 00003 - Smry 3\n" + 
            " taskFullDescription    : Task | 00003 - Smry 3\n" + 
            " taskRelease            : No release set\n" + 
            " subtaskType            : BALLPARK - Alpha\n" + 
            " subtaskStatus          : To Do\n" + 
            " subtaskId              : 0\n" + 
            " subtaskNum             : PROJ-0\n" + 
            " subtaskSummary         : BALLPARK - Alpha\n" + 
            " subtaskDescription     : 00000 - Smry 3\n" + 
            " subtaskFullDescription : BALLPARK - Alpha | 00000 - Smry 3\n" + 
            " tshirtSize             : S\n" + 
            " worklog                : 0.0\n" + 
            " wrongWorklog           : 1.0\n" + 
            " demandBallpark         : 1.0\n" + 
            " taskBallpark           : 2.0\n" + 
            " queryType              : FEATURE BALLPARK" +
            
            "\n\n"+
            
            " planningType           : Plan\n" + 
            " project                : A Project\n" + 
            " demandType             : Demand\n" + 
            " demandStatus           : To Do\n" + 
            " demandId               : 2\n" + 
            " demandNum              : PROJ-2\n" + 
            " demandSummary          : Smry 2\n" + 
            " demandDescription      : 00002 - Smry 2\n" + 
            " taskType               : Task\n" + 
            " taskStatus             : To Do\n" + 
            " taskId                 : 3\n" + 
            " taskNum                : PROJ-3\n" + 
            " taskSummary            : Smry 3\n" + 
            " taskDescription        : 00003 - Smry 3\n" + 
            " taskFullDescription    : Task | 00003 - Smry 3\n" + 
            " taskRelease            : No release set\n" + 
            " subtaskType            : Dev\n" + 
            " subtaskStatus          : To Do\n" + 
            " subtaskId              : 4\n" + 
            " subtaskNum             : PROJ-4\n" + 
            " subtaskSummary         : Smry 4\n" + 
            " subtaskDescription     : XL | 00004 - Smry 4\n" + 
            " subtaskFullDescription : Dev | XL | 00004 - Smry 4\n" + 
            " tshirtSize             : XL\n" + 
            " worklog                : 5.0\n" + 
            " wrongWorklog           : 0.0\n" + 
            " demandBallpark         : 1.0\n" + 
            " taskBallpark           : 2.0\n" + 
            " queryType              : SUBTASK PLAN");
    }
    
    @Test
    public void featureWithAllSubtasks_ShouldNotCreateDummies() {
        configureBallparkMappings(
                taskIssueType + " : \n" +
                "  - issueType : BALLPARK - Development\n" + 
                "    tshirtCustomFieldId: Dev_Tshirt\n" + 
                "    jiraIssueTypes:\n" + 
                "      - " + devIssueType + "\n" +
        
                "  - issueType : BALLPARK - Alpha\n" + 
                "    tshirtCustomFieldId: Alpha_TestTshirt\n" + 
                "    jiraIssueTypes:\n" + 
                "      - " + alphaIssueType + "\n"
                );
            
        tshirtSizeInfo.setIds(Arrays.asList("Dev_Tshirt","Alpha_TestTshirt","Review_Tshirt"));
        
        issues( 
            demand().id(2).key("PROJ-2").summary("Smry 2").originalEstimateInHours(1),
            
            task()  .id(3).key("PROJ-3").parent("PROJ-2").summary("Smry 3").originalEstimateInHours(2).timeSpentInHours(1)
                    .tshirt("Dev_Tshirt", "L")
                    .tshirt("Alpha_TestTshirt", "S"),
                    
            subtask().id(4).key("PROJ-4").summary("Smry 4").timeSpentInHours(5).parent("PROJ-3").issueType(devIssueType).tshirtSize("XL"),
            
            subtask().id(5).key("PROJ-5").summary("Smry 5").timeSpentInHours(15).parent("PROJ-3").issueType(alphaIssueType).tshirtSize("L")
        );
        
        assertFollowupsForIssuesEquals(
            " planningType           : Plan\n" + 
            " project                : A Project\n" + 
            " demandType             : Demand\n" + 
            " demandStatus           : To Do\n" + 
            " demandId               : 2\n" + 
            " demandNum              : PROJ-2\n" + 
            " demandSummary          : Smry 2\n" + 
            " demandDescription      : 00002 - Smry 2\n" + 
            " taskType               : Task\n" + 
            " taskStatus             : To Do\n" + 
            " taskId                 : 3\n" + 
            " taskNum                : PROJ-3\n" + 
            " taskSummary            : Smry 3\n" + 
            " taskDescription        : 00003 - Smry 3\n" + 
            " taskFullDescription    : Task | 00003 - Smry 3\n" + 
            " taskRelease            : No release set\n" + 
            " subtaskType            : Dev\n" + 
            " subtaskStatus          : To Do\n" + 
            " subtaskId              : 4\n" + 
            " subtaskNum             : PROJ-4\n" + 
            " subtaskSummary         : Smry 4\n" + 
            " subtaskDescription     : XL | 00004 - Smry 4\n" + 
            " subtaskFullDescription : Dev | XL | 00004 - Smry 4\n" + 
            " tshirtSize             : XL\n" + 
            " worklog                : 5.0\n" + 
            " wrongWorklog           : 0.0\n" + 
            " demandBallpark         : 1.0\n" + 
            " taskBallpark           : 2.0\n" + 
            " queryType              : SUBTASK PLAN" +
            
            "\n\n"+
            
            " planningType           : Plan\n" + 
            " project                : A Project\n" + 
            " demandType             : Demand\n" + 
            " demandStatus           : To Do\n" + 
            " demandId               : 2\n" + 
            " demandNum              : PROJ-2\n" + 
            " demandSummary          : Smry 2\n" + 
            " demandDescription      : 00002 - Smry 2\n" + 
            " taskType               : Task\n" + 
            " taskStatus             : To Do\n" + 
            " taskId                 : 3\n" + 
            " taskNum                : PROJ-3\n" + 
            " taskSummary            : Smry 3\n" + 
            " taskDescription        : 00003 - Smry 3\n" + 
            " taskFullDescription    : Task | 00003 - Smry 3\n" + 
            " taskRelease            : No release set\n" + 
            " subtaskType            : Alpha\n" + 
            " subtaskStatus          : To Do\n" + 
            " subtaskId              : 5\n" + 
            " subtaskNum             : PROJ-5\n" + 
            " subtaskSummary         : Smry 5\n" + 
            " subtaskDescription     : L | 00005 - Smry 5\n" + 
            " subtaskFullDescription : Alpha | L | 00005 - Smry 5\n" + 
            " tshirtSize             : L\n" + 
            " worklog                : 15.0\n" + 
            " wrongWorklog           : 0.0\n" + 
            " demandBallpark         : 1.0\n" + 
            " taskBallpark           : 2.0\n" + 
            " queryType              : SUBTASK PLAN");
    }
    
    @Test
    public void featureWithRelease_ShouldCreateBallparksWithRelease() {
        configureBallparkMappings(
            taskIssueType + " : \n" +
            "  - issueType : BALLPARK - Development\n" + 
            "    tshirtCustomFieldId: Dev_Tshirt\n" + 
            "    jiraIssueTypes:\n" + 
            "      - "+ devIssueType + "\n");
        
        tshirtSizeInfo.setIds(Arrays.asList("Dev_Tshirt","Alpha_TestTshirt"));
        
        issues( 
            demand().id(2).key("PROJ-2").summary("Smry 2").originalEstimateInHours(1),
            
            task()  .id(3).key("PROJ-3").parent("PROJ-2").summary("Smry 3").originalEstimateInHours(2).timeSpentInHours(1)
                .tshirt("Dev_Tshirt","L").release("release 66")
        );
        
        assertFollowupsForIssuesEquals(
            " planningType           : Ballpark\n" + 
            " project                : A Project\n" + 
            " demandType             : Demand\n" + 
            " demandStatus           : To Do\n" + 
            " demandId               : 2\n" + 
            " demandNum              : PROJ-2\n" + 
            " demandSummary          : Smry 2\n" + 
            " demandDescription      : 00002 - Smry 2\n" + 
            " taskType               : Task\n" + 
            " taskStatus             : To Do\n" + 
            " taskId                 : 3\n" + 
            " taskNum                : PROJ-3\n" + 
            " taskSummary            : Smry 3\n" + 
            " taskDescription        : 00003 - Smry 3\n" + 
            " taskFullDescription    : Task | 00003 - Smry 3\n" + 
            " taskRelease            : release 66\n" + 
            " subtaskType            : BALLPARK - Development\n" + 
            " subtaskStatus          : To Do\n" + 
            " subtaskId              : 0\n" + 
            " subtaskNum             : PROJ-0\n" + 
            " subtaskSummary         : BALLPARK - Development\n" + 
            " subtaskDescription     : 00000 - Smry 3\n" + 
            " subtaskFullDescription : BALLPARK - Development | 00000 - Smry 3\n" + 
            " tshirtSize             : L\n" + 
            " worklog                : 0.0\n" + 
            " wrongWorklog           : 1.0\n" + 
            " demandBallpark         : 1.0\n" + 
            " taskBallpark           : 2.0\n" + 
            " queryType              : FEATURE BALLPARK"
            );
    }
    
    @Test
    public void featureWithRelease_ShouldCreateSubtaskWithRelease() {
        configureBallparkMappings(
                taskIssueType + " : \n" +
                "  - issueType : BALLPARK - Development\n" + 
                "    tshirtCustomFieldId: Dev_Tshirt\n" + 
                "    jiraIssueTypes:\n" + 
                "      - " + devIssueType + "\n"
                );
            
        tshirtSizeInfo.setIds(Arrays.asList("Dev_Tshirt","Alpha_TestTshirt","Review_Tshirt"));
        
        issues( 
            demand().id(2).key("PROJ-2").summary("Smry 2").originalEstimateInHours(1),
            
            task()  .id(3).key("PROJ-3").parent("PROJ-2").summary("Smry 3").originalEstimateInHours(2).timeSpentInHours(1)
                    .tshirt("Dev_Tshirt", "L").release("release 66"),
                    
            subtask().id(4).key("PROJ-4").summary("Smry 4").timeSpentInHours(5).parent("PROJ-3").issueType(devIssueType).tshirtSize("XL")
        );
        
        assertFollowupsForIssuesEquals(
            " planningType           : Plan\n" + 
            " project                : A Project\n" + 
            " demandType             : Demand\n" + 
            " demandStatus           : To Do\n" + 
            " demandId               : 2\n" + 
            " demandNum              : PROJ-2\n" + 
            " demandSummary          : Smry 2\n" + 
            " demandDescription      : 00002 - Smry 2\n" + 
            " taskType               : Task\n" + 
            " taskStatus             : To Do\n" + 
            " taskId                 : 3\n" + 
            " taskNum                : PROJ-3\n" + 
            " taskSummary            : Smry 3\n" + 
            " taskDescription        : 00003 - Smry 3\n" + 
            " taskFullDescription    : Task | 00003 - Smry 3\n" + 
            " taskRelease            : release 66\n" + 
            " subtaskType            : Dev\n" + 
            " subtaskStatus          : To Do\n" + 
            " subtaskId              : 4\n" + 
            " subtaskNum             : PROJ-4\n" + 
            " subtaskSummary         : Smry 4\n" + 
            " subtaskDescription     : XL | 00004 - Smry 4\n" + 
            " subtaskFullDescription : Dev | XL | 00004 - Smry 4\n" + 
            " tshirtSize             : XL\n" + 
            " worklog                : 5.0\n" + 
            " wrongWorklog           : 0.0\n" + 
            " demandBallpark         : 1.0\n" + 
            " taskBallpark           : 2.0\n" + 
            " queryType              : SUBTASK PLAN"
            );
    }
    
    @Test
    public void subtaskWithoutParents_shouldSubTaskWithNullFields() {
        configureBallparkMappings(
                taskIssueType + " : \n" +
                "  - issueType : BALLPARK - Development\n" + 
                "    tshirtCustomFieldId: Dev_Tshirt\n" + 
                "    jiraIssueTypes:\n" + 
                "      - " + devIssueType + "\n"
                );
        issues( 
                task().id(3).key("PROJ-3").summary("Smry 3").originalEstimateInHours(2),
                subtask().id(4).key("PROJ-4").summary("Smry 4").timeSpentInHours(5).parent("PROJ-3").issueType(devIssueType).tshirtSize("XL")
        );
        
        assertFollowupsForIssuesEquals(
            " planningType           : Plan\n" + 
            " project                : A Project\n" + 
            " demandType             : \n" + 
            " demandStatus           : \n" + 
            " demandId               : \n" + 
            " demandNum              : \n" + 
            " demandSummary          : \n" + 
            " demandDescription      : \n" + 
            " taskType               : Task\n" + 
            " taskStatus             : To Do\n" + 
            " taskId                 : 3\n" + 
            " taskNum                : PROJ-3\n" + 
            " taskSummary            : Smry 3\n" + 
            " taskDescription        : 00003 - Smry 3\n" + 
            " taskFullDescription    : Task | 00003 - Smry 3\n" + 
            " taskRelease            : No release set\n" + 
            " subtaskType            : Dev\n" + 
            " subtaskStatus          : To Do\n" + 
            " subtaskId              : 4\n" + 
            " subtaskNum             : PROJ-4\n" + 
            " subtaskSummary         : Smry 4\n" + 
            " subtaskDescription     : XL | 00004 - Smry 4\n" + 
            " subtaskFullDescription : Dev | XL | 00004 - Smry 4\n" + 
            " tshirtSize             : XL\n" + 
            " worklog                : 5.0\n" + 
            " wrongWorklog           : 0.0\n" + 
            " demandBallpark         : 0.0\n" + 
            " taskBallpark           : 2.0\n" + 
            " queryType              : SUBTASK PLAN");
    }

    @Test
    public void featureWithCertainStatus_ShouldNotGenerateBallparks() {
        configureBallparkMappings(
                taskIssueType + " : \n" +
                "  - issueType : BALLPARK - Development\n" + 
                "    tshirtCustomFieldId: Dev_Tshirt\n" + 
                "    jiraIssueTypes:\n" + 
                "      - "+ frontEndIssueType + "\n" +
                "      - "+ devIssueType + "\n" 
                );
            
        tshirtSizeInfo.setIds(Arrays.asList("Dev_Tshirt","Alpha_TestTshirt"));
        followup.setFeatureStatusThatDontGenerateBallpark(Arrays.asList(statusDone));
        
        issues( 
            demand().id(2).key("PROJ-2").summary("Smry 2").originalEstimateInHours(1),
            
            task()  .id(3).key("PROJ-3").parent("PROJ-2").summary("Smry 3").originalEstimateInHours(2).timeSpentInHours(1)
                    .tshirt("Dev_Tshirt", "L").issueStatus(statusDone)
        );
        
        assertFollowupsForIssuesEquals("");
    }

    @Test
    public void subtaskWithCertainStatus_ShouldNotPreventBallparkGeneration() {
        configureBallparkMappings(
                taskIssueType + " : \n" +
                "  - issueType : BALLPARK - Development\n" + 
                "    tshirtCustomFieldId: Dev_Tshirt\n" + 
                "    jiraIssueTypes:\n" + 
                "      - "+ devIssueType + "\n"
                );
            
        tshirtSizeInfo.setIds(Arrays.asList("Dev_Tshirt","Alpha_TestTshirt","Review_Tshirt"));
        followup.setSubtaskStatusThatDontPreventBallparkGeneration(Arrays.asList(statusCancelled));
        
        issues( 
            demand().id(2).key("PROJ-2").summary("Smry 2").originalEstimateInHours(1),
            
            task()  .id(3).key("PROJ-3").parent("PROJ-2").summary("Smry 3").originalEstimateInHours(2).timeSpentInHours(1)
                    .tshirt("Dev_Tshirt", "L"),
                    
            subtask().id(4).key("PROJ-4").summary("Smry 4").timeSpentInHours(5).parent("PROJ-3").issueType(devIssueType).tshirtSize("XL")
                .issueStatus(statusCancelled)
        );
        
        assertFollowupsForIssuesEquals(
            " planningType           : Ballpark\n" + 
            " project                : A Project\n" + 
            " demandType             : Demand\n" + 
            " demandStatus           : To Do\n" + 
            " demandId               : 2\n" + 
            " demandNum              : PROJ-2\n" + 
            " demandSummary          : Smry 2\n" + 
            " demandDescription      : 00002 - Smry 2\n" + 
            " taskType               : Task\n" + 
            " taskStatus             : To Do\n" + 
            " taskId                 : 3\n" + 
            " taskNum                : PROJ-3\n" + 
            " taskSummary            : Smry 3\n" + 
            " taskDescription        : 00003 - Smry 3\n" + 
            " taskFullDescription    : Task | 00003 - Smry 3\n" + 
            " taskRelease            : No release set\n" + 
            " subtaskType            : BALLPARK - Development\n" + 
            " subtaskStatus          : To Do\n" + 
            " subtaskId              : 0\n" + 
            " subtaskNum             : PROJ-0\n" + 
            " subtaskSummary         : BALLPARK - Development\n" + 
            " subtaskDescription     : 00000 - Smry 3\n" + 
            " subtaskFullDescription : BALLPARK - Development | 00000 - Smry 3\n" + 
            " tshirtSize             : L\n" + 
            " worklog                : 0.0\n" + 
            " wrongWorklog           : 1.0\n" + 
            " demandBallpark         : 1.0\n" + 
            " taskBallpark           : 2.0\n" + 
            " queryType              : FEATURE BALLPARK"+
            
            "\n\n"+
            
            " planningType           : Plan\n" + 
            " project                : A Project\n" + 
            " demandType             : Demand\n" + 
            " demandStatus           : To Do\n" + 
            " demandId               : 2\n" + 
            " demandNum              : PROJ-2\n" + 
            " demandSummary          : Smry 2\n" + 
            " demandDescription      : 00002 - Smry 2\n" + 
            " taskType               : Task\n" + 
            " taskStatus             : To Do\n" + 
            " taskId                 : 3\n" + 
            " taskNum                : PROJ-3\n" + 
            " taskSummary            : Smry 3\n" + 
            " taskDescription        : 00003 - Smry 3\n" + 
            " taskFullDescription    : Task | 00003 - Smry 3\n" + 
            " taskRelease            : No release set\n" + 
            " subtaskType            : Dev\n" + 
            " subtaskStatus          : Cancelled\n" + 
            " subtaskId              : 4\n" + 
            " subtaskNum             : PROJ-4\n" + 
            " subtaskSummary         : Smry 4\n" + 
            " subtaskDescription     : XL | 00004 - Smry 4\n" + 
            " subtaskFullDescription : Dev | XL | 00004 - Smry 4\n" + 
            " tshirtSize             : XL\n" + 
            " worklog                : 5.0\n" + 
            " wrongWorklog           : 0.0\n" + 
            " demandBallpark         : 1.0\n" + 
            " taskBallpark           : 2.0\n" + 
            " queryType              : SUBTASK PLAN");
    }
    
    @Test
    public void withoutBallparkConfiguration_ShouldFail() {
        issues( 
                task().id(3).key("PROJ-3").summary("Smry 3").originalEstimateInHours(2),
                subtask().id(4).key("PROJ-4").summary("Smry 4").timeSpentInHours(5).parent("PROJ-3").issueType(devIssueType).tshirtSize("XL")
        );
        
        try {
            subject.getJiraData();
            fail("Should fail when trying to generate ballbark without mapping for given issue type");
        } catch(IllegalStateException e) {
            assertEquals("Ballpark mapping for issue type 'Task' (id 12) missing in configuration", e.getMessage());
        }
    }
    
    @Test
    public void demandAndfeatureWithRelease_ShouldCreateBallparksWithFeatureRelease() {
        configureBallparkMappings(
            taskIssueType + " : \n" +
            "  - issueType : BALLPARK - Development\n" + 
            "    tshirtCustomFieldId: Dev_Tshirt\n" + 
            "    jiraIssueTypes:\n" + 
            "      - "+ devIssueType + "\n");
        
        tshirtSizeInfo.setIds(Arrays.asList("Dev_Tshirt","Alpha_TestTshirt"));
        
        issues( 
            demand().id(2).key("PROJ-2").summary("Smry 2").originalEstimateInHours(1).release("release 66"),
            
            task()  .id(3).key("PROJ-3").parent("PROJ-2").summary("Smry 3").originalEstimateInHours(2).timeSpentInHours(1)
                .tshirt("Dev_Tshirt","L").release("release 88")
        );
        
        assertFollowupsForIssuesEquals(
            " planningType           : Ballpark\n" + 
            " project                : A Project\n" + 
            " demandType             : Demand\n" + 
            " demandStatus           : To Do\n" + 
            " demandId               : 2\n" + 
            " demandNum              : PROJ-2\n" + 
            " demandSummary          : Smry 2\n" + 
            " demandDescription      : 00002 - Smry 2\n" + 
            " taskType               : Task\n" + 
            " taskStatus             : To Do\n" + 
            " taskId                 : 3\n" + 
            " taskNum                : PROJ-3\n" + 
            " taskSummary            : Smry 3\n" + 
            " taskDescription        : 00003 - Smry 3\n" + 
            " taskFullDescription    : Task | 00003 - Smry 3\n" + 
            " taskRelease            : release 88\n" + 
            " subtaskType            : BALLPARK - Development\n" + 
            " subtaskStatus          : To Do\n" + 
            " subtaskId              : 0\n" + 
            " subtaskNum             : PROJ-0\n" + 
            " subtaskSummary         : BALLPARK - Development\n" + 
            " subtaskDescription     : 00000 - Smry 3\n" + 
            " subtaskFullDescription : BALLPARK - Development | 00000 - Smry 3\n" + 
            " tshirtSize             : L\n" + 
            " worklog                : 0.0\n" + 
            " wrongWorklog           : 1.0\n" + 
            " demandBallpark         : 1.0\n" + 
            " taskBallpark           : 2.0\n" + 
            " queryType              : FEATURE BALLPARK"
            );
    }
    
    @Test
    public void demandWithReleaseAndfeatureWithout_ShouldCreateBallparksWithDemandRelease() {
        configureBallparkMappings(
            taskIssueType + " : \n" +
            "  - issueType : BALLPARK - Development\n" + 
            "    tshirtCustomFieldId: Dev_Tshirt\n" + 
            "    jiraIssueTypes:\n" + 
            "      - "+ devIssueType + "\n");
        
        tshirtSizeInfo.setIds(Arrays.asList("Dev_Tshirt","Alpha_TestTshirt"));
        
        issues( 
            demand().id(2).key("PROJ-2").summary("Smry 2").originalEstimateInHours(1).release("release 66"),
            
            task()  .id(3).key("PROJ-3").parent("PROJ-2").summary("Smry 3").originalEstimateInHours(2).timeSpentInHours(1)
                .tshirt("Dev_Tshirt","L")
        );
        
        assertFollowupsForIssuesEquals(
            " planningType           : Ballpark\n" + 
            " project                : A Project\n" + 
            " demandType             : Demand\n" + 
            " demandStatus           : To Do\n" + 
            " demandId               : 2\n" + 
            " demandNum              : PROJ-2\n" + 
            " demandSummary          : Smry 2\n" + 
            " demandDescription      : 00002 - Smry 2\n" + 
            " taskType               : Task\n" + 
            " taskStatus             : To Do\n" + 
            " taskId                 : 3\n" + 
            " taskNum                : PROJ-3\n" + 
            " taskSummary            : Smry 3\n" + 
            " taskDescription        : 00003 - Smry 3\n" + 
            " taskFullDescription    : Task | 00003 - Smry 3\n" + 
            " taskRelease            : release 66\n" + 
            " subtaskType            : BALLPARK - Development\n" + 
            " subtaskStatus          : To Do\n" + 
            " subtaskId              : 0\n" + 
            " subtaskNum             : PROJ-0\n" + 
            " subtaskSummary         : BALLPARK - Development\n" + 
            " subtaskDescription     : 00000 - Smry 3\n" + 
            " subtaskFullDescription : BALLPARK - Development | 00000 - Smry 3\n" + 
            " tshirtSize             : L\n" + 
            " worklog                : 0.0\n" + 
            " wrongWorklog           : 1.0\n" + 
            " demandBallpark         : 1.0\n" + 
            " taskBallpark           : 2.0\n" + 
            " queryType              : FEATURE BALLPARK"
            );
    }    
    
    @Test
    public void ifSubtaskHasRelease_ShouldPreferSubtaskRelease() {
        configureBallparkMappings(
                taskIssueType + " : \n" +
                "  - issueType : BALLPARK - Development\n" + 
                "    tshirtCustomFieldId: Dev_Tshirt\n" + 
                "    jiraIssueTypes:\n" + 
                "      - " + devIssueType + "\n"
                );
            
        tshirtSizeInfo.setIds(Arrays.asList("Dev_Tshirt","Alpha_TestTshirt","Review_Tshirt"));
        
        issues( 
            demand().id(2).key("PROJ-2").summary("Smry 2").originalEstimateInHours(1).release("Demand Release #1"),
            
            task()  .id(3).key("PROJ-3").parent("PROJ-2").summary("Smry 3").originalEstimateInHours(2).timeSpentInHours(1)
                    .tshirt("Dev_Tshirt", "L")
                    .tshirt("Alpha_TestTshirt", "S")
                    .release("Feature Release #2"),
                    
            subtask().id(4).key("PROJ-4").summary("Smry 4").timeSpentInHours(5).parent("PROJ-3").issueType(devIssueType).tshirtSize("XL")
                    .release("Sub Task Release #3"),
                    
            subtask().id(5).key("PROJ-5").summary("Smry 5").timeSpentInHours(5).parent("PROJ-3").issueType(devIssueType).tshirtSize("XL")
                    .release("Sub Task Release #4")                    
        );
        
        assertFollowupsForIssuesEquals(
            " planningType           : Plan\n" + 
            " project                : A Project\n" + 
            " demandType             : Demand\n" + 
            " demandStatus           : To Do\n" + 
            " demandId               : 2\n" + 
            " demandNum              : PROJ-2\n" + 
            " demandSummary          : Smry 2\n" + 
            " demandDescription      : 00002 - Smry 2\n" + 
            " taskType               : Task\n" + 
            " taskStatus             : To Do\n" + 
            " taskId                 : 3\n" + 
            " taskNum                : PROJ-3\n" + 
            " taskSummary            : Smry 3\n" + 
            " taskDescription        : 00003 - Smry 3\n" + 
            " taskFullDescription    : Task | 00003 - Smry 3\n" + 
            " taskRelease            : Sub Task Release #3\n" + 
            " subtaskType            : Dev\n" + 
            " subtaskStatus          : To Do\n" + 
            " subtaskId              : 4\n" + 
            " subtaskNum             : PROJ-4\n" + 
            " subtaskSummary         : Smry 4\n" + 
            " subtaskDescription     : XL | 00004 - Smry 4\n" + 
            " subtaskFullDescription : Dev | XL | 00004 - Smry 4\n" + 
            " tshirtSize             : XL\n" + 
            " worklog                : 5.0\n" + 
            " wrongWorklog           : 0.0\n" + 
            " demandBallpark         : 1.0\n" + 
            " taskBallpark           : 2.0\n" + 
            " queryType              : SUBTASK PLAN" +
            
            "\n\n"+
            
            " planningType           : Plan\n" + 
            " project                : A Project\n" + 
            " demandType             : Demand\n" + 
            " demandStatus           : To Do\n" + 
            " demandId               : 2\n" + 
            " demandNum              : PROJ-2\n" + 
            " demandSummary          : Smry 2\n" + 
            " demandDescription      : 00002 - Smry 2\n" + 
            " taskType               : Task\n" + 
            " taskStatus             : To Do\n" + 
            " taskId                 : 3\n" + 
            " taskNum                : PROJ-3\n" + 
            " taskSummary            : Smry 3\n" + 
            " taskDescription        : 00003 - Smry 3\n" + 
            " taskFullDescription    : Task | 00003 - Smry 3\n" + 
            " taskRelease            : Sub Task Release #4\n" + 
            " subtaskType            : Dev\n" + 
            " subtaskStatus          : To Do\n" + 
            " subtaskId              : 5\n" + 
            " subtaskNum             : PROJ-5\n" + 
            " subtaskSummary         : Smry 5\n" + 
            " subtaskDescription     : XL | 00005 - Smry 5\n" + 
            " subtaskFullDescription : Dev | XL | 00005 - Smry 5\n" + 
            " tshirtSize             : XL\n" + 
            " worklog                : 5.0\n" + 
            " wrongWorklog           : 0.0\n" + 
            " demandBallpark         : 1.0\n" + 
            " taskBallpark           : 2.0\n" + 
            " queryType              : SUBTASK PLAN"             
            );
    }
    
    @Test
    public void ifDemandHasReleaseAndFeatureAndSubTaskDoesnt_UseDemandRelease() {
        configureBallparkMappings(
                taskIssueType + " : \n" +
                "  - issueType : BALLPARK - Development\n" + 
                "    tshirtCustomFieldId: Dev_Tshirt\n" + 
                "    jiraIssueTypes:\n" + 
                "      - " + devIssueType + "\n"
                );
            
        tshirtSizeInfo.setIds(Arrays.asList("Dev_Tshirt","Alpha_TestTshirt","Review_Tshirt"));
        
        issues( 
            demand().id(2).key("PROJ-2").summary("Smry 2").originalEstimateInHours(1).release("Demand Release #1"),
            
            task()  .id(3).key("PROJ-3").parent("PROJ-2").summary("Smry 3").originalEstimateInHours(2).timeSpentInHours(1)
                    .tshirt("Dev_Tshirt", "L")
                    .tshirt("Alpha_TestTshirt", "S"),
                    
            subtask().id(4).key("PROJ-4").summary("Smry 4").timeSpentInHours(5).parent("PROJ-3").issueType(devIssueType).tshirtSize("XL")
        );
        
        assertFollowupsForIssuesEquals(
            " planningType           : Plan\n" + 
            " project                : A Project\n" + 
            " demandType             : Demand\n" + 
            " demandStatus           : To Do\n" + 
            " demandId               : 2\n" + 
            " demandNum              : PROJ-2\n" + 
            " demandSummary          : Smry 2\n" + 
            " demandDescription      : 00002 - Smry 2\n" + 
            " taskType               : Task\n" + 
            " taskStatus             : To Do\n" + 
            " taskId                 : 3\n" + 
            " taskNum                : PROJ-3\n" + 
            " taskSummary            : Smry 3\n" + 
            " taskDescription        : 00003 - Smry 3\n" + 
            " taskFullDescription    : Task | 00003 - Smry 3\n" + 
            " taskRelease            : Demand Release #1\n" + 
            " subtaskType            : Dev\n" + 
            " subtaskStatus          : To Do\n" + 
            " subtaskId              : 4\n" + 
            " subtaskNum             : PROJ-4\n" + 
            " subtaskSummary         : Smry 4\n" + 
            " subtaskDescription     : XL | 00004 - Smry 4\n" + 
            " subtaskFullDescription : Dev | XL | 00004 - Smry 4\n" + 
            " tshirtSize             : XL\n" + 
            " worklog                : 5.0\n" + 
            " wrongWorklog           : 0.0\n" + 
            " demandBallpark         : 1.0\n" + 
            " taskBallpark           : 2.0\n" + 
            " queryType              : SUBTASK PLAN"
            );
    }
    
    @Test
    public void issuesInCertainStatus_shouldNotShowUpAtAll() {
        configureBallparkMappings(
                taskIssueType + " : \n" +
                "  - issueType : BALLPARK - Development\n" + 
                "    tshirtCustomFieldId: Dev_Tshirt\n" + 
                "    jiraIssueTypes:\n" + 
                "      - " + devIssueType + "\n"
                );
        
        followup.setStatusExcludedFromFollowup(Arrays.asList(statusOpen));
        
        issues( 
                demand().id(2).key("PROJ-2").summary("Smry 2").originalEstimateInHours(1).release("Demand Release #1").issueStatus(statusOpen),
                
                task()  .id(3).key("PROJ-3").parent("PROJ-2").summary("Smry 3").originalEstimateInHours(2).timeSpentInHours(1)
                        .issueStatus(statusOpen)
                        .tshirt("Dev_Tshirt", "L")
                        .tshirt("Alpha_TestTshirt", "S"),
                        
                subtask().id(4).key("PROJ-4").summary("Smry 4").timeSpentInHours(5).parent("PROJ-3").issueType(devIssueType).tshirtSize("XL")
                        .issueStatus(statusOpen)
            );
        
        assertFollowupsForIssuesEquals("");
    }
    
    @Test
    public void issuesWithoutParent_shouldNotBreakTheFollowupGeneration() {
        configureBallparkMappings(
                taskIssueType + " : \n" +
                "  - issueType : BALLPARK - Development\n" + 
                "    tshirtCustomFieldId: Dev_Tshirt\n" + 
                "    jiraIssueTypes:\n" + 
                "      - " + devIssueType + "\n"
                );
        
        issues( 
                subtask().id(4).key("PROJ-4").summary("Smry 4").timeSpentInHours(5).issueType(devIssueType).tshirtSize("XL")
            );
        assertFollowupsForIssuesEquals("");
    }
    
    private void configureBallparkMappings(String string) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Map<Long, List<BallparkMapping>> ballparkMappings;
        try {
            ballparkMappings = mapper.readValue(string,new TypeReference<Map<Long, List<BallparkMapping>>>(){});
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        followup.setBallparkMappings(ballparkMappings);
    }
    
    private List<FollowUpData> sortJiraDataByIssuesKeys(List<FollowUpData> actual) {
        Collections.sort(actual, new Comparator<FollowUpData>() {
            @Override
            public int compare(FollowUpData o1, FollowUpData o2) {
                return (o1.demandNum + o1.taskNum + o1.subtaskNum).compareTo(o2.demandNum + o2.taskNum + o2.subtaskNum);
            }
        });
        return actual;
    }
    
    private void assertFollowupsForIssuesEquals(String expectedFollowupList) {
        List<FollowUpData> actual = sortJiraDataByIssuesKeys(subject.getJiraData());

        assertEquals(
            expectedFollowupList,
            StringUtils.join(actual, "\n\n"));
    }
    
    private void issues(IssueBuilder ... builders) {
        List<Issue> issueList = new ArrayList<>();
        for(IssueBuilder b : builders)
            issueList.add(b.build());
        when(issueBufferService.getAllIssuesVisibleToUser()).thenReturn(issueList); 
    }
    
    private IssueBuilder demand() {
        return new IssueBuilder()
                .issueType(demandIssueType);
    }
	
	private IssueBuilder subtask() {
	    return new IssueBuilder();
	}
	
	private IssueBuilder task() {
	    return new IssueBuilder().issueType(taskIssueType);
	}
	
    private static String getProjectName() {
        return "A Project";
    }

    private static String getProjectKey() {
        return "PROJ";
    }
    
    private class IssueBuilder {
        private long issueType;
        private Long id; 
        private String key; 
        private String summary; 
        private Long status = statusToDo;
        private int originalEstimateMinutes;
        private int timeSpentMinutes;
        private String parent;
        private Map<String, Object> customFields = new LinkedHashMap<>();
        
        public IssueBuilder id(int id) {
            this.id = (long) id;
            return this;
        }

        public IssueBuilder release(String releaseName) {
            String releaseId = jiraProperties.getCustomfield().getRelease().getId();
            customFields.put(releaseId, new objective.taskboard.data.CustomField(releaseId, releaseName));
            return this;
        }

        public IssueBuilder issueStatus(long status) {
            this.status = status;
            return this;
        }

        public IssueBuilder tshirt(String tshirtId, String tshirtSize) {
            customFields.put(tshirtId, new objective.taskboard.data.CustomField(tshirtId, tshirtSize));
            return this;
        }

        public IssueBuilder parent(String parent) {
            this.parent = parent;
            return this;
        }

        public IssueBuilder issueType(long issueType) {
            this.issueType = issueType;
            return this;
        }

        public IssueBuilder timeSpentInHours(int hours) {
            this.timeSpentMinutes = hours * 60;
            return this;
        }

        public IssueBuilder originalEstimateInHours(int hours) {
            this.originalEstimateMinutes = hours * 60;
            return this;
        }
        public IssueBuilder key(String key) {
            this.key = key;
            return this;
        }

        public IssueBuilder summary(String summary) {
            this.summary = summary;
            return this;
        }
        
        private IssueBuilder tshirtSize(String tShirtSize) {
            return tshirt(tshirtSizeInfo.getMainTShirtSizeFieldId(), tShirtSize);
        }
        
        public Issue build() {
            return Issue.from(id, 
                key, 
                getProjectKey(), 
                getProjectName(), 
                issueType, 
                null, //typeIconUri
                summary, 
                status, 
                0L,   //startDateStepMillis
                null, //subresponsavel1
                null, //subresponsavel2
                parent, 
                0L,   //parentType
                null, //parentTypeIconUri
                new ArrayList<String>(),//dependencies 
                null, //color
                null, //subResponsaveis
                null, //assignee
                null, //usersTeam
                0L, //priority
                null, //dueDate
                0L, //created
                null, //description 
                null, //teams
                null, //comments
                null, //labels
                null, //components
                customFields, //customFields
                0L,   //priorityOrder
                new Issue.TaskboardTimeTracking(originalEstimateMinutes, timeSpentMinutes),
                jiraProperties,
                metadataService);
        }
    }
}
