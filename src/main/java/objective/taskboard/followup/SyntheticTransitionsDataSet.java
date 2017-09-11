package objective.taskboard.followup;

import java.util.List;

public class SyntheticTransitionsDataSet {
    public final List<String> headers;
    public final List<SyntheticTransitionsDataRow> rows;
    
    public SyntheticTransitionsDataSet(List<String> headers, List<SyntheticTransitionsDataRow> rows) {
        this.headers = headers;
        this.rows = rows;
    }
}
