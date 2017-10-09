package objective.taskboard.followup;

import objective.taskboard.utils.DateTimeUtils;

import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class SyntheticTransitionsDataRow implements TransitionDataRow {
    public final ZonedDateTime date;
    public final List<Integer> amountOfIssueInStatus;
    
    public SyntheticTransitionsDataRow(ZonedDateTime date, List<Integer> amountOfIssueInStatus) {
        this.date = date;
        this.amountOfIssueInStatus = amountOfIssueInStatus;
    }

    @Override
    public List<String> getAsStringList() {
        List<String> list = new LinkedList<>();
        list.add(DateTimeUtils.toStringExcelFormat(date));
        list.addAll(amountOfIssueInStatus.stream()
                .map(Object::toString)
                .collect(Collectors.toList()));
        return list;
    }

    @Override
    public List<Object> getAsObjectList() {
        List<Object> list = new LinkedList<>();
        list.add(date);
        list.addAll(amountOfIssueInStatus);
        return list;
    }
}
