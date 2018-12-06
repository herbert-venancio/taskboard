package objective.taskboard.followup;

import java.util.List;

public class AnalyticsTransitionsDataSet extends TransitionDataSet<AnalyticsTransitionsDataRow> {

    public AnalyticsTransitionsDataSet(String issueType, List<String> headers, List<AnalyticsTransitionsDataRow> rows) {
        super(issueType, headers, rows);
    }

    public int getInitialIndexStatusHeaders() {
        return headers.size() - rows.get(0).transitionsDates.size();
    }
    
    public List<String> getStatusHeader(){
        return headers.subList(getInitialIndexStatusHeaders(), headers.size());
    }

}
