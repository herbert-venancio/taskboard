package objective.taskboard.followup;

import static java.util.Collections.emptyList;

import java.util.List;

public class AnalyticsTransitionsDataSet extends TransitionDataSet<AnalyticsTransitionsDataRow> {

    public AnalyticsTransitionsDataSet(String issueType, List<String> headers, List<AnalyticsTransitionsDataRow> rows) {
        super(issueType, headers, rows);
    }

    public int getInitialIndexStatusHeaders() {
        return headers.size() - transitionsSize();
    }

    private int transitionsSize() {
        return rows.get(0).transitionsDates.size();
    }
    
    public List<String> getStatusHeader(){
        if(headers.size() < transitionsSize())
            return emptyList();
        return headers.subList(getInitialIndexStatusHeaders(), headers.size());
    }

}
