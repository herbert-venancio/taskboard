package objective.taskboard.followup.kpi.properties;

import java.util.ArrayList;
import java.util.List;

public class CumulativeFlowDiagramProperties {

    private List<Long> excludeIssueTypes = new ArrayList<>();

    public List<Long> getExcludeIssueTypes() {
        return excludeIssueTypes;
    }

    public void setExcludeIssueTypes(List<Long> excludeIssueTypes) {
        this.excludeIssueTypes = excludeIssueTypes;
    }

}
