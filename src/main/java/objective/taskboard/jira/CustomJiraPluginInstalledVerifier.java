package objective.taskboard.jira;

import objective.taskboard.jira.data.plugin.UserDetail;
import objective.taskboard.jira.endpoint.JiraEndpointAsMaster;
import objective.taskboard.utils.IOUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

import retrofit.RetrofitError;
import retrofit.client.Response;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class CustomJiraPluginInstalledVerifier {

    private static final Logger log = LoggerFactory.getLogger(CustomJiraPluginInstalledVerifier.class);

    @Autowired
    private JiraProperties properties;

    @Autowired
    private JiraEndpointAsMaster jiraEndpointAsMaster;

    @PostConstruct
    void checkCustomJiraPlugin() {
        try {
            jiraEndpointAsMaster.request(UserDetail.Service.class).get(properties.getLousa().getUsername());
        } catch (RetrofitError ex) {
            evaluateError(ex.getResponse());
            log.error("Failed to get response from jira", ex);
        }
    }

    private void evaluateError(Response response) {
        if (response == null)
            return;

        if (response.getStatus() != 404)
            return;

        boolean htmlResponse = response.getHeaders().stream()
                .filter(h -> HttpHeaders.CONTENT_TYPE.equalsIgnoreCase(h.getName()))
                .anyMatch(h -> h.getValue().contains("text/html"));
        if(htmlResponse)
            throw new CustomJiraPluginNotInstalledException();

        try (InputStream body = response.getBody().in()) {
            String content = IOUtilities.resourceAsString(body);
            if (content.contains("null for uri")) {
                throw new CustomJiraPluginNotInstalledException();
            }
        } catch (IOException ex) {
            log.error("Could not read response content", ex);
        }
    }

    public static class CustomJiraPluginNotInstalledException extends RuntimeException implements ExitCodeGenerator {

        private static final long serialVersionUID = 1349458331344286026L;

        public CustomJiraPluginNotInstalledException() {
            super("Taskboard Jira Plugin not installed");
        }

        @Override
        public int getExitCode() {
            return 1;
        }
    }
}
