package objective.taskboard.followup;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.jira.JiraProperties;
import objective.taskboard.jira.MetadataService;

@Service
public class FromJiraRowService {

    public static final String INTANGIBLE = "Intangible";
    public static final String NEW_SCOPE = "New Scope";
    public static final String BUG = "Bug";

    @Autowired
    private JiraProperties jiraProperties;

    @Autowired
    private MetadataService metadataService;

    public boolean isIntangible(FromJiraDataRow row) {
        if (!isEmpty(row.subtaskClassOfService))
            return row.subtaskClassOfService == INTANGIBLE;
        else if (!isEmpty(row.taskClassOfService))
            return row.taskClassOfService == INTANGIBLE;
        else if (!isEmpty(row.demandClassOfService))
            return row.demandClassOfService == INTANGIBLE;
        else
            return false;
    }

    public boolean isNewScope(FromJiraDataRow row) {
        if (isIntangible(row))
            return false;

        if (!isEmpty(row.subtaskLabels))
            return asList(row.subtaskLabels.split(",")).contains(NEW_SCOPE);
        else if (!isEmpty(row.taskLabels))
            return asList(row.taskLabels.split(",")).contains(NEW_SCOPE);
        else if (!isEmpty(row.demandLabels))
            return asList(row.demandLabels.split(",")).contains(NEW_SCOPE);
        else
            return false;
    }

    public boolean isRework(FromJiraDataRow row) {
        if (isIntangible(row) || isNewScope(row))
            return false;

        return !isEmpty(row.taskType) && BUG.equals(row.taskType);
    }

    public boolean isBaselineDone(FromJiraDataRow row) {
        if (isIntangible(row) || isNewScope(row) || isRework(row))
            return false;

        List<String> doneStatusesNames = getDoneStatusesNames();

        if (!isEmpty(row.subtaskStatus))
            return doneStatusesNames.contains(row.subtaskStatus);
        else if (!isEmpty(row.taskStatus))
            return doneStatusesNames.contains(row.taskStatus);
        else if (!isEmpty(row.demandStatus))
            return doneStatusesNames.contains(row.demandStatus);
        else 
            return false;
    }

    public boolean isBaselineBacklog(FromJiraDataRow row) {
        if (isIntangible(row) || isNewScope(row) || isRework(row) || isBaselineDone(row))
            return false;

        return true;
    }

    private List<String> getDoneStatusesNames() {
        List<String> completedStatusesNames = jiraProperties.getStatusesCompletedIds().stream()
                .map(id -> metadataService.getStatusById(id).name)
                .collect(Collectors.toList());
        List<String> canceledStatusesNames = jiraProperties.getStatusesCanceledIds().stream()
                .map(id -> metadataService.getStatusById(id).name)
                .collect(Collectors.toList());
        completedStatusesNames.addAll(canceledStatusesNames);
        return completedStatusesNames;
    }
}
