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
    private BehaviorFactory behaviorFactory = new BehaviorFactory(this);
    private Map<String,IssueKpi> issues = new LinkedHashMap<>();

    public KpiEnvironment environment() {
        return environment;
    }

    public IssueKpi getIssueKpi(String pKey) {
        issues.putIfAbsent(pKey, environment.givenIssue(pKey).buildIssueKpi());
        return issues.get(pKey);
    }

    public BehaviorFactory when() {
        return behaviorFactory;
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
            Optional<StatusTransition> firstStatus = environment.statusTransition().getFirstStatusTransition();
            ZoneId timezone = environment().getTimezone();
            return new StatusTransitionAsserter(timezone,firstStatus);
        }

    }

    public static class BehaviorFactory {

        private final DSLKpi kpiContext;
        private Map<String,IssueBehavior> issues = new LinkedHashMap<>();

        public BehaviorFactory(DSLKpi kpiContext) {
            this.kpiContext = kpiContext;
        }

        public IssueBehavior givenIssueKpi(String pkey) {
            return issues.computeIfAbsent(pkey, (key) -> new IssueBehavior(kpiContext.getIssueKpi(key)));
        }
        
        public <T> ExceptionBehavior<T> expectExceptionFromBehavior(DSLSimpleBehavior<T> behavior){
            ExceptionBehavior<T> exceptionBehavior = new ExceptionBehavior<>(behavior);
            exceptionBehavior.behave(kpiContext.environment);
            return exceptionBehavior;
        }

        public <T> DSLSimpleBehavior<T> appliesBehavior(DSLSimpleBehavior<T> behavior) {
            behavior.behave(kpiContext.environment);
            return behavior;
        }

        public class IssueBehavior {
            private IssueKpi kpi;

            public IssueBehavior(IssueKpi kpi) {
                this.kpi = kpi;
            }

            public IssueBehavior appliesBehavior(DSLBehavior<IssueKpi> behavior) {
                behavior.execute(kpiContext.environment, kpi);
                return this;
            }

            public DSLKpi then() {
                return kpiContext;
            }
        }
    }


}
