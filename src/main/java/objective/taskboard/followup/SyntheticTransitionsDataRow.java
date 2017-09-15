package objective.taskboard.followup;

import java.util.List;

import org.joda.time.DateTime;

public class SyntheticTransitionsDataRow {
    public final DateTime date;
    public final List<Integer> amountOfIssueInStatus;
    
    public SyntheticTransitionsDataRow(DateTime date, List<Integer> amountOfIssueInStatus) {
        this.date = date;
        this.amountOfIssueInStatus = amountOfIssueInStatus;
    }

    @Override
    public String toString() {
        return "SyntheticTransitionsDataRow [date=" + date + ", amountOfIssueInStatus=" + amountOfIssueInStatus + "]";
    }
}
