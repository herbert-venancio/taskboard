package objective.taskboard.followup;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Status;

import objective.taskboard.data.Issue;
import objective.taskboard.issueBuffer.IssueBufferService;
import objective.taskboard.jira.JiraProperties;
import objective.taskboard.jira.JiraProperties.CustomField;
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
    
    @InjectMocks
    FollowupDataProviderImpl subject;
    
    private static final long demandIssueType = 13;
    private static final long taskIssueType = 12; 
    private static final long devIssueType  = 14;
    
    private static final long statusToDo = 13L;
    private static final long statusInProgress = 15L;
    private static final long statusDone = 17L;
    
    @Before
    public void before() throws InterruptedException, ExecutionException {
        Map<Long, Status> statusMap = new LinkedHashMap<>();
        statusMap.put(statusToDo,       new Status(null, statusToDo,       "To Do", null, null));
        statusMap.put(statusInProgress, new Status(null, statusInProgress, "In Progress", null, null));
        statusMap.put(statusDone,       new Status(null, statusDone,       "Done", null, null));
        when(metadataService.getStatusesMetadata()).thenReturn(statusMap );
        
        Map<Long, IssueType> issueTypeMap = new LinkedHashMap<>();
        issueTypeMap.put(demandIssueType, new IssueType(null, demandIssueType, "Demand", false, null,null));
        issueTypeMap.put(taskIssueType,   new IssueType(null, taskIssueType,   "Task", false, null,null));
        issueTypeMap.put(devIssueType,    new IssueType(null, devIssueType,    "Dev", false, null,null));
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
        
    }
    
    @Test
    public void oneDemand_shouldCreateASingleBallpark() {
        issues( 
                demand(1L, "PROJ-1", "Smry 1", "Desc 1", statusToDo, 60, 600) 
        );
        
        List<FollowUpData> actual = subject.getJiraData();

        assertFirstBallpark(actual);
    }
    /*
    @Test
    public void twoDemandsAndOneWithSubtask_shouldCreateOneDemandBallParksAndOneSubTask() {
        issues( 
                demand (2L, "PROJ-2", "Smry 2", "Desc 2", statusToDo, 60, 0),
                feature(3L, "PROJ-3", "Smry 3", "Desc 3", statusToDo, 120, 0, "PROJ-2", taskIssueType)
        );
        List<FollowUpData> actual = subject.getJiraData();

        FollowUpData actualRow1 = actual.get(0);
        String expected=
        " planningType           : Ballpark\n" + 
        " project                : A Project\n" + 
        " demandType             : Demand\n" + 
        " demandStatus           : To Do\n" + 
        " demandId               : 2\n" + 
        " demandNum              : PROJ-2\n" + 
        " demandSummary          : Smry 2\n" + 
        " demandDescription      : 00002 - Desc @\n" + 
        " taskType               : Task\n" + 
        " taskStatus             : To Do\n" + 
        " taskId                 : 3\n" + 
        " taskNum                : PROJ-3\n" + 
        " taskSummary            : Smry 3\n" + 
        " taskDescription        : 00003 - Desc 3\n" + 
        " taskFullDescription    : Task | 00003 - Desc 3\n" + 
        " taskRelease            : No release set\n" + 
        /////////////
        " subtaskType            : Dev\n" + 
        " subtaskStatus          : To Do\n" + 
        " subtaskId              : 4\n" + 
        " subtaskNum             : PROJ-4\n" + 
        " subtaskSummary         : Smry 4\n" + 
        " subtaskDescription     : M | 00004 - Smry 4\n" + 
        " subtaskFullDescription : Dev | M | 00004 - Desc 4\n" + 
        " tshirtSize             : M\n" + 
        " worklog                : 5.0\n" + 
        " wrongWorklog           : 0.0\n" + 
        " demandBallpark         : 1.0\n" + 
        " taskBallpark           : 2.0\n" + 
        " queryType              : SUBTASK PLAN";
        
        assertEquals(expected, actualRow1.toString());
    }*/
    
    @Test
    public void oneSubtask_shouldCreateOnlyOneSubTaskLinkedWithDemandAndTask() {
        issues( 
                demand (2L, "PROJ-2", "Smry 2", "Desc 2", statusToDo, 60, 0),
                task(3L, "PROJ-3", "Smry 3", "Desc 3", statusToDo, 120, 0, "PROJ-2", taskIssueType),
                subtask(4L, "PROJ-4", "Smry 4", "Desc 4", statusToDo, 300,   "PROJ-3", devIssueType, tshirtSizeInfo.getExtraLarge())
        );
        List<FollowUpData> actual = subject.getJiraData();

        FollowUpData actualRow1 = actual.get(0);
        String expectedFollowupForRow1=
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
        " queryType              : SUBTASK PLAN";
        
        assertEquals(expectedFollowupForRow1, actualRow1.toString());
    }

    private void assertFirstBallpark(List<FollowUpData> actual) {
        FollowUpData actualData0 = actual.get(0);

        String ballpark1 = 
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
            " queryType              : DEMAND BALLPARK";

        assertEquals(ballpark1, actualData0.toString());
    }

    private void sortJiraDataByIssuesKeys(List<FollowUpData> actual) {
        Collections.sort(actual, new Comparator<FollowUpData>() {
            @Override
            public int compare(FollowUpData o1, FollowUpData o2) {
                return (o1.demandNum+o1.taskNum+o1.subtaskNum).compareTo(o2.demandNum+o2.taskNum+o2.subtaskNum);
            }
        });
    }
    
    private void issues(Issue ... i) {
        when(issueBufferService.getIssues()).thenReturn(asList(i)); 
    }
    
    private Issue demand(Long id, 
	        String key, 
	        String summary, 
	        String description, 
	        Long status,
	        Integer originalEstimateMinutes,
	        Integer timeSpentMinutes) {
        return Issue.from(id, 
                key, 
                getProjectKey(), //project key
                getProjectName(),//project name 
                demandIssueType, 
                null, 
                summary, 
                status,//status 
                0L, 
                null, 
                null, 
                null,//parent 
                0L, 
                null, 
                new ArrayList<String>(),//dependencies 
                null, 
                null, 
                null, 
                null, 
                0L, 
                null, 
                0L, 
                description, 
                null, 
                null, 
                null, 
                null, 
                null, 
                0L,
                new Issue.TaskboardTimeTracking(originalEstimateMinutes, timeSpentMinutes));
    }
	
	//substask(3L, "PROJ-3", "Smry 2", "Desc 2", 14L, 300, "PROJ-2")
	private Issue subtask(
	        Long id,
            String key, 
            String summary, 
            String description, 
            Long status,
            Integer timeSpentMinutes,
            String parentKey,
            Long issueType, 
            String tShirtSize) {
        Map<String, Object> cf = new LinkedHashMap<String, Object>();
        //((CustomField)i.getCustomFields().get(jiraProperties.getCustomfield().getTShirtSize().getMainTShirtSizeFieldId())).getValue()
        cf.put(tshirtSizeInfo.getMainTShirtSizeFieldId(), new objective.taskboard.data.CustomField("T-Shirt Size", tShirtSize));
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
                parentKey, 
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
                description, 
                null, //teams
                null, //comments
                null, //labels
                null, //components
                cf, //customFields
                0L,   //priorityOrder
                new Issue.TaskboardTimeTracking(0, timeSpentMinutes));
    }
	
	private Issue task(
            Long id,
            String key, 
            String summary, 
            String description, 
            Long status,
            Integer originalEstimateMinutes,
            Integer timeSpentMinutes,
            String parentKey,
            Long issueType) {
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
                parentKey, 
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
                description, 
                null, //teams
                null, //comments
                null, //labels
                null, //components
                null, //customFields
                0L,   //priorityOrder
                new Issue.TaskboardTimeTracking(originalEstimateMinutes, timeSpentMinutes));
    }

    private String getProjectName() {
        return "A Project";
    }

    private String getProjectKey() {
        return "PROJ";
    }
}
