package objective.taskboard.followup.kpi.enviroment;

import org.assertj.core.api.Assertions;

public abstract class RequestChartDataBehaviorBuilder<T extends RequestChartDataBehavior<?> > {
    protected String projectKey;
    protected String level;
    protected String zoneId;
    protected Boolean hasPermission;
    protected boolean preventProviderMock = false;

    public RequestChartDataBehaviorBuilder<T> forProject(String projectKey) {
        this.projectKey = projectKey;
        return this;
    }
    public RequestChartDataBehaviorBuilder<T> preventDataProviderMock() {
        this.preventProviderMock  = true;
        return this;
    }
    public RequestChartDataBehaviorBuilder<T> withLevel(String level) {
        this.level = level;
        return this;
    }
    public RequestChartDataBehaviorBuilder<T> withTimezone(String timezone) {
        this.zoneId = timezone;
        return this;
    }
    public RequestChartDataBehaviorBuilder<T> withPermission() {
        this.hasPermission = true;
        return this;
    }
    public RequestChartDataBehaviorBuilder<T> withoutPermission() {
        this.hasPermission = false;
        return this;
    }

    public T build(){
        validate();
        return doBuild();
    }

    protected abstract T doBuild();

    protected void validate() {
        if (projectKey == null) {
            Assertions.fail("Project key not configured");
        }
        if (level == null) {
            Assertions.fail("Level not configured");
        }
        if (zoneId == null) {
            Assertions.fail("ZoneId not configured");
        }
        if (hasPermission == null) {
            Assertions.fail("Permission not configured");
        }
    }
}
