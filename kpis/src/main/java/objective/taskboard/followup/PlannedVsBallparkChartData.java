package objective.taskboard.followup;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PlannedVsBallparkChartData {

    @JsonProperty("name")
    public String type;
    @JsonProperty("y")
    public double totalEffort;
    
    public PlannedVsBallparkChartData(String type, double totalEffort) {
        this.type = type;
        this.totalEffort = totalEffort;
    }
}
