package objective.taskboard.jira.data;

import retrofit.http.GET;

import java.net.URI;
import java.util.List;

public class Status extends AddressableEntity {

    public final Long id;
    public final String name;
    public final String description;
    public final StatusCategory statusCategory;

    public Status(URI self, Long id, String name, String description, StatusCategory statusCategory) {
        super(self);
        this.id = id;
        this.name = name;
        this.description = description;
        this.statusCategory = statusCategory;
    }

    public interface Service {
        @GET("/rest/api/latest/status")
        List<Status> all();
    }
}
