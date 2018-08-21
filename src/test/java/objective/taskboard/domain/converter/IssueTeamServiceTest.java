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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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

import com.google.common.collect.Sets;

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

    public static Set<Long> setOf(Long ...elements) {
        return Sets.newHashSet(elements);
    }

    public void defaultTeamTestSetup(Issue issue, ProjectFilterConfiguration project, Team team, Long issueTypeId, Long teamIdExpected) {
        when(issue.getProjectKey()).thenReturn("TEST");
        when(issue.getType()).thenReturn(issueTypeId);

        when(projectRepo.getProjectByKey(issue.getProjectKey())).thenReturn(Optional.of(project));
        when(project.getTeamByIssueTypeId(issue.getType())).thenReturn(Optional.of(teamIdExpected));
        when(project.getDefaultTeam()).thenReturn(teamIdExpected);

        when(team.getId()).thenReturn(teamIdExpected);
        when(team.getName()).thenReturn("TEST");
        when(teamRepo.findById(Mockito.eq(teamIdExpected))).thenReturn(Optional.of(team));
    }

}
