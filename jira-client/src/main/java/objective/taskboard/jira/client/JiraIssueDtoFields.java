package objective.taskboard.jira.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import objective.taskboard.jira.data.Version;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraIssueDtoFields {
    public JiraTimeTrackingDto timetracking;
    public JiraUserDto reporter;
    public List<JiraComponentDto> components = new ArrayList<>();
    public Set<String> labels = new HashSet<>();
    public List<JiraLinkDto> issuelinks = new ArrayList<>();
    public JiraPriorityDto priority;
    public String description;
    public List<JiraSubtaskDto> subtasks;
    public List<Version> fixVersions;

    @JsonDeserialize(using=JodaDateTimeDeserializer.class)
    public DateTime dueDate;

    public JiraStatusDto status;
    public String summary;
    public JiraIssueTypeDto issuetype;
    public JiraProjectDto project;

    @JsonDeserialize(using=JodaDateTimeDeserializer.class)
    public DateTime updated;

    @JsonDeserialize(using=JodaDateTimeDeserializer.class)
    public DateTime created;
    public JiraUserDto assignee;

    public JiraWorklogResultSetDto worklog;
    public JiraCommentResultSetDto comment;

    private Map<String, JiraIssueDto.JSONObjectAdapter> other = new HashMap<>();

    @JsonAnyGetter
    public Map<String, JiraIssueDto.JSONObjectAdapter> other() {
        return other;
    }

    @JsonAnySetter
    public void set(String name, JiraIssueDto.JSONObjectAdapter value) {
        other.put(name, value);
    }
}
