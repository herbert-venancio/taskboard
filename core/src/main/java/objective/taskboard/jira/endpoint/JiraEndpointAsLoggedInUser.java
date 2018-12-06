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

import org.springframework.stereotype.Component;

import objective.taskboard.auth.CredentialsHolder;

@Component
public class JiraEndpointAsLoggedInUser extends AuthorizedJiraEndpoint {

    @Override
    protected String getUsername() {
        return CredentialsHolder.username();
    }

    @Override
    protected String getPassword() {
        return CredentialsHolder.password();
    }
}
