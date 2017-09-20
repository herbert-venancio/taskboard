package objective.taskboard.followup;

import java.util.List;

public class SyntheticTransitionsDataSet extends TransitionDataSet<SyntheticTransitionsDataRow> {

    public SyntheticTransitionsDataSet(String issueType, List<String> headers, List<SyntheticTransitionsDataRow> rows) {
        super(issueType, headers, rows);
    }
}
