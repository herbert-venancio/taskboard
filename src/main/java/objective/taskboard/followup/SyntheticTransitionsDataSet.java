package objective.taskboard.followup;

import java.util.List;

public class SyntheticTransitionsDataSet extends TransitionDataSet<SyntheticTransitionsDataRow> {

    private static final int INITIAL_INDEX_STATUS_HEADER = 2;

    public SyntheticTransitionsDataSet(String issueType, List<String> headers, List<SyntheticTransitionsDataRow> rows) {
        super(issueType, headers, rows);
    }

    public int getInitialIndexStatusHeaders() {
        return INITIAL_INDEX_STATUS_HEADER;
    }
}
