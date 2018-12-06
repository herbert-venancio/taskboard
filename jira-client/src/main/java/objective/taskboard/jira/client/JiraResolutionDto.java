package objective.taskboard.jira.client;

import java.util.List;

import retrofit.http.GET;

public class JiraResolutionDto {

    public interface Service {
        @GET("/rest/api/latest/resolution")
        List<JiraResolutionDto> all();
    }

    public String id;
    public String name;
}
