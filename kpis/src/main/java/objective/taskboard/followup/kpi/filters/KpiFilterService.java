package objective.taskboard.followup.kpi.filters;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.jira.MetadataService;
import objective.taskboard.jira.properties.JiraProperties;

@Service
public class KpiFilterService {
    
    private MetadataService metadataService;
    
    private JiraProperties jiraProperties;

    @Autowired
    public KpiFilterService(MetadataService metadataService, JiraProperties jiraProperties) {
        this.metadataService = metadataService;
        this.jiraProperties = jiraProperties;
    }
    
    public KpiItemFilter getFilterStatusExcludedFromFollowup() {
        return new StatusExcludedFromFollowupFilter(getStatusesExcluded());
    }
    
    public KpiItemFilter getLevelFilter(KpiLevel level) {
        return new KpiLevelFilter(level);
    }

    private List<String> getStatusesExcluded() {
        return jiraProperties.getFollowup()
                    .getStatusExcludedFromFollowup()
                    .stream()
                    .map(metadataService::getStatusById)
                    .map(status -> status.name)
                    .collect(Collectors.toList());
    }

}
