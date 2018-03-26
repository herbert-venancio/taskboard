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
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static objective.taskboard.domain.converter.JiraIssueToIssueConverter.INVALID_TEAM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.data.Issue;
import objective.taskboard.filterConfiguration.TeamFilterConfigurationService;

@RunWith(MockitoJUnitRunner.class)
public class IssueTeamServiceTest {

    private static final String NAME_ASSIGNEE = "assignee";
    private static final String NAME_CO_ASSIGNEE = "co-assignee";
    private static final String NAME_PARENT_ASSIGNEE = "parent assignee";
    private static final String NAME_PARENT_CO_ASSIGNEE = "parent co-assignee";
    private static final String NAME_REPORTER = "reporter";
    private static final String NAME_PARENT_REPORTER = "parent reporter";

    private static final String MSG_USERS_SHOULD_BE_EMPTY = "Users should be empty";

    @InjectMocks
    private IssueTeamService subject;

    @Mock
    private TeamFilterConfigurationService teamFilterConfigurationService;

    private Issue issue = new Issue();
    private Issue parentCard = new Issue();

    @Before
    public void before() {
        when(teamFilterConfigurationService.getConfiguredTeamsNamesByUserAndProject(anyString(), anyString())).thenReturn(singletonList("Team"));
        issue.setIssueKey("FOO-34");
    }

    @Test
    public void issueWithValidAssignee() {
        given.issueHasAssignee();

        Map<String, List<String>> issueTeams = subject.getIssueTeams(issue);
        assertUserTeam(issueTeams, NAME_ASSIGNEE);
    }

    @Test
    public void issueWithValidCoAssignee() {
        given.issueHasCoAssignee();

        Map<String, List<String>> issueTeams = subject.getIssueTeams(issue);
        assertUserTeam(issueTeams, NAME_CO_ASSIGNEE);
    }

    @Test
    public void issueWithInvalidAssignee() {
        given.issueHasAssignee()
                .issueAssigneeHasNoTeam();

        Map<String, List<String>> issueTeams = subject.getIssueTeams(issue);
        assertThat(issueTeams).containsOnly(
                entry(NAME_ASSIGNEE, singletonList(INVALID_TEAM))
        );
    }

    @Test
    public void issueWithNoUserAndNoParent() {
        Map<String, List<String>> issueTeams = subject.getIssueTeams(issue);
        assertTrue(MSG_USERS_SHOULD_BE_EMPTY, issueTeams.isEmpty());
    }

    @Test
    public void parentIssueWithValidAssignee() {
        given.parentIssueHasAssignee();

        Map<String, List<String>> issueTeams = subject.getIssueTeams(issue);
        assertUserTeam(issueTeams, NAME_PARENT_ASSIGNEE);
    }

    @Test
    public void parentIssueWithValidCoAssignee() {
        given.parentIssueHasCoAssignee();

        Map<String, List<String>> issueTeams = subject.getIssueTeams(issue);
        assertUserTeam(issueTeams, NAME_PARENT_CO_ASSIGNEE);
    }

    @Test
    public void issueWithValidReporter() {
        given.issueHasReporter();
        issue.setParentCard(parentCard);

        Map<String, List<String>> issueTeams = subject.getIssueTeams(issue);
        assertUserTeam(issueTeams, NAME_REPORTER);
    }

    @Test
    public void issueWithReporterNull() {
        issue.setParentCard(parentCard);
        Map<String, List<String>> issueTeams = subject.getIssueTeams(issue);
        assertTrue(MSG_USERS_SHOULD_BE_EMPTY, issueTeams.isEmpty());
    }

    @Test
    public void givenIssueAndParentHasAllUsersFilled_usersContainsIssueAssigneeAndCoAssignee() {
        given.issueHasAssignee()
                .issueHasCoAssignee()
                .issueHasReporter()
                .parentIssueHasAssignee()
                .parentIssueHasCoAssignee()
                .parentIssueHasReporter();

        Map<String, List<String>> issueTeams = subject.getIssueTeams(issue);
        List<String> users = asList(subject.getUsersTeam(issue).split(","));
        Set<String> teams = subject.getTeams(issue);

        assertThat(issueTeams).containsOnly(
                entry(NAME_ASSIGNEE, singletonList("Team"))
                , entry(NAME_CO_ASSIGNEE, singletonList("Team"))
        );
        assertThat(users).containsOnly(NAME_ASSIGNEE, NAME_CO_ASSIGNEE);
        assertThat(teams).containsOnly("Team");
    }

    @Test
    public void givenIssueHasReporterAndParentHasAllUsersFilled_usersContainsParentAssigneeAndCoAssignee() {
        given.issueHasReporter()
                .parentIssueHasAssignee()
                .parentIssueHasCoAssignee()
                .parentIssueHasReporter();

        Map<String, List<String>> issueTeams = subject.getIssueTeams(issue);
        List<String> users = asList(subject.getUsersTeam(issue).split(","));
        Set<String> teams = subject.getTeams(issue);

        assertThat(issueTeams).containsOnly(
                entry(NAME_PARENT_ASSIGNEE, singletonList("Team"))
                , entry(NAME_PARENT_CO_ASSIGNEE, singletonList("Team"))
        );
        assertThat(users).containsOnly(NAME_PARENT_ASSIGNEE, NAME_PARENT_CO_ASSIGNEE);
        assertThat(teams).containsOnly("Team");
    }

    @Test
    public void givenIssueAndParentHasReporterFilled_usersContainsIssueReporter() {
        given.issueHasReporter()
                .parentIssueHasReporter();

        Map<String, List<String>> issueTeams = subject.getIssueTeams(issue);
        List<String> users = asList(subject.getUsersTeam(issue).split(","));
        Set<String> teams = subject.getTeams(issue);

        assertThat(issueTeams).containsOnly(
                entry(NAME_REPORTER, singletonList("Team"))
        );
        assertThat(users).containsOnly(NAME_REPORTER);
        assertThat(teams).containsOnly("Team");
    }

    @Test
    public void givenIssueHasReporterAndReporterHasNoTeam_issueTeamsContainsReporterWithoutTeams() {
        given.issueHasReporter()
                .issueReporterHasNoTeam();

        Map<String, List<String>> issueTeams = subject.getIssueTeams(issue);
        List<String> users = asList(subject.getUsersTeam(issue).split(","));
        Set<String> teams = subject.getTeams(issue);

        assertThat(issueTeams).containsOnly(
                entry(NAME_REPORTER, emptyList())
        );
        assertThat(users).containsOnly(NAME_REPORTER);
        assertThat(teams).isEmpty();
    }

    private void assertUserTeam(Map<String, List<String>> issueTeams, String nameCoAssignee) {
        assertTrue("User should have been found", issueTeams.containsKey(nameCoAssignee));

        List<String> teams = issueTeams.get(nameCoAssignee);
        assertEquals("User teams quantity", 1, teams.size());
        assertEquals("User team", "Team", teams.get(0));
    }

    private final FluentScenario given = new FluentScenario();
    private class FluentScenario {
        FluentScenario issueHasAssignee() {
            issue.setAssignee(NAME_ASSIGNEE);
            return this;
        }

        FluentScenario issueHasCoAssignee() {
            issue.setCoAssignees(singletonList(NAME_CO_ASSIGNEE));
            return this;
        }

        FluentScenario issueHasReporter() {
            issue.setReporter(NAME_REPORTER);
            return this;
        }

        FluentScenario parentIssueHasAssignee() {
            issue.setParentCard(parentCard);
            parentCard.setAssignee(NAME_PARENT_ASSIGNEE);
            return this;
        }

        FluentScenario parentIssueHasCoAssignee() {
            issue.setParentCard(parentCard);
            parentCard.setCoAssignees(singletonList(NAME_PARENT_CO_ASSIGNEE));
            return this;
        }

        FluentScenario parentIssueHasReporter() {
            issue.setParentCard(parentCard);
            parentCard.setReporter(NAME_PARENT_REPORTER);
            return this;
        }

        FluentScenario issueAssigneeHasNoTeam() {
            return userHasNoTeam(NAME_ASSIGNEE);
        }

        FluentScenario issueReporterHasNoTeam() {
            return userHasNoTeam(NAME_REPORTER);
        }

        FluentScenario userHasNoTeam(String name) {
            doReturn(emptyList())
                    .when(teamFilterConfigurationService).getConfiguredTeamsNamesByUserAndProject(eq(name), anyString());
            return this;
        }
    }
}
