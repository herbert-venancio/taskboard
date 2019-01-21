package objective.taskboard.jira.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import objective.taskboard.jira.client.JiraIssueDto;
import objective.taskboard.jira.client.JiraUserDto;

public class WebHookBody {
    public Long timestamp;
    public WebhookEvent webhookEvent;
    @JsonProperty("issue_event_type_name")
    public IssueEventTypeName issueEventTypeName;
    public JiraUserDto user;
    public JiraIssueDto issue;
    public Version version;
    public Changelog changelog = new Changelog();
    public static class Changelog {
        public List<Map<String, Object>> items = new ArrayList<>();
    }
}
