package objective.taskboard.jira.client;

import retrofit.http.GET;

public class JiraServerInfoDto {

    public interface Service {
        @GET("/rest/api/latest/serverInfo")
        JiraServerInfoDto get();
    }
}
