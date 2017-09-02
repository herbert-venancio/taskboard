package objective.taskboard.jira;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.atlassian.jira.rest.client.api.domain.Issue;

import objective.taskboard.jira.JiraProperties.CustomField;
import objective.taskboard.jira.JiraProperties.CustomField.Blocked;
import objective.taskboard.jira.JiraProperties.CustomField.CustomFieldDetails;
import objective.taskboard.jira.JiraProperties.CustomField.TShirtSize;
import objective.taskboard.jira.JiraProperties.IssueType;
import objective.taskboard.jira.JiraProperties.IssueType.IssueTypeDetails;
import objective.taskboard.jira.endpoint.JiraEndpointAsMaster;
import objective.taskboard.utils.IOUtilities;

@RunWith(MockitoJUnitRunner.class)
public class JiraSearchServiceTest {
    @Spy
    JiraProperties properties = new JiraProperties();
    
    @Mock
    JiraEndpointAsMaster jiraEndpointAsMaster;
    
    @InjectMocks
    JiraSearchService subject;
    
    @Before
    public void setup() {
        CustomField customFieldConfiguration = new CustomField();
        properties.setCustomfield(customFieldConfiguration);
        customFieldConfiguration.setCoAssignees(new CustomFieldDetails());
        customFieldConfiguration.setBlocked(new Blocked());
        customFieldConfiguration.setLastBlockReason(new CustomFieldDetails());
        customFieldConfiguration.setAdditionalEstimatedHours(new CustomFieldDetails());
        customFieldConfiguration.setRelease(new CustomFieldDetails());
        TShirtSize tShirtSize = new TShirtSize();
        tShirtSize.setIds(new ArrayList<String>());
        customFieldConfiguration.setTShirtSize( tShirtSize);
        
        IssueType issueType = new IssueType();
        issueType.setDemand(new IssueTypeDetails());
        properties.setIssuetype(issueType);
    }
    
    
    @Test
	public void searchIssues_happyDay() {
        when(jiraEndpointAsMaster.postWithRestTemplate(Matchers.anyString(), Matchers.any(), Matchers.any()))
            .thenReturn(result("TASKB-688"));
        
        SearchAndCollectFoundIssues collector = new SearchAndCollectFoundIssues();
        subject.searchIssuesAndParents(collector,"");
	    
	    assertEquals("TASKB-688", collector.collectedIssues());
	}
    
    @Test
    public void whenSearchIssuesWithParents_WithoutParents_ShouldProcessOnlyTheIssue() {
        when(jiraEndpointAsMaster.postWithRestTemplate(Matchers.anyString(), Matchers.any(), Matchers.any()))
            .thenReturn(result("TASKB-688"));
        
        SearchAndCollectFoundIssues collector = new SearchAndCollectFoundIssues();
        subject.searchIssuesAndParents(collector,"");
        
        assertEquals("TASKB-688", collector.collectedIssues());
    }
    
    @Test
    public void whenSearchIssuesWithParents_WithParent_ShouldProcessIssueAndParent() {
        when(jiraEndpointAsMaster.postWithRestTemplate(Matchers.anyString(), Matchers.any(), Matchers.any()))
        .thenAnswer(new Answer<String>() {

            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                JSONObject argumentAt = invocation.getArgumentAt(2, JSONObject.class);
                String jql = argumentAt.getString("jql");
                if (jql.contains("TASKB-685"))
                    return result("TASKB-685");
                return result("TASKB-686");
            }
        });
        
        SearchAndCollectFoundIssues collector = new SearchAndCollectFoundIssues();
        subject.searchIssuesAndParents(collector,"");
        
        assertEquals("TASKB-686,TASKB-685", collector.collectedIssues());
    }
    

    public static String result(String string) {
        return IOUtilities.resourceToString(IOUtilities.class, "/objective-jira-teste/"+"search_" + string + ".json");
    }
    
    
    private static class SearchAndCollectFoundIssues implements SearchIssueVisitor {
        ArrayList<String> processedIssues = new ArrayList<String>();
        @Override
        public void processIssue(Issue item) {
            processedIssues.add(item.getKey());
        }
        
        public String collectedIssues() {
            return StringUtils.join(processedIssues,",");
        }
    }    
}