package objective.taskboard.followup;

import static java.util.Arrays.asList;
import static objective.taskboard.followup.FromJiraDataRow.QUERY_TYPE_SUBTASK_PLAN;
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
    public static final String NEW_SCOPE = "new_scope";
    public static final String BUG = "Bug";

    @Autowired
    private JiraProperties jiraProperties;

    @Autowired
    private MetadataService metadataService;

    public boolean isIntangible(FromJiraDataRow row) {
        if (!isEmpty(row.subtaskClassOfService))
            return INTANGIBLE.equals(row.subtaskClassOfService);
        else if (!isEmpty(row.taskClassOfService))
            return INTANGIBLE.equals(row.taskClassOfService);
        else if (!isEmpty(row.demandClassOfService))
            return INTANGIBLE.equals(row.demandClassOfService);

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

        return isDone(row);
    }

    public boolean isBaselineBacklog(FromJiraDataRow row) {
        if (isIntangible(row) || isNewScope(row) || isRework(row) || isBaselineDone(row))
            return false;

        return true;
    }

    public boolean isDone(FromJiraDataRow row) {
        List<String> doneStatusesNames = getDoneStatusesNames();

        if (!isEmpty(row.subtaskStatus))
            return doneStatusesNames.contains(row.subtaskStatus);
        else if (!isEmpty(row.taskStatus))
            return doneStatusesNames.contains(row.taskStatus);
        else if (!isEmpty(row.demandStatus))
            return doneStatusesNames.contains(row.demandStatus);

        return false;
    }

    public boolean isBacklog(FromJiraDataRow row) {
        return !isDone(row);
    }

    public boolean isPlanned(FromJiraDataRow row) {
        return QUERY_TYPE_SUBTASK_PLAN.equals(row.queryType);
    }

    public boolean isBallpark(FromJiraDataRow row) {
        return !isPlanned(row);
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
