package objective.taskboard.followup.kpi.enviroment;

import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.assertj.core.api.Assertions;

import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.IssueKpiAsserter;
import objective.taskboard.followup.kpi.StatusTransition;
import objective.taskboard.followup.kpi.StatusTransitionAsserter;
import objective.taskboard.followup.kpi.cycletime.CycleTimeKpi;
import objective.taskboard.followup.kpi.cycletime.CycleTimeKpiAsserter;
import objective.taskboard.followup.kpi.cycletime.CycleTimeKpiFactory;
import objective.taskboard.followup.kpi.leadTime.LeadTimeKpiAsserter;
import objective.taskboard.followup.kpi.leadtime.LeadTimeKpi;

public class DSLKpi {

    private KpiEnvironment environment = new KpiEnvironment(this);
    private AsserterFactory assertFactory = new AsserterFactory();
    private BehaviorFactory behaviorFactory = new BehaviorFactory(this);
    private Map<String,IssueKpi> issues;

    public KpiEnvironment environment() {
        return environment;
    }

    public DSLKpi withNoIssuesConfigured() {
        Assertions.assertThat(environment.getAllIssueMockers()).hasSize(0);
        return this;
    }

    public IssueKpi getIssueKpi(String pKey) {
        initializeIssuesIfNeeded();

        Assertions.assertThat(issues).as("Issue %s not found",pKey).containsKey(pKey);
        return issues.get(pKey);
    }

    private void initializeIssuesIfNeeded() {
        if(this.issues == null)
            this.issues = environment.services().issueKpi().getIssues();
    }

    public BehaviorFactory when() {
        environment.services().mockAll();

        return behaviorFactory;
    }

    public AsserterFactory assertThat() {
        return assertFactory;
    }

    public class AsserterFactory{

        private Map<String, IssueKpiAsserter<AsserterFactory>> issuesAsserter = new LinkedHashMap<>();

        public IssueKpiAsserter<AsserterFactory> issueKpi(String pKey) {
            issuesAsserter.putIfAbsent(pKey,new IssueKpiAsserter<AsserterFactory>(getIssueKpi(pKey), environment,this));
            return issuesAsserter.get(pKey);
        }

        public StatusTransitionAsserter statusTransition() {
            Optional<StatusTransition> firstStatus = environment.statusTransition().getFirstStatusTransition();
            ZoneId timezone = environment().getTimezone();
            return new StatusTransitionAsserter(timezone,firstStatus);
        }

        public CycleTimeKpiAsserter<AsserterFactory> cycleTimeKpi(String pkey) {
            IssueKpi kpi = getIssueKpi(pkey);
            CycleTimeKpiFactory cycleTimeKpiFactory = new CycleTimeKpiFactory(
                    environment.withKpiProperties().getCycleStatusMap(),
                    environment.getTimezone(),
                    environment.services().issueColor().getService());
            CycleTimeKpi cKpi = cycleTimeKpiFactory.create(kpi);
            return new CycleTimeKpiAsserter<DSLKpi.AsserterFactory>(cKpi, this);
        }

        public LeadTimeKpiAsserter leadTimeKpi(String pKey) {
            LeadTimeKpi lKpi = environment.getLeadTimeKpi(pKey);
            return new LeadTimeKpiAsserter(lKpi);
        }

    }

    public static class BehaviorFactory {

        private final DSLKpi kpiContext;
        private Map<String,IssueBehavior> issues = new LinkedHashMap<>();

        public BehaviorFactory(DSLKpi kpiContext) {
            this.kpiContext = kpiContext;
        }

        public IssueBehavior givenIssueKpi(String pkey) {
            return issues.computeIfAbsent(pkey, (key) -> new IssueBehavior(kpiContext.environment.givenIssue(key)));
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
            private IssueKpiMocker kpi;

            public IssueBehavior(IssueKpiMocker issueKpiMocker) {
                this.kpi = issueKpiMocker;
            }

            public IssueBehavior appliesBehavior(DSLBehavior<IssueKpiMocker> behavior) {
                behavior.execute(kpiContext.environment, kpi);
                return this;
            }

            public DSLKpi then() {
                return kpiContext;
            }
        }
    }
}
