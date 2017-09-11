package objective.taskboard.followup;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class SyntheticTransitionsDataRow {
    public final Date date;
    public final List<Integer> amountOfIssueInStatus = new LinkedList<>();
    
    public SyntheticTransitionsDataRow(Date date) {
        this.date = date;
    }
}
