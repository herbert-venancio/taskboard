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
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.google.common.collect.Lists;

import objective.taskboard.auth.CredentialsHolder;
import objective.taskboard.config.CacheConfiguration;
import objective.taskboard.config.LoggedInUserKeyGenerator;
import objective.taskboard.data.Team;
import objective.taskboard.domain.Project;
import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.filterConfiguration.TeamFilterConfigurationService;
import objective.taskboard.jira.endpoint.JiraEndpointAsLoggedInUser;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;
import objective.taskboard.repository.UserTeamCachedRepository;

@Component
class ProjectCache {

    @Autowired
    private ProjectFilterConfigurationCachedRepository projectFilterConfiguration;

    @Autowired
    private JiraEndpointAsLoggedInUser jiraEndpointAsUser;

    @Autowired
    private UserTeamCachedRepository userTeamRepository;

    @Autowired
    private TeamFilterConfigurationService teamFilterConfigurationService;

    @Cacheable(cacheNames=CacheConfiguration.PROJECTS, keyGenerator=LoggedInUserKeyGenerator.NAME)
    public Map<String, Project> getVisibleProjects() {
        List<Long> teamsIdUser = getTeamsIdUser();

        Map<String, ProjectFilterConfiguration> configuredTeamProjects = projectFilterConfiguration.getProjects()
                .stream()
                .filter(pf -> pf.getTeamsIds().stream().anyMatch(id -> teamsIdUser.contains(id)))
                .collect(toMap(ProjectFilterConfiguration::getProjectKey, p -> p));

        return getProjectsVisibleToUserInJira()
                .stream()
                .filter(bp -> configuredTeamProjects.containsKey(bp.getKey()))
                .map(bp -> Project.from(bp, configuredTeamProjects.get(bp.getKey())))
                .collect(toMap(Project::getKey, p -> p));
    }

    private List<BasicProject> getProjectsVisibleToUserInJira() {
        Iterable<BasicProject> projects = jiraEndpointAsUser.executeRequest(client -> client.getProjectClient().getAllProjects());
        return Lists.newArrayList(projects);
    }

    private List<Long> getTeamsIdUser() {
        List<String> teamsUser = userTeamRepository.findByUserName(CredentialsHolder.username())
                .stream()
                .map(ut -> ut.getTeam())
                .collect(toList());

        return teamFilterConfigurationService.getConfiguredTeams()
                .stream()
                .filter(tf -> teamsUser.contains(tf.getName()))
                .map(Team::getId)
                .collect(toList());
    }
}
