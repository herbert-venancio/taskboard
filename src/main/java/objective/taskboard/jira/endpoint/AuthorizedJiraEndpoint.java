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

import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import objective.taskboard.jira.endpoint.JiraEndpoint.Request;

public abstract class AuthorizedJiraEndpoint {
    
    @Autowired
    private JiraEndpoint jiraEndpoint;

    public <T> T executeRequest(Request<T> request) {       
        return jiraEndpoint.executeRequest(getUsername(), getPassword(), request);
    }

    public String postWithRestTemplate(String path, MediaType mediaType, JSONObject jsonRequest) {
        return jiraEndpoint.postWithRestTemplate(getUsername(), getPassword(), path, mediaType, jsonRequest);
    }
    
    protected abstract String getUsername();
    
    protected abstract String getPassword();
}
