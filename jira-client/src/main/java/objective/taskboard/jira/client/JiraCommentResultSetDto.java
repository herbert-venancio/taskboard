package objective.taskboard.jira.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import retrofit.http.GET;
import retrofit.http.Path;

import java.util.LinkedList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown=true)
public class JiraCommentResultSetDto {
    public int maxResults;
    public int startAt;
    public int total;
    public List<JiraCommentDto> comments = new LinkedList<>();

    public interface Service {
        @GET("/rest/api/latest/issue/{key}/comment")
        public JiraCommentResultSetDto commentsForIssue(@Path("key") String issueKey);
    }
}
