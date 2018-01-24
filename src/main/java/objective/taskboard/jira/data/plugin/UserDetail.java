package objective.taskboard.jira.data.plugin;

import retrofit.http.GET;
import retrofit.http.Path;

import java.util.List;

public class UserDetail {

    public UserData userData;
    public List<Role> roles;

    public UserDetail() {}

    public UserDetail(UserData userData, List<Role> roles) {
        this.userData = userData;
        this.roles = roles;
    }

    public interface Service {
        @GET("/rest/projectbuilder/1.0/users/{user}")
        UserDetail get(@Path("user") String user);
    }

    public static class UserData {
        public String name;
        public String displayName;
        public String emailAddress;

        public UserData() {}

        public UserData(String name, String displayName, String emailAddress) {
            this.name = name;
            this.displayName = displayName;
            this.emailAddress = emailAddress;
        }
    }

    public static class Role {
        public Long id;
        public String name;
        public String projectKey;

        public Role() {}

        public Role(Long id, String name, String projectKey) {
            this.id = id;
            this.name = name;
            this.projectKey = projectKey;
        }
    }
}
