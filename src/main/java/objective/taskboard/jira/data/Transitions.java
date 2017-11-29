package objective.taskboard.jira.data;

import static objective.taskboard.jira.IssueFieldsUpdateSchema.makeUpdateSchema;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

public class Transitions {

    public transient String expand;
    public List<Transition> transitions;

    public Transitions(String expand, List<Transition> transitions) {
        this.expand = expand;
        this.transitions = transitions;
    }

    public interface Service {
        @GET("/rest/api/latest/issue/{issueKey}/transitions?expand=transitions.fields")
        Transitions get(@Path("issueKey") String issueKey);

        @POST("/rest/api/latest/issue/{issueKey}/transitions?expand=transitions.fields")
        Response doTransition(@Path("issueKey") String issueKey, @Body DoTransitionRequestBody transition);
    }

    public static class DoTransitionRequestBody {
        private final Map<String, Object> transition;
        private final Map<String, Object> update;

        public DoTransitionRequestBody(Long transitionId, Map<String, Object> fields) {
            this.transition = new HashMap<>();
            this.update = new HashMap<>();
            setTransitionOnRequest(transitionId);
            if (!fields.isEmpty())
                setFieldsOnTransitionSchema(fields);
        }

        private void setTransitionOnRequest(Long transitionId) {
            this.transition.put("id", transitionId);
        }

        private void setFieldsOnTransitionSchema(Map<String, Object> fields) {
            this.update.putAll(makeUpdateSchema(fields));
        }
    }

}
