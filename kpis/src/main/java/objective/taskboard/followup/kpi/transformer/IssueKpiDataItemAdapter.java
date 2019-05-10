package objective.taskboard.followup.kpi.transformer;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

import objective.taskboard.followup.kpi.IssueTypeKpi;
import objective.taskboard.followup.kpi.KpiLevel;

public interface IssueKpiDataItemAdapter {

    Map<String,ZonedDateTime> getTransitions();

    String getIssueKey();

    Optional<IssueTypeKpi> getIssueType();

    KpiLevel getLevel();
    
    Optional<String> getCustomFieldValue(String customFieldId);
    
}
