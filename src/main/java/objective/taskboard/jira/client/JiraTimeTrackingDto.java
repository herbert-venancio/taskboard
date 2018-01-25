package objective.taskboard.jira.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class JiraTimeTrackingDto {
    
    @JsonProperty
    public Integer originalEstimateSeconds;
    @JsonProperty
    public Integer timeSpentSeconds;
    
    public JiraTimeTrackingDto(){}

    public JiraTimeTrackingDto(Integer timeEstimateMinutes, Object object, Integer timeSpentMinutes) {
        this.originalEstimateSeconds = timeEstimateMinutes * 60;
        this.timeSpentSeconds = timeEstimateMinutes * 60;
    }

    public Integer getOriginalEstimateMinutes() {
        return originalEstimateSeconds == null? null:
            originalEstimateSeconds/60;
    }

    public Integer getTimeSpentMinutes() {
        return timeSpentSeconds == null? null:
            timeSpentSeconds/60;
    }

}
