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

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + ((project == null) ? 0 : project.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Request other = (Request) obj;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            if (project == null) {
                if (other.project != null)
                    return false;
            } else if (!project.equals(other.project))
                return false;
            return true;
        }
    }
}
