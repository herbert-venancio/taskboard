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

import static java.util.Arrays.asList;
import static objective.taskboard.domain.converter.IssueTeamService.TeamOrigin.DEFAULT_BY_ISSUE_TYPE;
import static objective.taskboard.domain.converter.IssueTeamService.TeamOrigin.DEFAULT_BY_PROJECT;
import static objective.taskboard.domain.converter.IssueTeamService.TeamOrigin.INHERITED;
import static objective.taskboard.domain.converter.IssueTeamService.TeamOrigin.SPECIFIC;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.tomcat.util.buf.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.data.Issue;
import objective.taskboard.data.Issue.CardTeam;
import objective.taskboard.data.Team;
import objective.taskboard.data.User;
import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.filterConfiguration.TeamFilterConfigurationService;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;
import objective.taskboard.repository.TeamCachedRepository;

@RunWith(MockitoJUnitRunner.class)
public class IssueTeamServiceTest {

    @InjectMocks
    private IssueTeamService subject;

    @Mock
    private TeamFilterConfigurationService teamFilterConfigurationService;

    @Mock
    private ProjectFilterConfigurationCachedRepository projectRepo;

    @Mock
    private TeamCachedRepository teamRepo;

    private Issue issue = new Issue();
    private Team team1337;
    private Team bravo7331;

    private static final long ISSUE_TYPE_WITH_TEAM = 1L;
    private static final long ISSUE_TYPE_WITHOUT_TEAM = 0L;

    @Before
    public void before() {
        issue.setIssueKey("FOO-34");
        issue.setProjectKey("FOO");
        ProjectFilterConfiguration mockProject = mock(ProjectFilterConfiguration.class);
        when(projectRepo.getProjectByKey("FOO")).thenReturn(Optional.of(mockProject));
        when(mockProject.getDefaultTeam()).thenReturn(1337L);

        team1337 = new Team();
        team1337.setName("L33t");
        when(teamRepo.findById(1337L)).thenReturn(Optional.of(team1337));

        bravo7331 = new Team();
        bravo7331.setName("bravo1337");
        when(teamRepo.findById(7331L)).thenReturn(Optional.of(bravo7331));
    }

    @Test
    public void getMismatchingUsers_whenThereAreUsersOutsideIssueTeam_shouldReturnTheirNames() {
        Issue i = mock(Issue.class);
        when(i.getRawAssignedTeamsIds()).thenReturn(asList(7331L));
        when(i.getTeams()).thenReturn(new HashSet<>(asList(Issue.CardTeam.from(bravo7331))));

        when(teamFilterConfigurationService.getConfiguredTeamsByUser("fulano")).thenReturn(asList(team1337));
        when(teamFilterConfigurationService.getConfiguredTeamsByUser("beltrano")).thenReturn(asList(bravo7331));
        when(i.getAssignees()).thenReturn(asList(new User("fulano"), new User("beltrano")));

        Set<String> mismatchingUsers = subject.getMismatchingUsers(i);

        assertEquals("fulano", StringUtils.join(mismatchingUsers));
    }

    @Test
    public void getDefaultTeamId_whenProjectAndTeamExists_shouldReturnTheDefaultTeamId() {
        Long issueTypeId = 1L;
        Long teamIdExpected = 10L;

        Issue issue = mock(Issue.class);
        ProjectFilterConfiguration project = mock(ProjectFilterConfiguration.class);
        Team team = mock(Team.class);

        defaultTeamTestSetup(issue, project, team, issueTypeId, teamIdExpected);

        Long defaultTeamId = subject.getDefaultTeamId(issue);

        assertEquals(teamIdExpected, defaultTeamId);
    }

    @Test
    public void getDefaultCardTeam_whenProjectAndTeamExists_shouldReturnTheDefaultCardTeam() {
        Long issueTypeId = 1L;
        Long teamIdExpected = 10L;

        Issue issue = mock(Issue.class);
        ProjectFilterConfiguration project = mock(ProjectFilterConfiguration.class);
        Team team = mock(Team.class);

        defaultTeamTestSetup(issue, project, team, issueTypeId, teamIdExpected);

        CardTeam cardTeam = subject.getDefaultTeam(issue);

        assertEquals(CardTeam.from(team), cardTeam);
        assertEquals(teamIdExpected, cardTeam.id);
    }

    @Test
    public void getCardTeamByIssueType_whenProjectAndTeamExists_shouldReturnTheCardTeamByIssueType() {
        Long issueTypeId = 1L;
        Long teamIdExpected = 10L;

        Issue issue = mock(Issue.class);
        ProjectFilterConfiguration project = mock(ProjectFilterConfiguration.class);
        Team team = mock(Team.class);

        defaultTeamTestSetup(issue, project, team, issueTypeId, teamIdExpected);

        Optional<CardTeam> cardTeamByIssueType = subject.getCardTeamByIssueType(issue);

        assertEquals(Optional.of(CardTeam.from(team)), cardTeamByIssueType);
        assertTrue(cardTeamByIssueType.isPresent());
        assertEquals(teamIdExpected, cardTeamByIssueType.get().id);
    }

    @Test
    public void resolveTeams_ifTeamsOriginIsSpecific_returnAssignedTeams() {
        long teamId1 = 1L;
        long teamId2 = 2L;
        teamSetup(teamId1);
        teamSetup(teamId2);

        DefaultByProjectFamily f = new DefaultByProjectFamily();

        when(f.issue.getRawAssignedTeamsIds()).thenReturn(asList(teamId1, teamId2));

        Set<CardTeam> response = subject.resolveTeams(f.issue);

        assertEquals(2, response.size());
        assertTrue(response.stream().anyMatch(t -> t.id == teamId1));
        assertTrue(response.stream().anyMatch(t -> t.id == teamId2));
    }

    @Test
    public void resolveTeams_ifTeamsOriginIsDefaultByIssueType_returnDefaultTeamByIssueType() {
        long teamId = 10L;

        issueTypeThatHasTeamSetup(ISSUE_TYPE_WITH_TEAM, teamId);

        DefaultByProjectFamily f = new DefaultByProjectFamily();

        when(f.issue.getType()).thenReturn(ISSUE_TYPE_WITH_TEAM);

        Set<CardTeam> response = subject.resolveTeams(f.issue);
        assertEquals(1, response.size());
        assertTrue(response.stream().anyMatch(t -> t.id == teamId));
    }

    @Test
    public void resolveTeams_ifTeamsOriginIsInherited_returnParentTeams() {
        long teamIdByIssueType = 10L;
        long teamId1 = 1L;
        long teamId2 = 2L;
        teamSetup(teamId1);
        teamSetup(teamId2);

        issueTypeThatHasTeamSetup(ISSUE_TYPE_WITH_TEAM, teamIdByIssueType);

        DefaultByProjectFamily f = new DefaultByProjectFamily();

        // GRANDPARENT WITH DEFAULT TEAM BY ISSUE_TYPE
        when(f.grandParent.getType()).thenReturn(ISSUE_TYPE_WITH_TEAM);

        Set<CardTeam> response = subject.resolveTeams(f.issue);
        assertEquals(1, response.size());
        assertTrue(response.stream().anyMatch(t -> t.id == teamIdByIssueType));

        // PARENT WITH SPECIFIC
        when(f.parent.getRawAssignedTeamsIds()).thenReturn(asList(teamId1, teamId2));

        response = subject.resolveTeams(f.issue);
        assertEquals(2, response.size());
        assertTrue(response.stream().anyMatch(t -> t.id == teamId1));
        assertTrue(response.stream().anyMatch(t -> t.id == teamId2));
    }

    @Test
    public void resolveTeams_ifTeamsOriginIsDefaultByProject_returnDefaultByProject() {
        long teamId = 1L;
        teamSetup(teamId);

        issueTypeThatHasTeamSetup(ISSUE_TYPE_WITH_TEAM);

        ProjectFilterConfiguration project = mock(ProjectFilterConfiguration.class);
        when(projectRepo.getProjectByKey(any())).thenReturn(Optional.of(project));
        when(project.getTeamByIssueTypeId(any())).thenReturn(Optional.empty());
        when(project.getDefaultTeam()).thenReturn(teamId);

        DefaultByProjectFamily f = new DefaultByProjectFamily();

        Set<CardTeam> response = subject.resolveTeams(f.issue);
        assertEquals(1, response.size());
        assertTrue(response.stream().anyMatch(t -> t.id == teamId));
    }

    @Test
    public void resolveTeamsOrigin_ifAllParentsAndIssueHaventTeams_shouldReturnDefaultByProject() {
        issueTypeThatHasTeamSetup(ISSUE_TYPE_WITH_TEAM);

        DefaultByProjectFamily f = new DefaultByProjectFamily();

        assertEquals(DEFAULT_BY_PROJECT, subject.resolveTeamsOrigin(f.issue));
    }

    @Test
    public void resolveTeamsOrigin_ifIssueHasntTeamButHasTeamByIssueType_andTheParentsHaventSpecificTeam_shouldReturnDefaultByIssueType() {
        issueTypeThatHasTeamSetup(ISSUE_TYPE_WITH_TEAM);

        DefaultByProjectFamily f = new DefaultByProjectFamily();

        // ISSUE WITH DEFAULT BY ISSUE TYPE
        when(f.issue.getType()).thenReturn(ISSUE_TYPE_WITH_TEAM);
        assertEquals(DEFAULT_BY_ISSUE_TYPE, subject.resolveTeamsOrigin(f.issue));

        // SOME PARENT WITH SPECIFIC
        when(f.greatGrandParent.getRawAssignedTeamsIds()).thenReturn(asList(1L, 2L, 3L));
        assertEquals(INHERITED, subject.resolveTeamsOrigin(f.issue));
    }

    @Test
    public void resolveTeamsOrigin_ifTheIssueHasAssignedTeams_shouldReturnSpecific() {
        DefaultByProjectFamily f = new DefaultByProjectFamily();

        when(f.issue.getRawAssignedTeamsIds()).thenReturn(asList(1L, 2L, 3L));

        assertEquals(SPECIFIC, subject.resolveTeamsOrigin(f.issue));
    }

    @Test
    public void resolveTeamsOrigin_ifTheIssueParentsHasSpecificTeamAndTheIssueHasntTeam_shouldReturnInherited() {
        DefaultByProjectFamily f = new DefaultByProjectFamily();

        when(f.parent.getRawAssignedTeamsIds()).thenReturn(asList(1L, 2L, 3L));

        assertEquals(INHERITED, subject.resolveTeamsOrigin(f.issue));
    }

    @Test
    public void resolveTeamsOrigin_ifTheIssueParentsHasTeamByIssueTypeAndTheIssueHasntTeam_shouldReturnInherited() {
        issueTypeThatHasTeamSetup(ISSUE_TYPE_WITH_TEAM);

        DefaultByProjectFamily f = new DefaultByProjectFamily();

        when(f.parent.getType()).thenReturn(ISSUE_TYPE_WITH_TEAM);

        assertEquals(INHERITED, subject.resolveTeamsOrigin(f.issue));
    }

    @Test
    public void resolveTeamsOrigin_ifAnyParentCardHasSpecificTeam_andTheIssueHasLessPriorityOrigin_shouldReturnInherited() {
        issueTypeThatHasTeamSetup(ISSUE_TYPE_WITH_TEAM);

        DefaultByProjectFamily f = new DefaultByProjectFamily();

        // SOME PARENT WITH SPECIFIC
        when(f.greatGrandParent.getRawAssignedTeamsIds()).thenReturn(asList(1L, 2L, 3L));

        // ISSUE WITH DEFAULT_BY_PROJECT
        when(f.issue.getType()).thenReturn(ISSUE_TYPE_WITHOUT_TEAM);
        assertEquals(INHERITED, subject.resolveTeamsOrigin(f.issue));

        // ISSUE WITH DEFAULT BY ISSUE TYPE
        when(f.issue.getType()).thenReturn(ISSUE_TYPE_WITH_TEAM);
        assertEquals(INHERITED, subject.resolveTeamsOrigin(f.issue));

        // ISSUE WITH SPECIFIC
        when(f.issue.getRawAssignedTeamsIds()).thenReturn(asList(1L, 2L, 3L));
        assertEquals(SPECIFIC, subject.resolveTeamsOrigin(f.issue));
    }

    private void defaultTeamTestSetup(Issue issue, ProjectFilterConfiguration project, Team team, Long issueTypeId, Long teamIdExpected) {
        when(issue.getProjectKey()).thenReturn("TEST");
        when(issue.getType()).thenReturn(issueTypeId);

        when(projectRepo.getProjectByKey(issue.getProjectKey())).thenReturn(Optional.of(project));
        when(project.getTeamByIssueTypeId(issue.getType())).thenReturn(Optional.of(teamIdExpected));
        when(project.getDefaultTeam()).thenReturn(teamIdExpected);

        when(team.getId()).thenReturn(teamIdExpected);
        when(team.getName()).thenReturn("TEST");
        when(teamRepo.findById(Mockito.eq(teamIdExpected))).thenReturn(Optional.of(team));
    }

    private void issueTypeThatHasTeamSetup(long issueTypeId) {
        issueTypeThatHasTeamSetup(issueTypeId, 10L);
    }

    private void issueTypeThatHasTeamSetup(long issueTypeId, long teamId) {
        ProjectFilterConfiguration project = mock(ProjectFilterConfiguration.class);
        when(project.getTeamByIssueTypeId(any())).thenReturn(Optional.empty());
        when(project.getTeamByIssueTypeId(eq(issueTypeId))).thenReturn(Optional.of(teamId));
        when(projectRepo.getProjectByKey(any())).thenReturn(Optional.of(project));

        teamSetup(teamId);
    }

    private void teamSetup(long teamId) {
        Team team = mock(Team.class);
        when(team.getId()).thenReturn(teamId);
        when(team.getName()).thenReturn("TEST " + teamId);
        when(teamRepo.findById(Mockito.eq(teamId))).thenReturn(Optional.of(team));
    }

    private static class DefaultByProjectFamily {
        public Issue greatGrandParent = mock(Issue.class);
        public Issue grandParent = mock(Issue.class);
        public Issue parent = mock(Issue.class);
        public Issue issue = mock(Issue.class);

        private DefaultByProjectFamily() {
            when(greatGrandParent.getParentCard()).thenReturn(Optional.empty());
            when(grandParent.getParentCard()).thenReturn(Optional.of(greatGrandParent));
            when(parent.getParentCard()).thenReturn(Optional.of(grandParent));
            when(issue.getParentCard()).thenReturn(Optional.of(parent));

            when(greatGrandParent.getRawAssignedTeamsIds()).thenReturn(asList());
            when(grandParent.getRawAssignedTeamsIds()).thenReturn(asList());
            when(parent.getRawAssignedTeamsIds()).thenReturn(asList());
            when(issue.getRawAssignedTeamsIds()).thenReturn(asList());

            when(greatGrandParent.getType()).thenReturn(ISSUE_TYPE_WITHOUT_TEAM);
            when(grandParent.getType()).thenReturn(ISSUE_TYPE_WITHOUT_TEAM);
            when(parent.getType()).thenReturn(ISSUE_TYPE_WITHOUT_TEAM);
            when(issue.getType()).thenReturn(ISSUE_TYPE_WITHOUT_TEAM);
        }
    }

}
