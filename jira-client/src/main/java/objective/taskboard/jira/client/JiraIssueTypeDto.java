package objective.taskboard.jira.client;

import java.net.URI;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import retrofit.http.GET;

@JsonIgnoreProperties(ignoreUnknown=true)
public class JiraIssueTypeDto {

    public interface Service {

        @GET("/rest/api/latest/issuetype")
        List<JiraIssueTypeDto> all();
    }
    private Long id;
    @JsonProperty
    private URI iconUrl;
    private String name;
    private boolean subtask;

    public JiraIssueTypeDto() {}

    public JiraIssueTypeDto(Long id, String name, boolean subtask) {
        this(id, name, subtask, null);
    }

    public JiraIssueTypeDto(Long id, String name, boolean subtask, URI iconUrl) {
        this.id = id;
        this.name = name;
        this.subtask = subtask;
        this.iconUrl = iconUrl;
    }

    public Long getId() {
        return id;
    }

    public URI getIconUri() {
        return iconUrl;
    }
    
    public String getName() {
        return name;
    }

    public boolean isSubtask() {
        return subtask;
    }
}
