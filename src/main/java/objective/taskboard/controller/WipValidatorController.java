package objective.taskboard.controller;

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

import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.PRECONDITION_FAILED;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.QueryParam;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;

import objective.taskboard.data.Team;
import objective.taskboard.data.UserTeam;
import objective.taskboard.domain.ProjectTeam;
import objective.taskboard.domain.WipConfiguration;
import objective.taskboard.jira.JiraProperties;
import objective.taskboard.jira.JiraSearchService;
import objective.taskboard.jira.JiraService;
import objective.taskboard.repository.ProjectTeamRepository;
import objective.taskboard.repository.TeamCachedRepository;
import objective.taskboard.repository.UserTeamCachedRepository;
import objective.taskboard.repository.WipConfigurationRepository;

@RestController
@RequestMapping("/api/wip-validator")
public class WipValidatorController {

    private static final String CLASS_OF_SERVICE_EXPEDITE = "Expedite";

    @Autowired
    private WipConfigurationRepository wipConfigRepo;

    @Autowired
    private UserTeamCachedRepository userTeamRepo;

    @Autowired
    private TeamCachedRepository teamRepo;

    @Autowired
    private ProjectTeamRepository projectTeamRepo;

    @Autowired
    private JiraSearchService jiraSearchService;

    @Autowired
    private JiraService jiraService;

    @Autowired
    private JiraProperties jiraProperties;

    @RequestMapping
    public ResponseEntity<WipValidatorResponse> validate(@QueryParam("issue") String issue,
            @QueryParam("user") String user, @QueryParam("status") String status) {
        WipValidatorResponse response = new WipValidatorResponse();

        try {
            Issue i;
            try {
                i = jiraService.getIssueByKeyAsMaster(issue);
            } catch (Exception e) {
                response.message = "Issue " + issue + " not found (" + (e.getMessage() == null ? e.toString() : e.getMessage()) + ")";
                return new ResponseEntity<WipValidatorResponse>(response, PRECONDITION_FAILED);
            }

            if (i == null) {
                response.message = "Issue " + issue + " not found";
                return new ResponseEntity<WipValidatorResponse>(response, PRECONDITION_FAILED);
            }

            if (user == null || user.isEmpty()) {
                response.message = "Query parameter 'user' is required";
                return new ResponseEntity<WipValidatorResponse>(response, PRECONDITION_FAILED);
            }

            if (status == null || status.isEmpty()) {
                response.message = "Query parameter 'status' is required";
                return new ResponseEntity<WipValidatorResponse>(response, PRECONDITION_FAILED);
            }

            if (isClassOfServiceExpedite(i)) {
                response.message = "Class of service is " + CLASS_OF_SERVICE_EXPEDITE;
                return new ResponseEntity<WipValidatorResponse>(response, OK);
            }

            WipConfiguration wipConfig = getWipConfig(user, i.getProject().getKey(), status);
            if (wipConfig == null) {
                response.message = "No wip configuration was found";
                return new ResponseEntity<WipValidatorResponse>(response, OK);
            }

            List<String> userTeamsWip = userTeamRepo.findByTeam(wipConfig.getTeam()).stream()
                    .map(u -> u.getUserName())
                    .collect(toList());

            Team team = teamRepo.findByName(wipConfig.getTeam());
            List<String> projectTeams = projectTeamRepo.findByIdTeamId(team.getId()).stream()
                    .map(p -> p.getProjectKey())
                    .collect(toList());

            Integer wipActual = jiraSearchService
                    .searchIssues("assignee in ('" + String.join(",", userTeamsWip) + "') " +
                            "and project in ('" + String.join(",", projectTeams) + "') " +
                            "and status = '" + status + "' " +
                            (i.getIssueType().isSubtask() ?
                                    "and issuetype in subTaskIssueTypes()" :
                                    "and issuetype in standardIssueTypes()"))
                    .size();

            if (wipActual >= wipConfig.getWip()) {
                response.isWipExceeded = true;
                response.message = "You can't exceed your team's WIP limit ";
            }

            response.message += String.format("(Team: %s, Actual: %d, Limit: %d)", wipConfig.getTeam(),
                    wipActual, wipConfig.getWip());

            return new ResponseEntity<WipValidatorResponse>(response, OK);
        } catch (Exception e) {
            response.message = e.getMessage() == null ? e.toString() : e.getMessage();
            return new ResponseEntity<WipValidatorResponse>(response, INTERNAL_SERVER_ERROR);
        }
    }

    private boolean isClassOfServiceExpedite(Issue issue) {
        try {
            String classOfServiceId = jiraProperties.getCustomfield().getClassOfService().getId();
            IssueField fieldClassOfService = issue.getField(classOfServiceId);
            if (fieldClassOfService == null || fieldClassOfService.getValue() == null)
                return false;
            JSONObject json = (JSONObject) fieldClassOfService.getValue();
            return json.getString("value").equals(CLASS_OF_SERVICE_EXPEDITE);
        } catch (JSONException e) {
            return false;
        }
    }

    private WipConfiguration getWipConfig(String user, String project, String status) {
        List<WipConfiguration> wipConfigs = new ArrayList<WipConfiguration>();
        List<UserTeam> userTeams = userTeamRepo.findByUserName(user);
        userTeams.stream().forEach((userTeam) -> {
            Team team = teamRepo.findByName(userTeam.getTeam());
            if (team == null)
                return;

            List<ProjectTeam> projectTeams = projectTeamRepo.findByIdProjectKeyAndIdTeamId(project, team.getId());

            if (projectTeams.isEmpty())
                return;
            wipConfigs.addAll(wipConfigRepo.findByTeamAndStatus(team.getName(), status));
        });

        if (wipConfigs.isEmpty())
            return null;

        List<WipConfiguration> wipsSorted = wipConfigs.stream().sorted((w1, w2) -> {
            if (w1 == null && w2 == null) return 0;
            if (w1 == null) return 1;
            if (w2 == null) return -1;
            return w1.getWip().compareTo(w2.getWip());
        }).collect(toList());

        return wipsSorted.get(0);
    }

}
