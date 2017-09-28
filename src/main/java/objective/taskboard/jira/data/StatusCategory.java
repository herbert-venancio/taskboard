package objective.taskboard.jira.data;


import retrofit.http.GET;

import java.net.URI;
import java.util.List;

public class StatusCategory extends AddressableEntity {

    public final Long id;
    public final String key;
    public final String name;
    public final String colorName;

    public StatusCategory(URI self, Long id, String key, String name, String colorName) {
        super(self);
        this.id = id;
        this.key = key;
        this.name = name;
        this.colorName = colorName;
    }

    public interface Service {
        @GET("/rest/api/latest/statuscategory")
        List<StatusCategory> all();
    }
}
