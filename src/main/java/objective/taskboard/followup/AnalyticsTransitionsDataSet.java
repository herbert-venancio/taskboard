package objective.taskboard.followup;

import java.util.List;

public class AnalyticsTransitionsDataSet extends TransitionDataSet<AnalyticsTransitionsDataRow> {

    public AnalyticsTransitionsDataSet(String issueType, List<String> headers, List<AnalyticsTransitionsDataRow> rows) {
        super(issueType, headers, rows);
    }
}
