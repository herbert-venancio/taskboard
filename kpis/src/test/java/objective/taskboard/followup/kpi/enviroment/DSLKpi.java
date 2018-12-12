package objective.taskboard.followup.kpi.enviroment;

import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.IssueKpiAsserter;
import objective.taskboard.followup.kpi.StatusTransition;
import objective.taskboard.followup.kpi.StatusTransitionAsserter;

public class DSLKpi {
    
    private KpiEnvironment environment = new KpiEnvironment(this);
    private AsserterFactory assertFactory = new AsserterFactory();
    private Map<String,IssueKpi> issues = new LinkedHashMap<>();
    
    public KpiEnvironment environment() {
        return environment;
    }
    
    public IssueKpi getIssueKpi(String pKey) {
        issues.putIfAbsent(pKey, environment.givenIssue(pKey).buildIssueKpi());
        return issues.get(pKey);
    }

    public AsserterFactory assertThat() {
        return assertFactory;
    }
    
    public class AsserterFactory{
        
        private Map<String, IssueKpiAsserter> issuesAsserter = new LinkedHashMap<>();
        
        public IssueKpiAsserter issueKpi(String pKey) {
            issuesAsserter.putIfAbsent(pKey,new IssueKpiAsserter(getIssueKpi(pKey), environment));
            return issuesAsserter.get(pKey);
        }

        public StatusTransitionAsserter statusTransition() {
            Optional<StatusTransition> firstStatus = environment.statusTransition().getFirtStatusTransition();
            ZoneId timezone = environment().getTimezone();
            return new StatusTransitionAsserter(timezone,firstStatus);
        }
        
    }
    

}
