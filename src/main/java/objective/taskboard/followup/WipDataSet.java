package objective.taskboard.followup;

import java.util.Arrays;
import java.util.List;

public class WipDataSet extends TransitionDataSet<WipRow>{

    private static List<String> headers = Arrays.asList("Date","Type","Status","WIP");
    
    public WipDataSet(String issueType, List<WipRow> rows) {
        super(issueType, headers, rows);
    }

}
