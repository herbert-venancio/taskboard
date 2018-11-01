package objective.taskboard.followup.kpi.transformer;

import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import objective.taskboard.followup.AnalyticsTransitionsDataRow;
import objective.taskboard.followup.kpi.IssueTypeKpi;
import objective.taskboard.followup.kpi.KpiLevel;

public class AnalyticDataRowAdapter implements IssueKpiDataItemAdapter {

    private AnalyticsTransitionsDataRow row;
    private List<String> statusHeader;
    private KpiLevel level;
    private Optional<IssueTypeKpi> type;

    public AnalyticDataRowAdapter(AnalyticsTransitionsDataRow row, Optional<IssueTypeKpi> type, List<String> statusHeader,KpiLevel level) {
        this.row = row;
        this.type = type;
        this.statusHeader = statusHeader;
        this.level = level;
    }

    @Override
    public Map<String, ZonedDateTime> getTransitions() {
        Map<String,ZonedDateTime> transitions = new LinkedHashMap<>();
        
        for (int i = 0; i < row.transitionsDates.size(); i++) {
            String status = statusHeader.get(i);
            ZonedDateTime dateTime = row.transitionsDates.get(i);
            transitions.put(status, dateTime);
        }
        return transitions;
    }

    @Override
    public String getIssueKey() {
        return row.issueKey;
    }

    @Override
    public Optional<IssueTypeKpi> getIssueType() {
        return type;
    }

    @Override
    public KpiLevel getLevel() {
        return level;
    }
        
}
