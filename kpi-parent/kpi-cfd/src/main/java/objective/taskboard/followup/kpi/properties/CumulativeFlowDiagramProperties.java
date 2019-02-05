package objective.taskboard.followup.kpi.properties;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.validation.annotation.Validated;

public class CumulativeFlowDiagramProperties {

    private List<Long> excludeIssueTypes = new ArrayList<>();

    public List<Long> getExcludeIssueTypes() {
        return excludeIssueTypes;
    }

    public void setExcludeIssueTypes(List<Long> excludeIssueTypes) {
        this.excludeIssueTypes = excludeIssueTypes;
    }

}
