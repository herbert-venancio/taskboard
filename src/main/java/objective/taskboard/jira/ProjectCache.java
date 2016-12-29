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
import static java.util.stream.Collectors.toSet;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.google.common.collect.Lists;

import objective.taskboard.config.CacheConfiguration;
import objective.taskboard.config.LoggedInUserKeyGenerator;
import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;

@Component
class ProjectCache {

    @Autowired
    private ProjectFilterConfigurationCachedRepository projectFilterConfiguration;
    
    @Autowired
    private JiraEndpoint jiraEndpoint;

    @Cacheable(cacheNames=CacheConfiguration.PROJECTS, keyGenerator=LoggedInUserKeyGenerator.NAME)
    public Map<String, BasicProject> getProjects() {
        Set<String> configuredProjectsKeys = projectFilterConfiguration.getProjects()
                .stream()
                .map(ProjectFilterConfiguration::getProjectKey)
                .collect(toSet());

        return getProjectsVisibleToUser()
                .stream()
                .filter(p -> configuredProjectsKeys.contains(p.getKey()))
                .collect(toMap(BasicProject::getKey, p -> p));
    }

    private List<BasicProject> getProjectsVisibleToUser() {
        Iterable<BasicProject> projects = jiraEndpoint.executeRequest(client -> client.getProjectClient().getAllProjects());
        return Lists.newArrayList(projects);
    }
}
