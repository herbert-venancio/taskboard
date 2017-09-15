package objective.taskboard.followup;

import java.util.List;

public class AnalyticsTransitionsDataSet {

    public final String issueType;
    public final List<String> headers;
    public final List<AnalyticsTransitionsDataRow> rows;

    public AnalyticsTransitionsDataSet(String issueType, List<String> headers, List<AnalyticsTransitionsDataRow> dataRows) {
        this.issueType = issueType;
        this.headers = headers;
        this.rows = dataRows;
    }

    @Override
    public String toString() {
        return "AnalyticsTransitionsDataSet [issueType=" + issueType + ", headers=" + headers + ", rows=" + rows + "]";
    }
}
