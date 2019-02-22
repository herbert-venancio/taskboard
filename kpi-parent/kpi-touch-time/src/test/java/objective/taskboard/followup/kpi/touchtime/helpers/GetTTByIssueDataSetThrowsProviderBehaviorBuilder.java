package objective.taskboard.followup.kpi.touchtime.helpers;

import java.time.ZoneId;

import org.assertj.core.api.Assertions;

import objective.taskboard.followup.kpi.KpiLevel;

public class GetTTByIssueDataSetThrowsProviderBehaviorBuilder {
    private String methodName;
    private String projectKey;
    private KpiLevel kpiLevel;
    private ZoneId timezone;

    public GetTTByIssueDataSetThrowsProviderBehaviorBuilder forMethod(String methodName) {
        this.methodName = methodName;
        return this;
    }

    public GetTTByIssueDataSetThrowsProviderBehaviorBuilder forProject(String projectKey) {
        this.projectKey = projectKey;
        return this;
    }
    public GetTTByIssueDataSetThrowsProviderBehaviorBuilder withLevel(KpiLevel kpiLevel) {
        this.kpiLevel = kpiLevel;
        return this;
    }
    public GetTTByIssueDataSetThrowsProviderBehaviorBuilder withTimezone(ZoneId timezone) {
        this.timezone = timezone;
        return this;
    }
    public GetTTByIssueDataSetThrowsProviderBehavior build() {
        validate();
        return new GetTTByIssueDataSetThrowsProviderBehavior(methodName, projectKey, kpiLevel, timezone);
    }
    private void validate() {
        Assertions.assertThat(methodName).isNotNull();
        Assertions.assertThat(projectKey).isNotNull();
        Assertions.assertThat(kpiLevel).isNotNull();
        Assertions.assertThat(timezone).isNotNull();
    }
}