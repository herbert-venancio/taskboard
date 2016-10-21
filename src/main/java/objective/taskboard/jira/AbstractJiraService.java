package objective.taskboard.jira;

/*-
 * [LICENSE]
 * Taskboard
 * - - -
 * Copyright (C) 2015 - 2016 Objective Solutions
 * - - -
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

import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.util.concurrent.Promise;

import lombok.extern.slf4j.Slf4j;
import objective.taskboard.auth.CredentialsHolder;

@Slf4j
public class AbstractJiraService {
    
    @Autowired private JiraProperties jiraProperties;

    protected <T> T executeRequest(PromisedRequest<T> request) {       
        return executeWrappedRequest(
                client -> {
            try {
                return request.execute(client).get();
            } catch (InterruptedException | ExecutionException e) {
                log.error(e.getMessage(), e);
                Throwable cause = e.getCause();
                if (cause instanceof RestClientException)
                    throw new JiraServiceException((RestClientException)cause);                 
                throw new RuntimeException(e.getMessage(), e);
            }
        });
    }

    protected <T> T executeWrappedRequest(Request<T> request) {
        return executeWrappedRequest(CredentialsHolder.username(), CredentialsHolder.password(), request);
    }
    
    protected <T> T executeWrappedRequest(String username, String password, Request<T> request) {
        JiraRestClient client = getClientWithUsernameAndPassword(username, password);
        try {
            return request.execute(client);
        } finally {
            closeClient(client);
        }
    }
    
    private JiraRestClient getClientWithUsernameAndPassword(String username, String password) {
        JiraClientFactory jiraClient = new JiraClientFactory();
        return jiraClient.getInstance(jiraProperties.getUrl(), username, password);
    }
    
    private void closeClient(JiraRestClient client) {
        try {
            client.close();
        } catch (Exception e) {
            log.error("Could not close jira rest client", e);
        }
    }

    protected interface PromisedRequest<T> {
        Promise<T> execute(JiraRestClient client);
    }

    protected interface Request<T> {
        T execute(JiraRestClient client);
    }

}
