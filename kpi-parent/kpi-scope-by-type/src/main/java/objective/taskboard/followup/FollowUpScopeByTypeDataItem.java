package objective.taskboard.followup;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FollowUpScopeByTypeDataItem {
    @JsonProperty("name")
    public String type;
    @JsonProperty("y")
    public Double effortEstimate;

    public FollowUpScopeByTypeDataItem(String type, Double effortEstimate) {
        this.type = type;
        this.effortEstimate = effortEstimate;
    }
}
