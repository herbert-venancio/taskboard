package objective.taskboard.jira.data;

import java.util.List;

import retrofit.http.GET;
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
    }

}
