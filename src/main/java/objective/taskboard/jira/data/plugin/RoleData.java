package objective.taskboard.jira.data.plugin;

import java.util.List;

import retrofit.http.GET;

public class RoleData {
    public String name;

    public RoleData() {}
    public RoleData(String name) {
        this.name = name;
    }

    public interface Service {
        @GET("/rest/projectbuilder/1.0/roles")
        List<RoleData> allVisible();
    }
}
