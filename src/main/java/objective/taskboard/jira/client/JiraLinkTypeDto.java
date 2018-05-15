package objective.taskboard.jira.client;

import java.util.List;

import retrofit.http.GET;

public class JiraLinkTypeDto {
    public interface Service {
        @GET("/rest/api/latest/issueLinkType")
        Response all();
    }

    public String id;
    public String name;
    public String inward;
    public String outward;

    public static class Response {
        public List<JiraLinkTypeDto> issueLinkTypes;
    }
}
