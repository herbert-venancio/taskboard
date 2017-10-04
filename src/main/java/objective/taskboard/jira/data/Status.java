package objective.taskboard.jira.data;

import java.util.List;

import retrofit.http.GET;

public class Status {

    public final Long id;
    public final String name;
    public final StatusCategory statusCategory;

    public Status(Long id, String name, StatusCategory statusCategory) {
        this.id = id;
        this.name = name;
        this.statusCategory = statusCategory;
    }

    public interface Service {
        @GET("/rest/api/latest/status")
        List<Status> all();
    }
}
