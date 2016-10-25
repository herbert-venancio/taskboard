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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;

@Service
public class ProjectUsersVisibilityService {

    @Autowired
    private ProjectFilterConfigurationCachedRepository projectFilterConfiguration;
    
    @Autowired
    private JiraProperties jiraProperties;

    @Cacheable("projectUsers")
    public Map<String, List<String>> getProjectUsers() {
        Map<String, List<String>> projectUsers = new HashMap<>();
        projectFilterConfiguration.getProjects().stream()
                .forEach(t -> projectUsers.put(t.getProjectKey(), fetchUsersForProject(t.getProjectKey())));
        return projectUsers;
    }

    private List<String> fetchUsersForProject(String projectKey) {
        return new RestTemplate().exchange(
                        String.format("%s/rest/api/2/user/permission/search?permissions=BROWSE&projectKey=%s&username&maxResults=1000", jiraProperties.getUrl(), projectKey),
                        HttpMethod.GET, new HttpEntity<>(getAuthorizationRequestHeader()),
                        new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .getBody().stream()
                    .map(t -> t.get("name").toString())
                    .collect(Collectors.toList());
    }

    private HttpHeaders getAuthorizationRequestHeader() {
        HttpHeaders headers = new HttpHeaders();
        String userAndPass = jiraProperties.getLousa().getUsername() + ":" + jiraProperties.getLousa().getPassword();
        headers.add("Authorization", "Basic " + new String(Base64.encodeBase64((userAndPass).getBytes())));
        return headers;
    }
}
