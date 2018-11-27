package objective.taskboard.followup;

public class FollowUpScopeByTypeDataItem {
    public String type;
    public Double effortEstimate;
    public Double percent;

    public FollowUpScopeByTypeDataItem(String type, Double effortEstimate, Double percent) {
        this.type = type;
        this.effortEstimate = effortEstimate;
        this.percent = percent;
    }
}
