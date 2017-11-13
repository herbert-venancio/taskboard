package objective.taskboard.jira.data;

import java.util.List;

import retrofit.http.GET;
import retrofit.http.Path;

public class JiraProject {

    public final String id;
    public final String key;
    public final List<Version> versions;
    public final String name;

    public JiraProject(String id, String key, List<Version> versions, String name) {
        this.id = id;
        this.key = key;
        this.versions = versions;
        this.name = name;
    }

    public interface Service {
        @GET("/rest/api/latest/project")
        List<JiraProject> all();

        @GET("/rest/api/latest/project/{id}")
        JiraProject get(@Path("id") String id);
    }
}