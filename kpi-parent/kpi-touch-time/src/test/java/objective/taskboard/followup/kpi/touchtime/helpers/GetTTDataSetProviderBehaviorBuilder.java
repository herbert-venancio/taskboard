package objective.taskboard.followup.kpi.touchtime.helpers;

import java.time.ZoneId;

import org.assertj.core.api.Assertions;

import objective.taskboard.followup.kpi.KpiLevel;

public abstract class GetTTDataSetProviderBehaviorBuilder<DSB> {
    private String methodName;
    private String projectKey;
    private KpiLevel kpiLevel;
    private ZoneId timezone;

    public GetTTDataSetProviderBehaviorBuilder<DSB> forMethod(String methodName) {
        this.methodName = methodName;
        return this;
    }

    public GetTTDataSetProviderBehaviorBuilder<DSB> forProject(String projectKey) {
        this.projectKey = projectKey;
        return this;
    }

    public GetTTDataSetProviderBehaviorBuilder<DSB> withLevel(KpiLevel kpiLevel) {
        this.kpiLevel = kpiLevel;
        return this;
    }

    public GetTTDataSetProviderBehaviorBuilder<DSB> withTimezone(ZoneId timezone) {
        this.timezone = timezone;
        return this;
    }

    abstract DSB doBuild();

    public DSB build() {
        validate();
        return doBuild();
    }

    String getMethodName() {
        return methodName;
    }

    String getProjectKey() {
        return projectKey;
    }

    KpiLevel getKpiLevel() {
        return kpiLevel;
    }

    ZoneId getTimezone() {
        return timezone;
    }

    private void validate() {
        Assertions.assertThat(methodName).isNotNull();
        Assertions.assertThat(projectKey).isNotNull();
        Assertions.assertThat(kpiLevel).isNotNull();
        Assertions.assertThat(timezone).isNotNull();
    }
}