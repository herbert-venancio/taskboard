package objective.taskboard.followup;

import java.util.Arrays;
import java.util.List;

public class ThroughputDataSet extends TransitionDataSet<ThroughputRow>{
    
    private static List<String> headers = Arrays.asList("Date","Type","Throughput");

    public ThroughputDataSet(String issueType, List<ThroughputRow> rows) {
        super(issueType, headers, rows);
    }

}
