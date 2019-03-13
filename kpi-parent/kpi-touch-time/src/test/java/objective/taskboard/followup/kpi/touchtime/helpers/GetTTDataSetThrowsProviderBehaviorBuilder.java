package objective.taskboard.followup.kpi.touchtime.helpers;

public class GetTTDataSetThrowsProviderBehaviorBuilder extends GetTTDataSetProviderBehaviorBuilder<GetTTByIssueDataSetProviderBehavior> {

    @Override
    GetTTByIssueDataSetProviderBehavior doBuild() {
        return new GetTTByIssueDataSetProviderBehavior(getMethodName(), getProjectKey(), getKpiLevel(), getTimezone());
    }
}