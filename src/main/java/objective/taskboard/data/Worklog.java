package objective.taskboard.data;

import java.io.Serializable;
import java.util.Date;

import objective.taskboard.jira.client.JiraWorklogDto;

public class Worklog implements Serializable {
    private static final long serialVersionUID = -9146377115189042747L;
    
    public Date started;
    public int timeSpentSeconds;
    public String author;
    
    public Worklog(String author, Date started, int timeSpentSeconds) {
        this.author = author;
        this.started = started;
        this.timeSpentSeconds = timeSpentSeconds;
    }

    public static Worklog from(JiraWorklogDto w) {
        return new Worklog(w.author.getName(), w.started, w.timeSpentSeconds);
    }
}