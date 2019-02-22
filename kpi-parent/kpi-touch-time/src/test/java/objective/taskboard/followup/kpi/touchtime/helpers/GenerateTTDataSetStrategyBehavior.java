package objective.taskboard.followup.kpi.touchtime.helpers;

import java.time.ZoneId;

import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.enviroment.DSLSimpleBehaviorWithAsserter;
import objective.taskboard.followup.kpi.enviroment.KpiEnvironment;

abstract class GenerateTTDataSetStrategyBehavior<DSA> implements DSLSimpleBehaviorWithAsserter<DSA> {
    private String projectKey;
    private KpiLevel issueLevel;
    private ZoneId timezone;
    private DSA asserter;

    GenerateTTDataSetStrategyBehavior(String projectKey, KpiLevel issueLevel, ZoneId timezone) {
        this.projectKey = projectKey;
        this.issueLevel = issueLevel;
        this.timezone = timezone;
    }

    @Override
    public void behave(KpiEnvironment environment) {
        this.asserter = doBehave(environment);
    }

    @Override
    public DSA then() {
        return asserter;
    }

    abstract DSA doBehave(KpiEnvironment environment);

    String getProjectKey() {
        return projectKey;
    }

    KpiLevel getIssueLevel() {
        return issueLevel;
    }

    ZoneId getTimezone() {
        return timezone;
    }

    DSA getAsserter() {
        return asserter;
    }
}