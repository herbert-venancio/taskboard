package objective.taskboard.jira.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

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
        public final Map<String, Object> transition;
        public final Map<String, Object> fields;

        public DoTransitionRequestBody(Long transitionId, String resolutionName) {
            this.transition = new HashMap<>();
            this.fields = new HashMap<>();
            setTransitionOnRequest(transitionId);
            if (StringUtils.isNotEmpty(resolutionName)) {
                setResolutionOnRequest(resolutionName);
            }
        }

        private void setTransitionOnRequest(Long transitionId) {
            this.transition.put("id", transitionId);
        }

        private void setResolutionOnRequest(String resolutionName) {
            Map<String, String> resolutionMap = new HashMap<>();
            resolutionMap.put("name", resolutionName);
            this.fields.put("resolution", resolutionMap);
        }
    }

}
