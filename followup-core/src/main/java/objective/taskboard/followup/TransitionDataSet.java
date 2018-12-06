package objective.taskboard.followup;

import java.util.List;

public class TransitionDataSet<T extends TransitionDataRow> {

    public final String issueType;
    public final List<String> headers;
    public final List<T> rows;

    public TransitionDataSet(String issueType, List<String> headers, List<T> rows) {
        super();
        this.issueType = issueType;
        this.headers = headers;
        this.rows = rows;
    }
}