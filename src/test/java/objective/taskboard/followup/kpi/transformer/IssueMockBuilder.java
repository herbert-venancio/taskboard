package objective.taskboard.followup.kpi.transformer;

import static objective.taskboard.followup.kpi.KpiLevel.DEMAND;
import static objective.taskboard.followup.kpi.KpiLevel.FEATURES;
import static objective.taskboard.followup.kpi.KpiLevel.SUBTASKS;
import static objective.taskboard.utils.DateTimeUtils.parseDateTime;
import static org.mockito.Mockito.when;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Assert;
import org.mockito.Mockito;

import objective.taskboard.data.Issue;
import objective.taskboard.followup.IssueTransitionService;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.transformer.IssueKpiDataItemAdapter;

public class IssueMockBuilder {
    
    private IssueTransitionService transitionService;
    private String[] statuses;
    
    private String pKey;
    private String projectKey;
    private long status;
    private Map<String,ZonedDateTime> transitions = new LinkedHashMap<>();
    private KpiLevel level;
    private String type;
    private String parentKey;
    
    public IssueMockBuilder(String[] statuses) {
        this.statuses = statuses;
        
        for (String status : statuses) {
            transitions.put(status,null);
        }
        
    }
    
    public IssueMockBuilder withKey(String pKey) {
        this.pKey = pKey;
        return this;
    }
    
    public IssueMockBuilder withProjectKey(String projectKey) {
        this.projectKey = projectKey;
        return this;
    }
    
    public IssueMockBuilder withType(String type) {
        this.type = type;
        return this;
    }
    
    public IssueMockBuilder withLevel(KpiLevel level) {
        this.level = level;
        return this;
    }
    
    public IssueMockBuilder withStatusId(long statusId) {
        this.status = statusId;
        return this;
    }
    
    public IssueMockBuilder withParent(String parentKey) {
        this.parentKey = parentKey;
        return this;
    }
    
    public IssueMockBuilder setIssueTransitionService(IssueTransitionService transitionService) {
        this.transitionService = transitionService;
        return this;
    }
            
    public IssueMockBuilder addTransition(String status, String dateTime) {
        if(!this.transitions.containsKey(status))
            Assert.fail(String.format("Status %s not expected to have transition", status));
        this.transitions.put(status, parseDateTime(dateTime));
        return this;
    }
    
    public Issue mockIssue() {
        if(this.transitionService == null) {
            Assert.fail("IssueTransitionService not configured");
            return null;
        }
        
        Issue issue = Mockito.mock(Issue.class);
        when(issue.getProjectKey()).thenReturn(projectKey);
        when(issue.getStatus()).thenReturn(status);
        when(issue.getIssueKey()).thenReturn(pKey);
        when(issue.getIssueTypeName()).thenReturn(type);
        when(issue.getParent()).thenReturn(parentKey);
        when(issue.isDemand()).thenReturn(DEMAND == this.level);
        when(issue.isFeature()).thenReturn(FEATURES == this.level);
        when(issue.isSubTask()).thenReturn(SUBTASKS == this.level);
        
        when(transitionService.getTransitions(issue, ZoneId.systemDefault(), statuses)).thenReturn(transitions);
        
        return issue;    
    }
    
    public IssueKpiDataItemAdapter buildIssueKPI() {
        return new FakeIssueKpi(transitions, pKey, type, level);
    }
    
    private class FakeIssueKpi implements IssueKpiDataItemAdapter {

        private Map<String,ZonedDateTime> transitions = new LinkedHashMap<>();
        private String issueKey;
        private String issueType;
        private KpiLevel level;
        
        public FakeIssueKpi(Map<String, ZonedDateTime> transitions, String issueKey, String issueType, KpiLevel level) {
            this.transitions = transitions;
            this.issueKey = issueKey;
            this.issueType = issueType;
            this.level = level;
        }

        @Override
        public Map<String, ZonedDateTime> getTransitions() {
            return transitions;
        }

        @Override
        public String getIssueKey() {
            return issueKey;
        }

        @Override
        public String getIssueType() {
            return issueType;
        }

        @Override
        public KpiLevel getLevel() {
            return level;
        }
        
    }
    
}