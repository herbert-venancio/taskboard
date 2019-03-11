package objective.taskboard.data;

import java.net.URI;

import objective.taskboard.jira.client.JiraIssueTypeDto;

public class IssueTypeDto {
    public boolean isSizeRequired;
    public boolean visibleAtSubtaskCreation;
    public Long id;
    public URI iconUrl;
    public String name;
    public IssueTypeDto(JiraIssueTypeDto it, boolean isSizeRequired, boolean visibleAtSubtaskCreation) {
        this.isSizeRequired = isSizeRequired;
        this.visibleAtSubtaskCreation = visibleAtSubtaskCreation;
        this.id = it.getId();
        this.iconUrl = it.getIconUri();
        this.name = it.getName();
    }
}

