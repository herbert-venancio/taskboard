package objective.taskboard.followup.kpi.transformer;

import java.time.ZonedDateTime;
import java.util.Map;

import objective.taskboard.data.Issue;
import objective.taskboard.followup.kpi.KpiLevel;

public class IssueDataItemAdapter implements IssueKpiDataItemAdapter {
    
    private Issue issue;
    private Map<String, ZonedDateTime> transitions;
    private KpiLevel level;

    IssueDataItemAdapter(Issue issue,KpiLevel level, Map<String,ZonedDateTime> transitions) {
        this.issue = issue;
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
    public String getIssueType() {
        return issue.getIssueTypeName();
    }

    @Override
    public KpiLevel getLevel() {
        return level;
    }

}
