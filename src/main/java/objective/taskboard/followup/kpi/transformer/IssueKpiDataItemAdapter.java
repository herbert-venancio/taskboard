package objective.taskboard.followup.kpi.transformer;

import java.time.ZonedDateTime;
import java.util.Map;

import objective.taskboard.followup.kpi.KpiLevel;

public interface IssueKpiDataItemAdapter {

    Map<String,ZonedDateTime> getTransitions();

    String getIssueKey();

    String getIssueType();

    KpiLevel getLevel();

}
