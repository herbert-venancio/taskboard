package objective.taskboard.jira.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown=true)
public class JiraCommentDto {

    public JiraUserDto author;
    public Date created;
    public String body;
}
