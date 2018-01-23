package objective.taskboard.jira.data;

import java.io.Serializable;
import retrofit.http.Body;
import retrofit.http.POST;

public class Version implements Serializable {

    private static final long serialVersionUID = -845658400746750858L;

    public String id;
    public String name;
    
    public Version(){}

    public Version(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public interface Service {
        @POST("/rest/api/latest/version")
        Version create(@Body Version.Request request);
    }

    public static class Request {

        public String project;
        public String name;
        
        public Request(){}

        public Request(String project, String name) {
            this.project = project;
            this.name = name;
        }
    }
}
