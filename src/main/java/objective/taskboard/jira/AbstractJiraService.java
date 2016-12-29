package objective.taskboard.jira;

/*-
 * [LICENSE]
 * Taskboard
 * ---
 * Copyright (C) 2015 - 2016 Objective Solutions
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

import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

public class AbstractJiraService {

    @Autowired 
    protected JiraEndpoint jiraEndpoint;

    protected <T> T executeRequest(JiraEndpoint.PromisedRequest<T> request) {       
        return jiraEndpoint.executeRequest(request);
    }

    protected <T> T executeWrappedRequest(JiraEndpoint.Request<T> request) {
        return jiraEndpoint.executeWrappedRequest(request);
    }

    protected <T> T executeWrappedRequest(String username, String password, JiraEndpoint.Request<T> request) {
        return jiraEndpoint.executeWrappedRequest(username, password, request);
    }

    protected String postWithRestTemplate(String path, MediaType mediaType, JSONObject jsonRequest) {
        return jiraEndpoint.postWithRestTemplate(path, mediaType, jsonRequest);
    }
}
