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

import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.google.common.collect.Lists;

import objective.taskboard.config.CacheConfiguration;
import objective.taskboard.config.LoggedInUserKeyGenerator;
import objective.taskboard.domain.Project;
import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.jira.endpoint.JiraEndpointAsLoggedInUser;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;

@Component
class ProjectCache {

    @Autowired
    private ProjectFilterConfigurationCachedRepository projectFilterConfiguration;
    
    @Autowired
    private JiraEndpointAsLoggedInUser jiraEndpointAsUser;

    @Cacheable(cacheNames=CacheConfiguration.PROJECTS, keyGenerator=LoggedInUserKeyGenerator.NAME)
    public Map<String, Project> getVisibleProjects() {
        Map<String, ProjectFilterConfiguration> configuredProjects = projectFilterConfiguration.getProjects()
                .stream()
                .collect(toMap(ProjectFilterConfiguration::getProjectKey, p -> p));

        return getProjectsVisibleToUser()
                .stream()
                .filter(bp -> configuredProjects.containsKey(bp.getKey()))
                .map(bp -> Project.from(bp, configuredProjects.get(bp.getKey())))
                .collect(toMap(Project::getKey, p -> p));
    }

    private List<BasicProject> getProjectsVisibleToUser() {
        Iterable<BasicProject> projects = jiraEndpointAsUser.executeRequest(client -> client.getProjectClient().getAllProjects());
        return Lists.newArrayList(projects);
    }
}
