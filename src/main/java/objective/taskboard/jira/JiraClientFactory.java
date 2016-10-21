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

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;

import java.net.URI;
import java.net.URISyntaxException;

import static com.google.common.base.Strings.isNullOrEmpty;

public class JiraClientFactory {

    private String jiraUrl;
    private String username;
    private String password;

    public JiraRestClient getInstance(String jiraUrl, String username, String password) {
        this.jiraUrl = jiraUrl;
        this.username = username;
        this.password = password;
        return createClient();
    }

    private void checkUsernameAndPassword(String username, String password) {
        if (isNullOrEmpty(username))
            throw new IllegalArgumentException("Jira user must be configured.");

        if (isNullOrEmpty(password))
            throw new IllegalArgumentException("Jira password must be configured.");
    }

    private JiraRestClient createClient() {
        checkUrl(jiraUrl);
        checkUsernameAndPassword(this.username, this.password);
        return getFactory().createWithBasicHttpAuthentication(getURI(jiraUrl), this.username, this.password);
    }

    private AsynchronousJiraRestClientFactory getFactory() {
        return new AsynchronousJiraRestClientFactory();
    }

    private void checkUrl(String jiraUrl) {
        if (isNullOrEmpty(jiraUrl))
            throw new IllegalArgumentException("Jira URL must be configured.");
        else
            getURI(jiraUrl);
    }

    private URI getURI(String jiraUrl) {
        try {
            return new URI(jiraUrl);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(String.format("Jira URL '%s' is not valid.", jiraUrl));
        }
    }

}