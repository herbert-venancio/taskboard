package objective.taskboard.followup.kpi.services;

import org.assertj.core.api.Assertions;

public abstract class RequestChartDataBehaviorBuilder<T extends RequestChartDataBehavior<?> > {
    protected String projectKey;
    protected String level;
    protected String zoneId;
    protected Boolean hasPermission;
    protected boolean preventProviderMock = false;
    private boolean requireLevel = true;

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
    
    public RequestChartDataBehaviorBuilder<T> withNoLevelConfigured() {
        this.requireLevel = false;
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
        
        validateProjectKey();
        validateZoneId();
        validatePermission();
        validateLevel();
        
    }
    
    private void validateLevel() {
        if(!requireLevel)
            return;
        
        if (level == null) 
            Assertions.fail("Level not configured");
    }
    private void validatePermission() {
        if (hasPermission == null)
            Assertions.fail("Permission not configured");
    }
    private void validateZoneId() {
        if (zoneId == null)
            Assertions.fail("ZoneId not configured");
    }
    private void validateProjectKey() {
        if (projectKey == null)
            Assertions.fail("Project key not configured");
    }
}
