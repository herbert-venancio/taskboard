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
import static org.springframework.util.StringUtils.isEmpty;

import java.util.ArrayList;
import java.util.Collections;
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
        try {
            for (List<String> teams : getIssueTeams(issue).values())
                issueTeams.addAll(teams);
        } catch (InvalidTeamException e) {
            issueTeams.add(JiraIssueToIssueConverter.INVALID_TEAM);
        }
        
        return issueTeams;
    }
    
    public String getUsersTeam(Issue issue) {
        try {
            return String.join(",", getIssueTeams(issue).keySet());
        } catch (InvalidTeamException e) {
            return String.join(",", e.getUsersInInvalidTeam());
        }
    }

    public Map<String, List<String>> getIssueTeams(Issue issue) throws InvalidTeamException {
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

        return getUsersTeams(Sets.newHashSet(reporter), issue.getProjectKey());
    }

    private Map<String, List<String>> getIssueUsersTeams(Issue issue) throws InvalidTeamException {
        Set<String> users = new LinkedHashSet<>();

        String assignee = issue.getAssignee();
        if (!isEmpty(assignee))
            users.add(assignee);
        for (IssueCoAssignee coAssignee : issue.getCoAssignees())
            users.add(coAssignee.getName());

        return getUsersTeams(users, issue.getProjectKey());
    }

    private Map<String, List<String>> getUsersTeams(Set<String> users, String projectKey) throws InvalidTeamException {
        Map<String, List<String>> usersTeams = newHashMap();

        boolean foundSomeTeam = false;
        for (String user : users) {
            List<String> teams = teamFilterConfigurationService.getConfiguredTeamsNamesByUserAndProject(user, projectKey);

            if (!teams.isEmpty())
                foundSomeTeam = true;

            usersTeams.put(user, teams);
        }

        if (!users.isEmpty() && !foundSomeTeam)
            throw new InvalidTeamException(new ArrayList<String>(users));

        return usersTeams;
    }

    public static class InvalidTeamException extends Exception {
        private static final long serialVersionUID = 1L;

        private final List<String> usersInInvalidTeam;

        public InvalidTeamException(List<String> usersInInvalidTeam) {
            this.usersInInvalidTeam = Collections.unmodifiableList(usersInInvalidTeam);
        }

        public List<String> getUsersInInvalidTeam() {
            return usersInInvalidTeam;
        }
    }

}