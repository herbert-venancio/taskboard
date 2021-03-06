package objective.taskboard.jira.endpoint;

import java.io.IOException;
import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;

import com.squareup.okhttp.Headers;

public abstract class AuthorizedJiraEndpoint {

    @Autowired
    private JiraEndpoint jiraEndpoint;

    public <S> S request(Class<S> service) {
        return jiraEndpoint.request(service, getUsername(), getPassword(), getHeaders());
    }

    public byte[] readBytesFromURL(URL url) throws IOException {
        return jiraEndpoint.readBytesFromURL(url, getUsername(), getPassword());
    }

    protected abstract String getUsername();

    protected abstract String getPassword();

    protected Headers getHeaders() {
        return new Headers.Builder().build();
    }

}
