package objective.taskboard.followup;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class AnalyticsTransitionsDataRow {
    public final String issueKey;
    public final List<Date> lastTransitionDate = new LinkedList<Date>();
    
    public AnalyticsTransitionsDataRow(String issueKey) {
        this.issueKey = issueKey;
    }
}
