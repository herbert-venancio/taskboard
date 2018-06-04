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

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.atlassian.jira.rest.client.api.GetCreateIssueMetadataOptions;
import com.atlassian.jira.rest.client.api.GetCreateIssueMetadataOptionsBuilder;
import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.CimProject;

import objective.taskboard.config.CacheConfiguration;
import objective.taskboard.config.LoggedInUserKeyGenerator;
import objective.taskboard.jira.data.JiraProject;
import objective.taskboard.jira.endpoint.JiraEndpointAsLoggedInUser;
import objective.taskboard.jira.endpoint.JiraEndpointAsMaster;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;

@Component
class JiraProjectService {

    @Autowired
    private ProjectFilterConfigurationCachedRepository projectFilterConfiguration;

    @Autowired
    private JiraEndpointAsMaster jiraEndpointAsMaster;

    @Autowired
    private JiraEndpointAsLoggedInUser jiraEndpointAsUser;

    @Cacheable(cacheNames = CacheConfiguration.ALL_PROJECTS)
    public List<JiraProject> getAllProjects() {
        Set<String> configuredProjects = projectFilterConfiguration.getProjects().stream()
                .map(pfc -> pfc.getProjectKey())
                .collect(toSet());

        JiraProject.Service service = jiraEndpointAsMaster.request(JiraProject.Service.class);
        return service.all()
                .stream()
                .filter(project -> configuredProjects.contains(project.key))
                .map(project -> service.get(project.key))
                .collect(toList());
    }
    
    @Cacheable(cacheNames=CacheConfiguration.USER_PROJECTS, keyGenerator=LoggedInUserKeyGenerator.NAME)
    public List<String> getUserProjectKeys() {
        Iterable<BasicProject> visible = jiraEndpointAsUser.executeRequest(client -> client.getProjectClient().getAllProjects());
        List<String> visibleKeys = new LinkedList<>();
        for (BasicProject basicProject : visible) 
            visibleKeys.add(basicProject.getKey());
        
        return visibleKeys;
    }

    public Iterable<CimProject> getCreateIssueMetadata(String projectKey) {
        GetCreateIssueMetadataOptions options = new GetCreateIssueMetadataOptionsBuilder()
                .withExpandedIssueTypesFields()
                .withProjectKeys(projectKey)
                .build();

        return jiraEndpointAsUser.executeRequest(c -> c.getIssueClient().getCreateIssueMetadata(options));
    }
}
