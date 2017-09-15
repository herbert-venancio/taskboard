package objective.taskboard.followup;

import java.util.List;

public class SyntheticTransitionsDataSet {

    public final String issueType;
    public final List<String> headers;
    public final List<SyntheticTransitionsDataRow> rows;

    public SyntheticTransitionsDataSet(String issueType, List<String> headers, List<SyntheticTransitionsDataRow> rows) {
        this.issueType = issueType;
        this.headers = headers;
        this.rows = rows;
    }

    @Override
    public String toString() {
        return "SyntheticTransitionsDataSet [issueType=" + issueType + ", headers=" + headers + ", rows=" + rows + "]";
    }
}
