package objective.taskboard.issue;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseStatus;

import objective.taskboard.data.SubtaskDto;
import objective.taskboard.jira.MetadataService;
import objective.taskboard.jira.client.JiraIssueTypeDto;
import objective.taskboard.jira.properties.JiraProperties;
import objective.taskboard.jira.properties.JiraProperties.IssueType.IssueTypeDetails;

@Service
public class SubtasksService {
    @Autowired
    private MetadataService metadataService;

    @Autowired 
    private JiraProperties jiraProperties;

    public void validateSubtasks(List<SubtaskDto> subtasks) throws SubtaskValidationException {
        Set<String> errors = new HashSet<String>();

        subtasks.forEach(subtask -> {
            boolean hasIssueType = true;

            if (isBlank(subtask.summary))
                errors.add("\"Summary\" is required.");

            else if (subtask.issuetype == null) {
                hasIssueType = false;
                errors.add("\"Issue Type\" is required.");
            }

            else if (!metadataService.issueTypeExistsByIdAsLoggedInUser(subtask.issuetype))
                errors.add("Issue Type \""+ subtask.issuetype +"\" doesn't exist.");

            else if (!jiraProperties.getCustomfield().getTShirtSize().getSizes().contains(subtask.tShirtSize))
                errors.add("Size \""+ subtask.tShirtSize +"\" doesn't exist.");

            if (hasIssueType) {
                JiraIssueTypeDto issueType = metadataService.getIssueTypeById(subtask.issuetype);
                boolean canBeUsedToCreateSubtask = issueTypeIsVisibibleAtSubtaskCreation(issueType);

                if (!canBeUsedToCreateSubtask)
                    errors.add("You can't create subtasks with issue type "+ issueType.getName() + ".");

                else if (isSizeRequired(issueType) && isBlank(subtask.tShirtSize))
                    errors.add("Issue Type "+ issueType.getName() +" must have a size.");
            }
        });

        if (!errors.isEmpty())
            throw new SubtaskValidationException(errors.stream().collect(Collectors.joining(" ")));
    }

    public boolean isSizeRequired(JiraIssueTypeDto issueType) {
        return jiraProperties.getIssuetype().getSubtasks().stream()
                .filter(itProperty -> itProperty.getId() == issueType.getId())
                .findFirst()
                .map(itProperty -> itProperty.isSizeRequired())
                .orElse(true);
    }

    public boolean issueTypeIsVisibibleAtSubtaskCreation(JiraIssueTypeDto issueType) {
        return jiraProperties.getIssuetype().getSubtasks().stream()
                .filter(itProperty -> itProperty.getId() == issueType.getId())
                .findFirst()
                .map(itProperty -> canToBeUsedOnCreation(issueType, itProperty))
                .orElse(false);
    }

    private boolean canToBeUsedOnCreation(JiraIssueTypeDto issueType, IssueTypeDetails itProperty) {
        return issueType.isSubtask() && itProperty.isVisibleAtSubtaskCreation();
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public static class SubtaskValidationException extends IllegalArgumentException {
        private static final long serialVersionUID = 1L;

        public SubtaskValidationException(String error) {
            super(error);
        }
    }


}
