package objective.taskboard.followup.kpi.touchtime.helpers;

public class GetTTByIssueDataSetBehaviorProviderBuilder extends GetTTDataSetProviderBehaviorBuilder<GetTTByIssueDataSetProviderBehavior>{
    public GetTTByIssueDataSetBehaviorProviderBuilder() {
        super("byIssue");
    }

    @Override
    GetTTByIssueDataSetProviderBehavior doBuild() {
        return new GetTTByIssueDataSetProviderBehavior(getMethodName(), getProjectKey(), getKpiLevel(), getTimezone());
    }
}