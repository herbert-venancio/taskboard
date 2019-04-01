package objective.taskboard.jira.data;

import java.util.ArrayList;
import java.util.List;

import objective.taskboard.jira.client.ChangelogItemDto;
import objective.taskboard.jira.client.JiraIssueDto;
import objective.taskboard.jira.client.JiraUserDto;

public class WebHookBody {

    public Long timestamp;
    public WebhookEvent webhookEvent;
    public JiraUserDto user;
    public JiraIssueDto issue;
    public Version version;
    public Changelog changelog = new Changelog();

    public static class Changelog {
        public List<ChangelogItemDto> items = new ArrayList<>();
    }
}
