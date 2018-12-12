package objective.taskboard.followup.kpi.enviroment;

import static objective.taskboard.followup.kpi.KpiLevel.DEMAND;
import static objective.taskboard.followup.kpi.KpiLevel.FEATURES;
import static objective.taskboard.followup.kpi.KpiLevel.SUBTASKS;
import static org.mockito.Mockito.when;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.mockito.Mockito;

import objective.taskboard.data.Issue;
import objective.taskboard.data.Worklog;
import objective.taskboard.followup.IssueTransitionService;
import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.IssueTypeKpi;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.StatusTransition;
import objective.taskboard.followup.kpi.SubtaskWorklogDistributor;
import objective.taskboard.followup.kpi.enviroment.StatusTransitionBuilder.DefaultStatus;
import objective.taskboard.followup.kpi.transformer.IssueKpiDataItemAdapter;
import objective.taskboard.testUtils.FixedClock;
import objective.taskboard.utils.Clock;
import objective.taskboard.utils.DateTimeUtils;

public class IssueKpiBuilder {

    private final String pKey;
    private Optional<IssueTypeKpi> type = Optional.empty();
    private final KpiLevel level;
    private StatusTransitionBuilder statusBuilder = new StatusTransitionBuilder();
    private Optional<StatusTransition> firstStatus = Optional.empty();
    private List<Worklog> worklogs = new LinkedList<>();
    private List<IssueKpi> children  = new LinkedList<>();
    private String projectKey;
    private DefaultStatus status;
    private String parentKey;
    private Clock clock;
    
    public IssueKpiBuilder(String pKey, IssueTypeKpi type, KpiLevel level, Clock clock) {
        this.pKey = pKey;
        this.type = Optional.ofNullable(type);
        this.level = level;
        this.clock = clock;
    }

    public IssueKpiBuilder(String pKey, IssueTypeKpi type, KpiLevel level) {
        this(pKey, type, level, new FixedClock());
    }

    public IssueKpiBuilder addTransition(DefaultStatus step) {
        statusBuilder.addTransition(step);
        return this;
    }
    
    public IssueKpiBuilder addTransition(DefaultStatus step, String date) {
        statusBuilder.addTransition(step,date);
        return this;
    }
    
    public IssueKpiBuilder setFirstStatus(Optional<StatusTransition> firstStatus) {
        this.firstStatus = firstStatus;
        return this;
    }
    
    public IssueKpiBuilder addWorklog(String date, int time) {
        Worklog worklog = new Worklog("a.developer", DateTimeUtils.parseStringToDate(date), time);
        addWorklog(worklog);
        return this;
    }
    
    public IssueKpiBuilder addWorklog(Worklog worklog) {
        this.worklogs.add(worklog);
        return this;
    }
    
    public IssueKpiBuilder addChild(IssueKpi child) {
        children.add(child);
        return this;
    }
    
    public IssueKpiBuilder withProjectKey(String projectKey) {
        this.projectKey = projectKey;
        return this;
    }
    
    public IssueKpiBuilder withFather(String fatherKey) {
        this.parentKey = fatherKey;
        return this;
    }
    
    public IssueKpiBuilder withStatus(DefaultStatus status) {
        this.status = status;
        return this;
    }

    public IssueKpi build() {
        Optional<StatusTransition> firstChain = firstStatus.isPresent() ? firstStatus: statusBuilder.build();
        IssueKpi kpi = new IssueKpi(pKey, type, level,firstChain, clock);
        addChildren(kpi);
        SubtaskWorklogDistributor distributor = new SubtaskWorklogDistributor();
        distributor.distributeWorklogs(kpi, this.worklogs);
        
        return kpi;
    }
    
    public IssueKpiDataItemAdapter buildAdapter() {
        return new FakeIssueKpi(statusBuilder.getReversedTransitions(), pKey, type.orElse(new IssueTypeKpi(0l,"Unconfigured")), level);
    }
    
    private void addChildren(IssueKpi kpi) {
        for (IssueKpi issueKpi : children) {
            kpi.addChild(issueKpi);
        }
    }
    
    public String getIssueKey() {
        return pKey;
    }
    
    public Issue mockIssue(IssueTransitionService transitionService) {
        Issue issue = Mockito.mock(Issue.class);
        when(issue.getProjectKey()).thenReturn(projectKey);
        when(issue.getStatus()).thenReturn(getStatusId());
        when(issue.getIssueKey()).thenReturn(pKey);
        when(issue.getIssueTypeName()).thenReturn(type.map(t -> t.getType()).orElse("Unmapped"));
        when(issue.getParent()).thenReturn(parentKey);
        when(issue.isDemand()).thenReturn(DEMAND == this.level);
        when(issue.isFeature()).thenReturn(FEATURES == this.level);
        when(issue.isSubTask()).thenReturn(SUBTASKS == this.level);
        when(issue.getWorklogs()).thenReturn(worklogs);
        
        when(transitionService.getTransitions(issue, ZoneId.systemDefault(), statusBuilder.getReversedStatusOrder())).thenReturn(statusBuilder.getReversedTransitions());
        
        return issue;
    }

    private Long getStatusId() {
        if(status != null)
            return status.id;
        return statusBuilder.lastTransitionStatusId();
    }
    
    private class FakeIssueKpi implements IssueKpiDataItemAdapter {

        private Map<String,ZonedDateTime> transitions = new LinkedHashMap<>();
        private String issueKey;
        private IssueTypeKpi issueType;
        private KpiLevel level;
        
        public FakeIssueKpi(Map<String, ZonedDateTime> transitions, String issueKey, IssueTypeKpi issueType, KpiLevel level) {
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
        public Optional<IssueTypeKpi> getIssueType() {
            return Optional.of(issueType);
        }

        @Override
        public KpiLevel getLevel() {
            return level;
        }
        
    }

}
