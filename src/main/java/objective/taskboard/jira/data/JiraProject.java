package objective.taskboard.jira.data;

import java.net.URI;
import java.util.List;
import java.util.Map;

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

        @GET("/rest/api/latest/project/{project}/role")
        Map<String, URI> getRoles(@Path("project") String projectId);

        @GET("/rest/api/latest/project/{project}/role/{roleId}")
        JiraProject.Role getRole(@Path("project") String projectId, @Path("roleId") String roleId);
    }

    public static class Role {
        public final String name;
        public final String description;
        public final List<Actor> actors;

        public Role(String name, String description, List<Actor> actors) {
            this.name = name;
            this.description = description;
            this.actors = actors;
        }
    }

    public static class Actor {
        public final String name;
        public final String type;
        public final String displayName;

        public Actor(String name, String type, String displayName) {
            this.name = name;
            this.type = type;
            this.displayName = displayName;
        }
    }
}