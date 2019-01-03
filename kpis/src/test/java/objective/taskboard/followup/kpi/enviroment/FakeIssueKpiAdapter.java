package objective.taskboard.followup.kpi.enviroment;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

import objective.taskboard.followup.kpi.IssueTypeKpi;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.transformer.IssueKpiDataItemAdapter;

public class FakeIssueKpiAdapter implements IssueKpiDataItemAdapter {

    private Map<String,ZonedDateTime> transitions;
    private String issueKey;
    private Optional<IssueTypeKpi> issueType;
    private KpiLevel level;
    
    public FakeIssueKpiAdapter(Map<String, ZonedDateTime> transitions, String issueKey, Optional<IssueTypeKpi> issueType, KpiLevel level) {
        this.transitions = transitions;
        this.issueKey = issueKey;
        this.issueType = issueType;
        this.level = level;
    }

    @Override
    public Map<String, ZonedDateTime> getTransitions() {
        return transitions;
    }

    @Override
    public String getIssueKey() {
        return issueKey;
    }

    @Override
    public Optional<IssueTypeKpi> getIssueType() {
        return issueType;
    }

    @Override
    public KpiLevel getLevel() {
        return level;
    }
    
}
