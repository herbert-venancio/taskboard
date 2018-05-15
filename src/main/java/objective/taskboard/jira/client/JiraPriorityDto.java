package objective.taskboard.jira.client;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import retrofit.http.GET;

@JsonIgnoreProperties(ignoreUnknown=true)
public class JiraPriorityDto {

    public interface Service {
        @GET("/rest/api/latest/priority")
        List<JiraPriorityDto> all();
    }

    private long id;

    public long getId() {
        return id;
    }
}
