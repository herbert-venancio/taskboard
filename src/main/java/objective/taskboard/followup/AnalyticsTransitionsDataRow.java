package objective.taskboard.followup;

import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class AnalyticsTransitionsDataRow implements TransitionDataRow {
    public final String issueKey;
    public final List<ZonedDateTime> transitionsDates;
    
    public AnalyticsTransitionsDataRow(String issueKey, List<ZonedDateTime> lastTransitionDate) {
        this.issueKey = issueKey;
        this.transitionsDates = lastTransitionDate;
    }

    @Override
    public List<String> getAsStringList() {
        List<String> list = new LinkedList<>();
        list.add(this.issueKey);
        list.addAll(this.transitionsDates.stream().map(
                date -> date == null ? "" : date.toString()
        ).collect(Collectors.toList()));
        return list;
    }
}