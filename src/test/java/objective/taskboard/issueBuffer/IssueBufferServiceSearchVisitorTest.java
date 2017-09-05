package objective.taskboard.issueBuffer;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.internal.json.SearchResultJsonParser;

import objective.taskboard.database.IssuePriorityService;
import objective.taskboard.domain.IssueColorService;
import objective.taskboard.domain.converter.IssueTeamService;
import objective.taskboard.domain.converter.JiraIssueToIssueConverter;
import objective.taskboard.domain.converter.StartDateStepService;
import objective.taskboard.jira.JiraProperties;
import objective.taskboard.jira.JiraProperties.CustomField;
import objective.taskboard.jira.JiraProperties.CustomField.Blocked;
import objective.taskboard.jira.JiraProperties.CustomField.CustomFieldDetails;
import objective.taskboard.jira.JiraProperties.CustomField.TShirtSize;
import objective.taskboard.jira.JiraProperties.IssueLink;
import objective.taskboard.jira.JiraProperties.IssueType;
import objective.taskboard.jira.JiraProperties.IssueType.IssueTypeDetails;
import objective.taskboard.jira.JiraSearchServiceTest;

@RunWith(MockitoJUnitRunner.class)
public class IssueBufferServiceSearchVisitorTest {
    
    @Spy
    JiraProperties properties = new JiraProperties();
    
    @Mock
    IssuePriorityService priorityService;
    
    @Mock
    private StartDateStepService startDateStepService;
    
    @Mock
    private IssueTeamService issueTeamService;
    
    @Mock
    private IssueColorService issueColorService;
    
    @InjectMocks
    JiraIssueToIssueConverter issueConverter ;
    
    @Before
    public void setup() {
        CustomField customFieldConfiguration = new CustomField();
        properties.setCustomfield(customFieldConfiguration);
        CustomFieldDetails coAssignees = new CustomFieldDetails();
        coAssignees.setId("customfield_11456");
        customFieldConfiguration.setCoAssignees(coAssignees);
        customFieldConfiguration.setBlocked(new Blocked());
        CustomFieldDetails lastBlockReason = new CustomFieldDetails();
        lastBlockReason.setId("customfield_11452");
        customFieldConfiguration.setLastBlockReason(lastBlockReason);
        CustomFieldDetails additionalEstimatedHours = new CustomFieldDetails();
        additionalEstimatedHours.setId("customfield_11450");
        customFieldConfiguration.setAdditionalEstimatedHours(additionalEstimatedHours);
        CustomFieldDetails release = new CustomFieldDetails();
        release.setId("customfield_11455");
        customFieldConfiguration.setRelease(release);
        TShirtSize tShirtSize = new TShirtSize();
        tShirtSize.setIds(new ArrayList<String>());
        customFieldConfiguration.setTShirtSize( tShirtSize);
        
        IssueType issueType = new IssueType();
        issueType.setDemand(new IssueTypeDetails());
        properties.setIssuetype(issueType);
        
        when(priorityService.determinePriority(any())).thenReturn(1l);
        when(priorityService.priorityUpdateDate((any()))).thenReturn(Optional.empty());
        
        when(issueColorService.getColor(any())).thenReturn("#FFFFFF");
        
        IssueLink issuelink = new IssueLink();
        issuelink.setDependencies(Arrays.asList());
        properties.setIssuelink(issuelink);
    }
    
    @Test
	public void whenProcessingIssueWithoutParent() throws JSONException {
        IssueBufferServiceSearchVisitor subject = new IssueBufferServiceSearchVisitor(issueConverter);
	    
        SearchResultJsonParser searchResultParser = new SearchResultJsonParser();
        SearchResult searchResult = searchResultParser.parse(new JSONObject(JiraSearchServiceTest.result("TASKB-685")));
        ArrayList<Issue> list = newArrayList(searchResult.getIssues());
        
        list.stream().forEach(subject::processIssue);
        
        Map<String, objective.taskboard.data.Issue> buffer = subject.getIssuesByKey();
        
        ArrayList<String> keys = new ArrayList<String>(buffer.keySet());
        Collections.sort(keys);
        assertEquals("TASKB-685", keys.get(0));
        subject.complete();
	}
    
    @Test
    public void whenIssuesAreReturnedOutOfOrder() throws JSONException {
        IssueBufferServiceSearchVisitor subject = new IssueBufferServiceSearchVisitor(issueConverter);
        
        SearchResultJsonParser searchResultParser = new SearchResultJsonParser();
        SearchResult searchResult = searchResultParser.parse(new JSONObject(JiraSearchServiceTest.result("TASKB-686_TASKB-685")));
        ArrayList<Issue> list = newArrayList(searchResult.getIssues());
        
        list.stream().forEach(subject::processIssue);
        
        Map<String, objective.taskboard.data.Issue> buffer = subject.getIssuesByKey();
        
        ArrayList<String> keys = new ArrayList<String>(buffer.keySet());
        assertEquals("TASKB-685,TASKB-686", StringUtils.join(keys,","));
        subject.complete();
    }
    
    @Test
    public void whenIssuesAreReturnedOutOfOrderWithNestedDependencies() throws JSONException {
        issueConverter.setParentIssueLinks(Arrays.asList("is demanded by"));
        
        IssueBufferServiceSearchVisitor subject = new IssueBufferServiceSearchVisitor(issueConverter);
        
        SearchResultJsonParser searchResultParser = new SearchResultJsonParser();
        SearchResult searchResult = searchResultParser.parse(new JSONObject(JiraSearchServiceTest.result("TASKB-634_TASKB-630_TASKB-628")));
        ArrayList<Issue> list = newArrayList(searchResult.getIssues());
        
        list.stream().forEach(subject::processIssue);
        
        Map<String, objective.taskboard.data.Issue> buffer = subject.getIssuesByKey();
        
        ArrayList<String> keys = new ArrayList<String>(buffer.keySet());
        assertEquals("TASKB-628,TASKB-630,TASKB-634", StringUtils.join(keys,","));
        subject.complete();
    }
    
    @Test
    public void whenIssuesAreReturnedInOrder() throws JSONException {
        IssueBufferServiceSearchVisitor subject = new IssueBufferServiceSearchVisitor(issueConverter);
        
        SearchResultJsonParser searchResultParser = new SearchResultJsonParser();
        SearchResult searchResult = searchResultParser.parse(new JSONObject(JiraSearchServiceTest.result("TASKB-685_TASKB-686")));
        ArrayList<Issue> list = newArrayList(searchResult.getIssues());
        
        list.stream().forEach(subject::processIssue);
        
        Map<String, objective.taskboard.data.Issue> buffer = subject.getIssuesByKey();
        
        ArrayList<String> keys = new ArrayList<String>(buffer.keySet());
        assertEquals("TASKB-685,TASKB-686", StringUtils.join(keys,","));
        subject.complete();
    }
}