package objective.taskboard.data;

import java.io.Serializable;

import com.atlassian.jira.rest.client.api.domain.TimeTracking;

public class TaskboardTimeTracking implements Serializable {
    private static final long serialVersionUID = 6559922928445540685L;
    private Integer originalEstimateMinutes;
    private Integer timeSpentMinutes;

    public TaskboardTimeTracking() {
        originalEstimateMinutes = 0;
        timeSpentMinutes = 0;
    }

    public TaskboardTimeTracking(Integer originalEstimateMinutes, Integer timeSpentMinutes) {
        this.originalEstimateMinutes = originalEstimateMinutes;
        this.timeSpentMinutes = timeSpentMinutes;
    }

    public static TaskboardTimeTracking fromJira(TimeTracking tt) {
        if (tt == null)
            return null;
        return new TaskboardTimeTracking(tt.getOriginalEstimateMinutes(), tt.getTimeSpentMinutes());
    }

    public Integer getOriginalEstimateMinutes() {
        return originalEstimateMinutes;
    }
    public void setOriginalEstimateMinutes(Integer originalEstimateMinutes) {
        this.originalEstimateMinutes = originalEstimateMinutes;
    }
    public Integer getTimeSpentMinutes() {
        return timeSpentMinutes;
    }
    public void setTimeSpentMinutes(Integer timeSpentMinutes) {
        this.timeSpentMinutes = timeSpentMinutes;
    }
}