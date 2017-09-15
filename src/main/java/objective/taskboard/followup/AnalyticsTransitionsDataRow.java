package objective.taskboard.followup;

import java.util.List;

import org.joda.time.DateTime;

public class AnalyticsTransitionsDataRow {
    public final String issueKey;
    public final List<DateTime> transitionsDates;
    
    public AnalyticsTransitionsDataRow(String issueKey, List<DateTime> lastTransitionDate) {
        this.issueKey = issueKey;
        this.transitionsDates = lastTransitionDate;
    }

    @Override
    public String toString() {
        return "AnalyticsTransitionsDataRow [issueKey=" + issueKey + ", transitionsDates=" + transitionsDates + "]";
    }
}