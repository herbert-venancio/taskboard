package objective.taskboard.data;

import java.io.Serializable;
import java.util.Optional;

import objective.taskboard.jira.client.JiraTimeTrackingDto;

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

    public static TaskboardTimeTracking fromJira(JiraTimeTrackingDto tt) {
        if (tt == null)
            return null;
        return new TaskboardTimeTracking(tt.getOriginalEstimateMinutes(), tt.getTimeSpentMinutes());
    }

    public Optional<Integer> getOriginalEstimateMinutes() {
        return Optional.ofNullable(originalEstimateMinutes);
    }
    public void setOriginalEstimateMinutes(Integer originalEstimateMinutes) {
        this.originalEstimateMinutes = originalEstimateMinutes;
    }
    public Optional<Integer> getTimeSpentMinutes() {
        return Optional.ofNullable(timeSpentMinutes);
    }
    public void setTimeSpentMinutes(Integer timeSpentMinutes) {
        this.timeSpentMinutes = timeSpentMinutes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((originalEstimateMinutes == null) ? 0 : originalEstimateMinutes.hashCode());
        result = prime * result + ((timeSpentMinutes == null) ? 0 : timeSpentMinutes.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TaskboardTimeTracking other = (TaskboardTimeTracking) obj;
        if (originalEstimateMinutes == null) {
            if (other.originalEstimateMinutes != null)
                return false;
        } else if (!originalEstimateMinutes.equals(other.originalEstimateMinutes))
            return false;
        if (timeSpentMinutes == null) {
            if (other.timeSpentMinutes != null)
                return false;
        } else if (!timeSpentMinutes.equals(other.timeSpentMinutes))
            return false;
        return true;
    }   
}