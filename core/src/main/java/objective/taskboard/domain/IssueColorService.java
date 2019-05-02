package objective.taskboard.domain;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.data.StepConfiguration;
import objective.taskboard.filter.LaneService;
import objective.taskboard.jira.MetadataService;
import objective.taskboard.jira.properties.JiraProperties;

@Service
public class IssueColorService {

    @Autowired
    private JiraProperties jiraProperties;

    @Autowired
    private LaneService laneService;

    @Autowired
    private MetadataService metadataService;

    public String getColor(Long classOfServiceId) {
        Map<Long, String> colors = jiraProperties.getCustomfield().getClassOfService().getColors();
        String color = colors == null ? null : colors.get(classOfServiceId);

        if (color != null)
            return color;

        return "#DDF9D9";
    }

    public String getStatusColor(long issueType, long status) {
        return laneService.getSteps(issueType, status).stream()
                .map(StepConfiguration::getColor)
                .findFirst()
                .orElse(null);
    }

    public String getStatusColor(Long issueTypeId, String statusName) {
        Long statusId = metadataService.getIdOfStatusByName(statusName);
        return getStatusColor(issueTypeId, statusId);
    }
}
