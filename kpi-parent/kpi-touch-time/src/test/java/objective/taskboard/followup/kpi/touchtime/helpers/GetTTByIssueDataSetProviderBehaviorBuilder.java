package objective.taskboard.followup.kpi.touchtime.helpers;

public class GetTTByIssueDataSetProviderBehaviorBuilder extends GetTTDataSetProviderBehaviorBuilder<GetTTByIssueDataSetProviderBehavior>{
    public GetTTByIssueDataSetProviderBehaviorBuilder() {
        forMethod("byIssue");
    }

    @Override
    GetTTByIssueDataSetProviderBehavior doBuild() {
        return new GetTTByIssueDataSetProviderBehavior(getMethodName(), getProjectKey(), getKpiLevel(), getTimezone());
    }
}