package objective.taskboard.followup;

import java.util.List;

public class FromJiraDataSet {
    public List<String> headers;
    public List<FromJiraDataRow> rows;

    public FromJiraDataSet(List<String> headers, List<FromJiraDataRow> rows) {
        this.headers = headers;
        this.rows = rows;
    }
}
