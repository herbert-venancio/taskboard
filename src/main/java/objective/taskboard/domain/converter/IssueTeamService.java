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

import static objective.taskboard.domain.converter.IssueTeamService.TeamOrigin.DEFAULT_BY_ISSUE_TYPE;
import static objective.taskboard.domain.converter.IssueTeamService.TeamOrigin.DEFAULT_BY_PROJECT;
import static objective.taskboard.domain.converter.IssueTeamService.TeamOrigin.INHERITED;
import static objective.taskboard.domain.converter.IssueTeamService.TeamOrigin.SPECIFIC;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.data.Issue;
import objective.taskboard.data.Issue.CardTeam;
import objective.taskboard.data.Team;
import objective.taskboard.data.User;
import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.filterConfiguration.TeamFilterConfigurationService;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;
import objective.taskboard.repository.TeamCachedRepository;

@Service
public class IssueTeamService {
    private static final Logger log = LoggerFactory.getLogger(IssueTeamService.class);    

    @Autowired
    private TeamFilterConfigurationService teamFilterConfigurationService;

    @Autowired
    private TeamCachedRepository teamRepo;

    @Autowired
    private ProjectFilterConfigurationCachedRepository projectRepo;

    public Set<CardTeam> getTeamsForIds(List<Long> ids) {
        Set<CardTeam> issueTeams = new LinkedHashSet<>();
        for (Long teamId : ids) {
            Optional<Team> team = teamRepo.findById(teamId);
            if (!team.isPresent()) {
                log.warn("Invalid team id " + teamId + ". Team not found");
                continue;
            }
            issueTeams.add(CardTeam.from(team.get()));
        }
        return issueTeams;
    }

    public Long getDefaultTeamId(Issue issue) {
        return this.getDefaultTeam(issue).id;
    }

    public CardTeam getDefaultTeam(Issue issue) {
        Optional<ProjectFilterConfiguration> project = projectRepo.getProjectByKey(issue.getProjectKey());
        if (!project.isPresent())
            throw new IllegalStateException("Project not found for issue \"" + issue.getIssueKey() + "\".");

        Optional<Team> team = teamRepo.findById(project.get().getDefaultTeam());
        if (!team.isPresent())
            throw new IllegalStateException("Default team \""+ project.get().getDefaultTeam() +"\" for project \"" + issue.getProjectKey() + "\" not found.");

        return CardTeam.from(team.get());
    }

    public Optional<CardTeam> getCardTeamByIssueType(Issue issue) {
        Optional<ProjectFilterConfiguration> project = projectRepo.getProjectByKey(issue.getProjectKey());
        if (!project.isPresent())
            throw new IllegalStateException("Project not found for issue \"" + issue.getIssueKey() + "\".");

        Optional<Long> teamByIssueType = project.get().getTeamByIssueTypeId(issue.getType());
        if (!teamByIssueType.isPresent())
            return Optional.empty();

        Optional<Team> team = teamRepo.findById(teamByIssueType.get());
        if (!team.isPresent())
            throw new IllegalStateException("Default team \""+ teamByIssueType.get() +"\" by issue type \""+ issue.getType() +"\" for project \"" + issue.getProjectKey() + "\" not found.");

        return Optional.of(CardTeam.from(team.get()));
    }

    /**
     * Returns assigned users that are not in the issue teams 
     * @param issue
     * @return
     */
    public Set<String> getMismatchingUsers(Issue issue) {
        List<User> assignees = new LinkedList<>();
        assignees.addAll(issue.getAssignees());
        
        Set<String> validTeams = issue.getTeams().stream().map(t->t.name).collect(Collectors.toSet());
        Set<String> mismatches = new LinkedHashSet<>();

        for (User user : assignees) {
            List<Team> teams = teamFilterConfigurationService.getConfiguredTeamsByUser(user.name);
            
            if (!teams.stream().anyMatch(t -> validTeams.contains(t.getName())))
                mismatches.add(user.name);
        }

        return mismatches;
    }

    public Set<CardTeam> resolveTeams(Issue issue) {
        TeamOrigin teamsOrigin = resolveTeamsOrigin(issue);

        if (teamsOrigin == SPECIFIC)
            return getTeamsForIds(issue.getRawAssignedTeamsIds());

        Optional<CardTeam> teamByIssueType = getCardTeamByIssueType(issue);
        if (teamsOrigin == DEFAULT_BY_ISSUE_TYPE && teamByIssueType.isPresent())
            return Collections.singleton(teamByIssueType.get());

        Optional<Issue> parent = issue.getParentCard();
        if (teamsOrigin == INHERITED && parent.isPresent())
            return resolveTeams(parent.get());

        return Collections.singleton(getDefaultTeam(issue));
    }

    public TeamOrigin resolveTeamsOrigin(Issue issue) {
        Optional<Issue> parentCard = issue.getParentCard();

        if (!issue.getRawAssignedTeamsIds().isEmpty())
            return SPECIFIC;

        if (!hasParentWithSpecificOrigin(issue) && getCardTeamByIssueType(issue).isPresent())
            return DEFAULT_BY_ISSUE_TYPE;

        if (parentCard.isPresent() && resolveTeamsOrigin(parentCard.get()) != DEFAULT_BY_PROJECT)
            return INHERITED;

        return DEFAULT_BY_PROJECT;
    }

    private boolean hasParentWithSpecificOrigin(Issue issue) {
        Optional<Issue> parentCard = issue.getParentCard();
        if (!parentCard.isPresent())
            return false;
        if (resolveTeamsOrigin(parentCard.get()) == SPECIFIC)
            return true;
        return hasParentWithSpecificOrigin(parentCard.get());
    }

    public static enum TeamOrigin {
        SPECIFIC,
        DEFAULT_BY_ISSUE_TYPE,
        INHERITED,
        DEFAULT_BY_PROJECT;
    }

}