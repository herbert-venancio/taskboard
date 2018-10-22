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

package objective.taskboard.controller;

import static java.util.stream.Collectors.toList;
import static objective.taskboard.auth.authorizer.Permissions.PROJECT_DASHBOARD_OPERATIONAL;
import static objective.taskboard.auth.authorizer.Permissions.PROJECT_DASHBOARD_TACTICAL;

import java.util.List;

import javax.transaction.Transactional;

import objective.taskboard.domain.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

import objective.taskboard.auth.authorizer.Authorizer;
import objective.taskboard.controller.ProjectCreationData.ProjectCreationDataTeam;
import objective.taskboard.data.Team;
import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.domain.TeamFilterConfiguration;
import objective.taskboard.followup.FollowUpFacade;
import objective.taskboard.jira.ProjectService;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;
import objective.taskboard.repository.TeamCachedRepository;
import objective.taskboard.repository.TeamFilterConfigurationCachedRepository;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    ProjectFilterConfigurationCachedRepository projectRepository;

    @Autowired
    TeamCachedRepository teamRepository;

    @Autowired
    TeamFilterConfigurationCachedRepository teamFilterConfigurationRepository;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private FollowUpFacade followUpFacade;

    @Autowired
    private Authorizer authorizer;

    @GetMapping
    public List<ProjectData> getProjectsVisibleOnTaskboard() {
        return projectService.getNonArchivedJiraProjectsForUser().stream()
                .map(this::generateProjectData)
                .collect(toList());
    }

    @RequestMapping("/dashboard")
    public List<ProjectData> getProjectsVisibleOnDashboard() {
        return projectService.getTaskboardProjects(projectService::isNonArchivedAndUserHasAccess, PROJECT_DASHBOARD_TACTICAL, PROJECT_DASHBOARD_OPERATIONAL).stream()
                .map(this::generateProjectData)
                .collect(toList());
    }

    @RequestMapping(method = RequestMethod.PATCH, consumes="application/json")
    public ResponseEntity<String> updateProjectsTeams(@RequestBody ProjectData [] projectsTeams) {
        throw new UnsupportedOperationException("This endpoint has been discontinued. Change the default team directly in taskboard");
    }

    @RequestMapping(value="{projectKey}", method = RequestMethod.GET)
    public ResponseEntity<Void> get(@PathVariable("projectKey") String projectKey) {
        return projectService.jiraProjectExistsAndUserHasAccess(projectKey) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @RequestMapping(method = RequestMethod.POST, consumes="application/json")
    @Transactional
    public void create(@RequestBody ProjectCreationData data) {
        if (projectService.taskboardProjectExists(data.projectKey))
            return;

        if (data.defaultTeam == null)
            throw new IllegalArgumentException("Default team is mandatory");

        ProjectCreationDataTeam defaultTeam = data.defaultTeam;

        validateTeamDoesntExist(defaultTeam.name);

        TeamFilterConfiguration team = createTeamAndConfigurations(defaultTeam.name, data.teamLeader, data.teamLeader, defaultTeam.members);

        projectService.saveTaskboardProject(new ProjectFilterConfiguration(data.projectKey, team.getTeamId()));
    }

    private void validateTeamDoesntExist(String teamName) {
        if (teamRepository.exists(teamName))
            throw new IllegalArgumentException("Team '" + teamName + "' already exists.");
    }

    private TeamFilterConfiguration createTeamAndConfigurations(String teamName, String manager, String coach, List<String> members) {
        Team team = createTeam(teamName, manager, coach, members);
        return createTeamFilterConfiguration(team);
    }

    private Team createTeam(String name, String manager, String coach, List<String> members) {
        final Team team = new Team(name, manager, coach, members);
        return teamRepository.save(team);
    }

    private TeamFilterConfiguration createTeamFilterConfiguration(Team team) {
        final TeamFilterConfiguration teamFilterConfiguration = new TeamFilterConfiguration();
        teamFilterConfiguration.setTeamId(team.getId());
        return teamFilterConfigurationRepository.save(teamFilterConfiguration);
    }

    private ProjectData generateProjectData(Project project) {
        String projectDisplayName = project.getKey() + " - " + project.getName();
        return generateProjectData(project.getKey(), projectDisplayName);
    }

    private ProjectData generateProjectData(ProjectFilterConfiguration projectFilterConfiguration) {
        return generateProjectData(projectFilterConfiguration.getProjectKey(), projectFilterConfiguration.getProjectKey());
    }

    private ProjectData generateProjectData(String projectKey, String projectDisplayName) {
        ProjectData projectData = new ProjectData();
        projectData.projectKey = projectKey;
        projectData.projectDisplayName = projectDisplayName;
        projectData.followUpDataHistory = followUpFacade.getHistoryGivenProject(projectKey);
        projectData.roles = authorizer.getRolesForProject(projectKey);

        return projectData;
    }
}
