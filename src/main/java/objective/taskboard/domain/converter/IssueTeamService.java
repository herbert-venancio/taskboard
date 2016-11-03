package objective.taskboard.domain.converter;

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

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atlassian.jira.rest.client.api.domain.User;

import objective.taskboard.data.Team;
import objective.taskboard.data.UserTeam;
import objective.taskboard.domain.converter.IssueMetadata.IssueCoAssignee;
import objective.taskboard.filterConfiguration.TeamFilterConfigurationService;
import objective.taskboard.repository.UserTeamCachedRepository;

@Service
public class IssueTeamService {

    @Autowired
    private UserTeamCachedRepository userTeamRepository;

    @Autowired
    private TeamFilterConfigurationService teamFilterConfigurationService;

    
    public List<String> getIssueTeams(IssueMetadata metadata, IssueMetadata parentMetadata) throws InvalidTeamException {
        return getTeamGroups(metadata, parentMetadata);
    }
    
    private List<String> getTeamGroups(IssueMetadata metadata, IssueMetadata parentMetadata) throws InvalidTeamException {
        List<UserTeam> users = getUsers(metadata, parentMetadata);
        return users.stream()
                .filter(Objects::nonNull)
                .map(u -> u.getTeam())
                .distinct()
                .collect(Collectors.toList());
    }
    
    private List<UserTeam> getUsers(IssueMetadata metadata, IssueMetadata parentMetadata) throws InvalidTeamException {
        List<UserTeam> usersTeam = getUsersResponsaveis(metadata);
        if (!usersTeam.isEmpty())
            return usersTeam;

        List<UserTeam> parentUsersTeam = getParentUsersResponsaveis(parentMetadata);
        if (!parentUsersTeam.isEmpty())
            return parentUsersTeam;

        User reporter = metadata.getIssue().getReporter();
        if (reporter == null)
            return newArrayList();
        
        return getUsersTeams(newArrayList(reporter.getName()));
    }
    
    private List<UserTeam> getUsersResponsaveis(IssueMetadata metadata) throws InvalidTeamException {
        List<String> users = newArrayList();
        
        User assignee = metadata.getIssue().getAssignee();
        if (assignee != null)
            users.add(assignee.getName());
        for (IssueCoAssignee coAssignee : metadata.getCoAssignees())
            users.add(coAssignee.getName());     

        users = users.stream()
                .distinct()
                .collect(Collectors.toList());

        return getUsersTeams(users);
    }

    private List<UserTeam> getUsersTeams(List<String> users) throws InvalidTeamException {
        List<UserTeam> usersTeams = users.stream()
                .map(u -> userTeamRepository.findByUserName(u))
                .filter(Objects::nonNull)
                .filter(uTeam -> isTeamVisible(uTeam.getTeam()))
                .collect(Collectors.toList());
        
        if (!users.isEmpty() && usersTeams.isEmpty())
            throw new InvalidTeamException(users);

        return usersTeams;        
    }

    private List<UserTeam> getParentUsersResponsaveis(IssueMetadata parentMetadata) throws InvalidTeamException {
        List<UserTeam> parentUsers = newArrayList();
        if (parentMetadata == null)
            return parentUsers;
        return getUsersResponsaveis(parentMetadata);
    }

    private boolean isTeamVisible(String team) {
        List<Team> visibleTeams = teamFilterConfigurationService.getVisibleTeams();
        return visibleTeams.stream()
                .filter(t -> Objects.equals(t.getName(), team))
                .findFirst()
                .isPresent();
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