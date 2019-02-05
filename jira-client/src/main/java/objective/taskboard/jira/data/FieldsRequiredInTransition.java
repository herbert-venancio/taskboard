package objective.taskboard.jira.data;

import java.util.List;

import retrofit.http.Body;
import retrofit.http.POST;
import retrofit.http.Path;

public class FieldsRequiredInTransition {

    public Long id;
    public List<String> requiredFields;

    public FieldsRequiredInTransition(){}

    public FieldsRequiredInTransition(Long id, List<String> requiredFields) {
        this.id = id;
        this.requiredFields = requiredFields;
    }

    public interface Service {
        @POST("/rest/projectbuilder/1.0/issue/{issueKey}/fields-required-in-transitions")
        List<FieldsRequiredInTransition> post(@Path("issueKey") String issueKey, @Body List<Long> transitionIds);
    }

}
