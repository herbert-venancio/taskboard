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
package objective.taskboard.domain.converter;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.singletonList;
import static objective.taskboard.domain.converter.JiraIssueToIssueConverter.INVALID_TEAM;
import static org.springframework.util.StringUtils.isEmpty;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;

import objective.taskboard.data.Issue;
import objective.taskboard.filterConfiguration.TeamFilterConfigurationService;

@Service
public class IssueTeamService {

    @Autowired
    private TeamFilterConfigurationService teamFilterConfigurationService;

    public Set<String> getTeams(Issue issue) {
        Set<String> issueTeams = new LinkedHashSet<>();
        for (List<String> teams : getIssueTeams(issue).values())
            issueTeams.addAll(teams);

        return issueTeams;
    }

    public String getUsersTeam(Issue issue) {
        return String.join(",", getIssueTeams(issue).keySet());
    }

    Map<String, List<String>> getIssueTeams(Issue issue) {
        Map<String, List<String>> usersTeam = getIssueUsersTeams(issue);
        if (!usersTeam.isEmpty())
            return usersTeam;

        Optional<Issue> parent = issue.getParentCard();

        Map<String, List<String>> parentUsersTeam = parent.isPresent()? getIssueUsersTeams(parent.get()):newHashMap();
        if (!parentUsersTeam.isEmpty())
            return parentUsersTeam;

        String reporter = issue.getReporter();
        if (reporter == null)
            return newHashMap();

        return getUsersTeams(Sets.newHashSet(reporter), issue.getProjectKey(), false);
    }

    private Map<String, List<String>> getIssueUsersTeams(Issue issue) {
        Set<String> users = new LinkedHashSet<>();

        String assignee = issue.getAssignee();
        if (!isEmpty(assignee))
            users.add(assignee);
        for (IssueCoAssignee coAssignee : issue.getCoAssignees())
            users.add(coAssignee.getName());

        return getUsersTeams(users, issue.getProjectKey(), true);
    }

    private Map<String, List<String>> getUsersTeams(Set<String> users, String projectKey, boolean useInvalidTeam) {
        Map<String, List<String>> usersTeams = newHashMap();

        for (String user : users) {
            List<String> teams = teamFilterConfigurationService.getConfiguredTeamsNamesByUserAndProject(user, projectKey);

            if (teams.isEmpty() && useInvalidTeam)
                teams = singletonList(INVALID_TEAM);

            usersTeams.put(user, teams);
        }

        return usersTeams;
    }
}