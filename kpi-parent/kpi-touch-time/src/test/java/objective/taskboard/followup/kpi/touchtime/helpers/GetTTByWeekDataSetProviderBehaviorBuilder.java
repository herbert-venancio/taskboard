package objective.taskboard.followup.kpi.touchtime.helpers;

public class GetTTByWeekDataSetProviderBehaviorBuilder extends GetTTDataSetProviderBehaviorBuilder<GetTTByWeekDataSetProviderBehavior> {
    public GetTTByWeekDataSetProviderBehaviorBuilder() {
        forMethod("byWeek");
    }

    @Override
    GetTTByWeekDataSetProviderBehavior doBuild() {
        return new GetTTByWeekDataSetProviderBehavior(getMethodName(), getProjectKey(), getKpiLevel(), getTimezone());
    }
}