package objective.taskboard.followup.kpi;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import objective.taskboard.data.Worklog;

public class ZonedWorklog {
    private ZonedDateTime started;
    
    private int timeSpentSeconds;
    public ZonedWorklog(Worklog worklog, ZoneId timezone) {
        this.started = ZonedDateTime.ofInstant(worklog.started.toInstant(), timezone);
        this.timeSpentSeconds = worklog.timeSpentSeconds;
    }
    ZonedDateTime getStarted() {
        return started;
    }
    int getTimeSpentSeconds() {
        return timeSpentSeconds;
    }
}
