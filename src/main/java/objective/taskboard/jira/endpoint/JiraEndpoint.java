package objective.taskboard.jira.endpoint;

/*-
 * [LICENSE]
 * Taskboard
 * ---
 * Copyright (C) 2015 - 2017 Objective Solutions
 * ---
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * [/LICENSE]
 */

import org.apache.commons.codec.binary.Base64;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.util.concurrent.Promise;

import objective.taskboard.jira.JiraClientFactory;
import objective.taskboard.jira.JiraProperties;
import objective.taskboard.jira.JiraServiceException;

@Component
public class JiraEndpoint {

    @Autowired 
    private JiraProperties jiraProperties;

    /**
     * @throws JiraServiceException
     * @throws RuntimeException
     */
    public <T> T executeRequest(String username, String password, Request<T> request) {
        JiraRestClient client = getClient(username, password);

        try {
            return request.execute(client).claim();

        } catch (RestClientException e) {
            throw new JiraServiceException(e);

        } finally {
            closeClient(client);
        }
    }

    private JiraRestClient getClient(String username, String password) {
        JiraClientFactory jiraClient = new JiraClientFactory();
        return jiraClient.getInstance(jiraProperties.getUrl(), username, password);
    }

    private void closeClient(JiraRestClient client) {
        try {
            client.close();
        } catch (Exception e) {}
    }

    public String postWithRestTemplate(String username, String password, String path, MediaType mediaType, JSONObject jsonRequest) {
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> request = new HttpEntity<>(jsonRequest.toString(), getAuthorizationRequestHeader(username, password, mediaType));
        String url = jiraProperties.getUrl() + path;
        return restTemplate.postForObject(url, request, String.class);
    }

    private HttpHeaders getAuthorizationRequestHeader(String username, String password, MediaType mediaType) {
        HttpHeaders headers = new HttpHeaders();
        String userAndPass = username + ":" + password;
        headers.add("Authorization", "Basic " + new String(Base64.encodeBase64((userAndPass).getBytes())));
        headers.setContentType(mediaType);
        return headers;
    }

    public interface Request<T> {
        Promise<T> execute(JiraRestClient client);
    }
}
