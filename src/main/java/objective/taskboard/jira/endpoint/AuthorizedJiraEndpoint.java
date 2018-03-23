package objective.taskboard.jira.endpoint;

import java.io.IOException;
import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;

import objective.taskboard.jira.endpoint.JiraEndpoint.Request;

public abstract class AuthorizedJiraEndpoint {
    
    @Autowired
    private JiraEndpoint jiraEndpoint;

    public <T> T executeRequest(Request<T> request) {
        return jiraEndpoint.executeRequest(getUsername(), getPassword(), request);
    }

    public <S> S request(Class<S> service) {
        return jiraEndpoint.request(service, getUsername(), getPassword());
    }

    public byte[] readBytesFromURL(URL url) throws IOException {
        return jiraEndpoint.readBytesFromURL(url, getUsername(), getPassword());
    }

    protected abstract String getUsername();
    
    protected abstract String getPassword();
}
