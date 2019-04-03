package objective.taskboard.followup.kpi.transformer;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

import objective.taskboard.data.Issue;
import objective.taskboard.followup.kpi.IssueTypeKpi;
import objective.taskboard.followup.kpi.KpiLevel;

public class IssueDataItemAdapter implements IssueKpiDataItemAdapter {
    
    private Issue issue;
    private Map<String, ZonedDateTime> transitions;
    private KpiLevel level;
    private Optional<IssueTypeKpi> type;

    IssueDataItemAdapter(Issue issue,Optional<IssueTypeKpi> type, KpiLevel level, Map<String,ZonedDateTime> transitions) {
        this.issue = issue;
        this.type = type;
        this.level = level;
        this.transitions = transitions;
    }

    @Override
    public Map<String, ZonedDateTime> getTransitions() {
        return transitions;
    }

    @Override
    public String getIssueKey() {
        return issue.getIssueKey();
    }

    @Override
    public Optional<IssueTypeKpi> getIssueType() {
        return type;
    }

    @Override
    public KpiLevel getLevel() {
        return level;
    }

    @Override
    public Optional<String> getCustomFieldValue(String customFieldId) {
        return Optional.ofNullable(issue.getExtraFields().get(customFieldId));
    }

}
