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

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.PRECONDITION_FAILED;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;

import com.google.common.collect.Sets;

import objective.taskboard.data.Issue;
import objective.taskboard.data.Team;
import objective.taskboard.data.UserTeam;
import objective.taskboard.domain.Filter;
import objective.taskboard.domain.Step;
import objective.taskboard.domain.WipConfiguration;
import objective.taskboard.issueBuffer.IssueBufferService;
import objective.taskboard.jira.JiraProperties;
import objective.taskboard.jira.JiraProperties.CustomField;
import objective.taskboard.jira.JiraProperties.CustomField.ClassOfServiceDetails;
import objective.taskboard.jira.JiraProperties.Wip;
import objective.taskboard.jira.MetadataCachedService;
import objective.taskboard.jira.data.Status;
import objective.taskboard.repository.TeamCachedRepository;
import objective.taskboard.repository.UserTeamCachedRepository;
import objective.taskboard.repository.WipConfigurationRepository;

@RunWith(MockitoJUnitRunner.class)
public class WipValidatorControllerTest {

    private static final String MSG_ASSERT_WIP_SHOULD_HAVE_EXCEEDED = "Wip should have exceeded";
    private static final String MSG_ASSERT_WIP_SHOULDN_T_HAVE_EXCEEDED = "Wip shouldn't have exceeded";
    private static final String MSG_ASSERT_RESPONSE_MESSAGE = "Response message";
    private static final String MSG_ASSERT_RESPONSE_STATUS_CODE = "Response status code";

    private static final String MSG_EXPECTED_WIP_EXCEEDED = "You can't exceed your team's WIP limit (Team: Team, Actual: 1, Limit: 1)";
    private static final String MSG_EXPECTED_NO_WIP = "No wip configuration was found";

    private static final Status STATUS = new Status(10L, "status", null);
    private static final Status STATUS_2 = new Status(11L, "status_2", null);
    private static final String PROJECT_KEY = "PROJECT";
    private static final String USER = "user";
    private static final String TEAM_NAME = "Team";
    private static final String TEAM2_NAME = "Team2";
    private static final String CLASS_OF_SERVICE_ID = "classOfServiceId";

    @InjectMocks
    private WipValidatorController subject;

    @Mock
    private Issue issue;
    @Mock
    private JiraProperties jiraProperties;
    @Mock
    private CustomField customField;
    @Mock
    private ClassOfServiceDetails classOfServiceDetails;
    @Mock
    private UserTeamCachedRepository userTeamRepo;
    @Mock
    private TeamCachedRepository teamRepo;
    @Mock
    private WipConfigurationRepository wipConfigRepo;
    
    @Mock
    private UserTeam userTeam;
    @Mock
    private UserTeam userTeam2;
    @Mock
    private Team team;
    @Mock
    private Team team2;
    @Mock
    private IssueBufferService cardService;

    @Mock
    private WipConfiguration wipConfig;
    @Mock
    private Step step;
    @Mock
    private MetadataCachedService metadataService;
    
    @Before
    public void before() throws JSONException {
        when(classOfServiceDetails.getId()).thenReturn(CLASS_OF_SERVICE_ID);
        when(customField.getClassOfService()).thenReturn(classOfServiceDetails);
        when(jiraProperties.getCustomfield()).thenReturn(customField);

        when(issue.getClassOfServiceValue()).thenReturn("Standard");
        when(issue.getType()).thenReturn(99L);
        when(issue.getProjectKey()).thenReturn(PROJECT_KEY);
        when(issue.getTeams()).thenReturn(Sets.newHashSet(new Issue.CardTeam(TEAM_NAME, 1L), new Issue.CardTeam(TEAM2_NAME, 2L)));

        when(cardService.getIssueByKey("I-1")).thenReturn(issue);
        
        Issue otherIssue = Mockito.mock(Issue.class);
        when(otherIssue.getType()).thenReturn(99L);
        when(otherIssue.getStatus()).thenReturn(10L);
        when(otherIssue.getTeams()).thenReturn(Sets.newHashSet(new Issue.CardTeam(TEAM_NAME, 1L), new Issue.CardTeam(TEAM2_NAME, 2L)));
        when(cardService.getAllIssues()).thenReturn(Arrays.asList(issue, otherIssue));

        when(userTeam.getUserName()).thenReturn(USER);
        when(userTeam.getTeam()).thenReturn(TEAM_NAME);
        when(userTeam2.getUserName()).thenReturn(USER);
        when(userTeam2.getTeam()).thenReturn(TEAM2_NAME);
        
        when(userTeamRepo.findByUserName(USER)).thenReturn(asList(userTeam));
        when(userTeamRepo.findByTeam(TEAM_NAME)).thenReturn(asList(userTeam));
        when(userTeamRepo.findByTeam(TEAM2_NAME)).thenReturn(asList(userTeam2));

        when(team.getId()).thenReturn(1L);
        when(team.getName()).thenReturn(TEAM_NAME);
        when(teamRepo.findByName(TEAM_NAME)).thenReturn(team);
        
        when(team2.getId()).thenReturn(2L);
        when(team2.getName()).thenReturn(TEAM2_NAME);
        when(teamRepo.findByName(TEAM2_NAME)).thenReturn(team2);

        Filter filter = new Filter();
        filter.setIssueTypeId(99L);
        filter.setStatusId(STATUS.id);
        filter.setStep(step);
        
        when(step.getFilters()).thenReturn(asList(filter));

        when(jiraProperties.getWip()).thenReturn(new Wip());
        when(wipConfig.getTeam()).thenReturn(TEAM_NAME);
        when(wipConfig.getWip()).thenReturn(1);
        when(wipConfig.isApplicable(99L, STATUS.id)).thenReturn(true);
        when(wipConfig.getStep()).thenReturn(step);
        when(wipConfigRepo.findByTeamIn(asList(TEAM_NAME, TEAM2_NAME))).thenReturn(asList(wipConfig));
        
        Map<Long, Status> statuses = new HashMap<>();
        statuses.put(STATUS.id, STATUS);
        statuses.put(STATUS_2.id, STATUS_2);
        when(metadataService.getStatusesMetadata()).thenReturn(statuses);
    }

    @Test
    public void whenWipExceeds_reponseOkWipExceedsFlagTrue() {
        ResponseEntity<WipValidatorResponse> responseEntity = subject.validate("I-1", STATUS.name);
        
        assertTrue(MSG_ASSERT_WIP_SHOULD_HAVE_EXCEEDED, responseEntity.getBody().isWipExceeded);
        assertEquals(MSG_ASSERT_RESPONSE_MESSAGE, MSG_EXPECTED_WIP_EXCEEDED, responseEntity.getBody().message);
        assertEquals(MSG_ASSERT_RESPONSE_STATUS_CODE, OK, responseEntity.getStatusCode());
    }

    @Test
    public void whenIssueNotFound_ResponseError() {
        when(cardService.getIssueByKey(anyString())).thenReturn(null);
        ResponseEntity<WipValidatorResponse> responseEntity = subject.validate("", "");
        assertFalse(MSG_ASSERT_WIP_SHOULDN_T_HAVE_EXCEEDED, responseEntity.getBody().isWipExceeded);
        assertEquals(MSG_ASSERT_RESPONSE_MESSAGE, "Issue  not found", responseEntity.getBody().message);
        assertEquals(MSG_ASSERT_RESPONSE_STATUS_CODE, PRECONDITION_FAILED, responseEntity.getStatusCode());
    }
    
    @Test
    public void whenWipNotConfigured_ResponseOkWipExceedsFlagFalse() {
        when(issue.getStatus()).thenReturn(1000L);
        
        ResponseEntity<WipValidatorResponse> responseEntity = subject.validate("I-1", STATUS_2.name);
        
        assertFalse(MSG_ASSERT_WIP_SHOULDN_T_HAVE_EXCEEDED, responseEntity.getBody().isWipExceeded);
        assertEquals(MSG_ASSERT_RESPONSE_MESSAGE, MSG_EXPECTED_NO_WIP, responseEntity.getBody().message);
        assertEquals(MSG_ASSERT_RESPONSE_STATUS_CODE, OK, responseEntity.getStatusCode());
    }

    @Test
    public void whenStatusEmpty_ResponseError() {
        when(cardService.getIssueByKey(anyString())).thenReturn(issue);
        ResponseEntity<WipValidatorResponse> responseEntity = subject.validate("", "");
        assertFalse(MSG_ASSERT_WIP_SHOULDN_T_HAVE_EXCEEDED, responseEntity.getBody().isWipExceeded);
        assertEquals(MSG_ASSERT_RESPONSE_MESSAGE, "Query parameter 'status' is required",
                responseEntity.getBody().message);
        assertEquals(MSG_ASSERT_RESPONSE_STATUS_CODE, PRECONDITION_FAILED, responseEntity.getStatusCode());
    }

    @Test
    public void whenStatusIsNull_ResponseError() {
        when(cardService.getIssueByKey(anyString())).thenReturn(issue);
        ResponseEntity<WipValidatorResponse> responseEntity = subject.validate("", null);
        assertFalse(MSG_ASSERT_WIP_SHOULDN_T_HAVE_EXCEEDED, responseEntity.getBody().isWipExceeded);
        assertEquals(MSG_ASSERT_RESPONSE_MESSAGE, "Query parameter 'status' is required",
                responseEntity.getBody().message);
        assertEquals(MSG_ASSERT_RESPONSE_STATUS_CODE, PRECONDITION_FAILED, responseEntity.getStatusCode());
    }

    @Test
    public void whenClassOfServiceExpedite_ResponseOkWipExceedsFlagFalse() throws JSONException {
        when(issue.getClassOfServiceValue()).thenReturn("Expedite");
        
        ResponseEntity<WipValidatorResponse> responseEntity = subject.validate("I-1", STATUS.name);
        
        assertFalse(MSG_ASSERT_WIP_SHOULD_HAVE_EXCEEDED, responseEntity.getBody().isWipExceeded);
        assertEquals(MSG_ASSERT_RESPONSE_STATUS_CODE, OK, responseEntity.getStatusCode());
        assertEquals(MSG_ASSERT_RESPONSE_MESSAGE, "Class of service is Expedite", responseEntity.getBody().message);
    }

    @Test
    public void whenClassOfServiceFieldNull_ResponseOkWipExceedsFlagTrue() {
    	when(issue.getClassOfServiceValue()).thenReturn(null);
        
        ResponseEntity<WipValidatorResponse> responseEntity = subject.validate("I-1", STATUS.name);
        
        assertEquals(MSG_ASSERT_RESPONSE_MESSAGE, MSG_EXPECTED_WIP_EXCEEDED, responseEntity.getBody().message);
        assertTrue(MSG_ASSERT_WIP_SHOULD_HAVE_EXCEEDED, responseEntity.getBody().isWipExceeded);
        assertEquals(MSG_ASSERT_RESPONSE_STATUS_CODE, OK, responseEntity.getStatusCode());
    }

    @Test
    public void whenExceptionHappens_ResponseErrorMessageContainsException() {
    	when(wipConfigRepo.findByTeamIn(asList(TEAM_NAME, TEAM2_NAME))).thenReturn(null);
        ResponseEntity<WipValidatorResponse> responseEntity = subject.validate("I-1", STATUS.name);
        assertFalse(MSG_ASSERT_WIP_SHOULDN_T_HAVE_EXCEEDED, responseEntity.getBody().isWipExceeded);
        assertEquals(MSG_ASSERT_RESPONSE_MESSAGE, "java.lang.NullPointerException", responseEntity.getBody().message);
        assertEquals(MSG_ASSERT_RESPONSE_STATUS_CODE, INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    }

    @Test
    public void whenWipNotExceeded_ReponseOkWipExceedsFlagFalse() {
        when(wipConfig.getWip()).thenReturn(2);

        ResponseEntity<WipValidatorResponse> responseEntity = subject.validate("I-1", STATUS.name);
        assertFalse(MSG_ASSERT_WIP_SHOULDN_T_HAVE_EXCEEDED, responseEntity.getBody().isWipExceeded);
        assertEquals(MSG_ASSERT_RESPONSE_MESSAGE, "(Team: Team, Actual: 1, Limit: 2)", responseEntity.getBody().message);
        assertEquals(MSG_ASSERT_RESPONSE_STATUS_CODE, OK, responseEntity.getStatusCode());
    }

    @Test
    public void whenIssueTypeIsIgnoredInProperty_ResponseOkWipExceedsFlagFalse() {
        Wip wip = new Wip();
        wip.setIgnoreIssuetypesIds(asList(99L));
        when(jiraProperties.getWip()).thenReturn(wip);

        ResponseEntity<WipValidatorResponse> responseEntity = subject.validate("I-1", STATUS.name);
        assertFalse(MSG_ASSERT_WIP_SHOULDN_T_HAVE_EXCEEDED, responseEntity.getBody().isWipExceeded);
    }

    @Test
    public void ignoreWipIfIssueHasTheSameIssueTypeFromProperty() {
        Wip wip = new Wip();
        wip.setIgnoreIssuetypesIds(asList(1L,2L));
        when(jiraProperties.getWip()).thenReturn(wip);
        when(issue.getType()).thenReturn(1L);
        when(issue.getIssueTypeName()).thenReturn("Feature Planning");

        ResponseEntity<WipValidatorResponse> responseEntity = subject.validate("I-1", STATUS.name);
        assertFalse(responseEntity.getBody().isWipExceeded);
        assertEquals("Issue Type Feature Planning is ignored on WIP count.", responseEntity.getBody().message);
        assertEquals(OK, responseEntity.getStatusCode());
    }
}
