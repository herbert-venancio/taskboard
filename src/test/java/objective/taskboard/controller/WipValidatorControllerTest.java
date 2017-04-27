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

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;

import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.IssueType;

import objective.taskboard.data.Team;
import objective.taskboard.data.UserTeam;
import objective.taskboard.domain.ProjectTeam;
import objective.taskboard.domain.WipConfiguration;
import objective.taskboard.jira.JiraProperties;
import objective.taskboard.jira.JiraProperties.CustomField;
import objective.taskboard.jira.JiraProperties.CustomField.ClassOfServiceDetails;
import objective.taskboard.jira.JiraSearchService;
import objective.taskboard.jira.JiraService;
import objective.taskboard.repository.ProjectTeamRepository;
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

    private static final String STATUS = "status";
    private static final String PROJECT_KEY = "PROJECT";
    private static final String USER = "user";
    private static final String TEAM_NAME = "Team";
    private static final String CLASS_OF_SERVICE_ID = "classOfServiceId";

    @InjectMocks
    private WipValidatorController subject;

    @Mock
    private JiraSearchService jiraSearchService;
    @Mock
    private JiraService jiraService;
    @Mock
    private Issue issue;
    @Mock
    private IssueField issueField;
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
    private ProjectTeamRepository projectTeamRepo;
    @Mock
    private WipConfigurationRepository wipConfigRepo;
    @Mock
    private BasicProject project;
    @Mock
    private IssueType issueType;
    @Mock
    private UserTeam userTeam;
    @Mock
    private Team team;
    @Mock
    private ProjectTeam projectTeam;
    @Mock
    private WipConfiguration wipConfig;
    @Mock
    private WipConfiguration wipConfig2;

    @Before
    public void before() throws JSONException {
        when(classOfServiceDetails.getId()).thenReturn(CLASS_OF_SERVICE_ID);
        when(customField.getClassOfService()).thenReturn(classOfServiceDetails);
        when(jiraProperties.getCustomfield()).thenReturn(customField);

        when(issueField.getValue()).thenReturn(new JSONObject("{value:Standard}"));
        when(issue.getField(CLASS_OF_SERVICE_ID)).thenReturn(issueField);

        when(project.getKey()).thenReturn(PROJECT_KEY);
        when(issue.getProject()).thenReturn(project);

        when(issueType.isSubtask()).thenReturn(true);
        when(issue.getIssueType()).thenReturn(issueType);

        when(jiraService.getIssueByKeyAsMaster(anyString())).thenReturn(issue);

        when(userTeam.getUserName()).thenReturn(USER);
        when(userTeam.getTeam()).thenReturn(TEAM_NAME);
        when(userTeamRepo.findByUserName(USER)).thenReturn(asList(userTeam));
        when(userTeamRepo.findByTeam(TEAM_NAME)).thenReturn(asList(userTeam));

        when(team.getId()).thenReturn(1L);
        when(team.getName()).thenReturn(TEAM_NAME);
        when(teamRepo.findByName(TEAM_NAME)).thenReturn(team);

        when(projectTeam.getProjectKey()).thenReturn(PROJECT_KEY);
        when(projectTeamRepo.findByIdProjectKeyAndIdTeamId(PROJECT_KEY, 1L)).thenReturn(asList(projectTeam));
        when(projectTeamRepo.findByIdTeamId(1L)).thenReturn(asList(projectTeam));

        when(wipConfig.getTeam()).thenReturn(TEAM_NAME);
        when(wipConfig.getWip()).thenReturn(1);
        when(wipConfigRepo.findByTeamAndStatus(TEAM_NAME, STATUS)).thenReturn(asList(wipConfig));

        when(jiraSearchService.searchIssues(anyString())).thenReturn(asList(issue));
    }

    @Test
    public void wipExceeded() {
        ResponseEntity<WipValidatorResponse> responseEntity = subject.validate("I-1", USER, STATUS);
        assertTrue(MSG_ASSERT_WIP_SHOULD_HAVE_EXCEEDED, responseEntity.getBody().isWipExceeded);
        assertEquals(MSG_ASSERT_RESPONSE_MESSAGE, MSG_EXPECTED_WIP_EXCEEDED, responseEntity.getBody().message);
        assertEquals(MSG_ASSERT_RESPONSE_STATUS_CODE, OK, responseEntity.getStatusCode());
    }

    @Test
    public void issueNotFound() {
        when(jiraService.getIssueByKeyAsMaster(anyString())).thenReturn(null);
        ResponseEntity<WipValidatorResponse> responseEntity = subject.validate("", "", "");
        assertFalse(MSG_ASSERT_WIP_SHOULDN_T_HAVE_EXCEEDED, responseEntity.getBody().isWipExceeded);
        assertEquals(MSG_ASSERT_RESPONSE_MESSAGE, "Issue  not found", responseEntity.getBody().message);
        assertEquals(MSG_ASSERT_RESPONSE_STATUS_CODE, PRECONDITION_FAILED, responseEntity.getStatusCode());
    }

    @Test
    public void issueNotFoundThrowedByException() {
        when(jiraService.getIssueByKeyAsMaster(anyString())).thenThrow(new RuntimeException("Issue Does Not Exist"));
        ResponseEntity<WipValidatorResponse> responseEntity = subject.validate("", "", "");
        assertFalse(MSG_ASSERT_WIP_SHOULDN_T_HAVE_EXCEEDED, responseEntity.getBody().isWipExceeded);
        assertEquals(MSG_ASSERT_RESPONSE_MESSAGE, "Issue  not found (Issue Does Not Exist)",
                responseEntity.getBody().message);
        assertEquals(MSG_ASSERT_RESPONSE_STATUS_CODE, PRECONDITION_FAILED, responseEntity.getStatusCode());
    }

    @Test
    public void userEmptyRequired() {
        when(jiraService.getIssueByKeyAsMaster(anyString())).thenReturn(issue);
        ResponseEntity<WipValidatorResponse> responseEntity = subject.validate("", "", "");
        assertFalse(MSG_ASSERT_WIP_SHOULDN_T_HAVE_EXCEEDED, responseEntity.getBody().isWipExceeded);
        assertEquals(MSG_ASSERT_RESPONSE_MESSAGE, "Query parameter 'user' is required",
                responseEntity.getBody().message);
        assertEquals(MSG_ASSERT_RESPONSE_STATUS_CODE, PRECONDITION_FAILED, responseEntity.getStatusCode());
    }

    @Test
    public void userNullRequired() {
        when(jiraService.getIssueByKeyAsMaster(anyString())).thenReturn(issue);
        ResponseEntity<WipValidatorResponse> responseEntity = subject.validate("", null, "");
        assertFalse(MSG_ASSERT_WIP_SHOULDN_T_HAVE_EXCEEDED, responseEntity.getBody().isWipExceeded);
        assertEquals(MSG_ASSERT_RESPONSE_MESSAGE, "Query parameter 'user' is required",
                responseEntity.getBody().message);
        assertEquals(MSG_ASSERT_RESPONSE_STATUS_CODE, PRECONDITION_FAILED, responseEntity.getStatusCode());
    }

    @Test
    public void statusEmptyRequired() {
        when(jiraService.getIssueByKeyAsMaster(anyString())).thenReturn(issue);
        ResponseEntity<WipValidatorResponse> responseEntity = subject.validate("", USER, "");
        assertFalse(MSG_ASSERT_WIP_SHOULDN_T_HAVE_EXCEEDED, responseEntity.getBody().isWipExceeded);
        assertEquals(MSG_ASSERT_RESPONSE_MESSAGE, "Query parameter 'status' is required",
                responseEntity.getBody().message);
        assertEquals(MSG_ASSERT_RESPONSE_STATUS_CODE, PRECONDITION_FAILED, responseEntity.getStatusCode());
    }

    @Test
    public void statusNullRequired() {
        when(jiraService.getIssueByKeyAsMaster(anyString())).thenReturn(issue);
        ResponseEntity<WipValidatorResponse> responseEntity = subject.validate("", USER, null);
        assertFalse(MSG_ASSERT_WIP_SHOULDN_T_HAVE_EXCEEDED, responseEntity.getBody().isWipExceeded);
        assertEquals(MSG_ASSERT_RESPONSE_MESSAGE, "Query parameter 'status' is required",
                responseEntity.getBody().message);
        assertEquals(MSG_ASSERT_RESPONSE_STATUS_CODE, PRECONDITION_FAILED, responseEntity.getStatusCode());
    }

    @Test
    public void classOfServiceExpedite() throws JSONException {
        when(issueField.getValue()).thenReturn(new JSONObject("{value:Expedite}"));
        ResponseEntity<WipValidatorResponse> responseEntity = subject.validate("", USER, STATUS);
        assertFalse(MSG_ASSERT_WIP_SHOULDN_T_HAVE_EXCEEDED, responseEntity.getBody().isWipExceeded);
        assertEquals(MSG_ASSERT_RESPONSE_MESSAGE, "Class of service is Expedite", responseEntity.getBody().message);
        assertEquals(MSG_ASSERT_RESPONSE_STATUS_CODE, OK, responseEntity.getStatusCode());
    }

    @Test
    public void noUserTeam() {
        when(userTeamRepo.findByUserName(USER)).thenReturn(asList());
        ResponseEntity<WipValidatorResponse> responseEntity = subject.validate("", USER, STATUS);
        assertFalse(MSG_ASSERT_WIP_SHOULDN_T_HAVE_EXCEEDED, responseEntity.getBody().isWipExceeded);
        assertEquals(MSG_ASSERT_RESPONSE_MESSAGE, MSG_EXPECTED_NO_WIP, responseEntity.getBody().message);
        assertEquals(MSG_ASSERT_RESPONSE_STATUS_CODE, OK, responseEntity.getStatusCode());
    }

    @Test
    public void noTeam() {
        when(teamRepo.findByName(TEAM_NAME)).thenReturn(null);
        ResponseEntity<WipValidatorResponse> responseEntity = subject.validate("", USER, STATUS);
        assertFalse(MSG_ASSERT_WIP_SHOULDN_T_HAVE_EXCEEDED, responseEntity.getBody().isWipExceeded);
        assertEquals(MSG_ASSERT_RESPONSE_MESSAGE, MSG_EXPECTED_NO_WIP, responseEntity.getBody().message);
        assertEquals(MSG_ASSERT_RESPONSE_STATUS_CODE, OK, responseEntity.getStatusCode());
    }

    @Test
    public void noProjectTeam() {
        when(projectTeamRepo.findByIdProjectKeyAndIdTeamId(PROJECT_KEY, 1L)).thenReturn(asList());
        ResponseEntity<WipValidatorResponse> responseEntity = subject.validate("", USER, STATUS);
        assertFalse(MSG_ASSERT_WIP_SHOULDN_T_HAVE_EXCEEDED, responseEntity.getBody().isWipExceeded);
        assertEquals(MSG_ASSERT_RESPONSE_MESSAGE, MSG_EXPECTED_NO_WIP, responseEntity.getBody().message);
        assertEquals(MSG_ASSERT_RESPONSE_STATUS_CODE, OK, responseEntity.getStatusCode());
    }

    @Test
    public void standardIssueType() {
        when(issueType.isSubtask()).thenReturn(false);
        ResponseEntity<WipValidatorResponse> responseEntity = subject.validate("", USER, STATUS);
        assertTrue(MSG_ASSERT_WIP_SHOULD_HAVE_EXCEEDED, responseEntity.getBody().isWipExceeded);
        assertEquals(MSG_ASSERT_RESPONSE_MESSAGE, MSG_EXPECTED_WIP_EXCEEDED, responseEntity.getBody().message);
        assertEquals(MSG_ASSERT_RESPONSE_STATUS_CODE, OK, responseEntity.getStatusCode());
    }

    @Test
    public void classOfServiceFieldNull() {
        when(issue.getField(anyString())).thenReturn(null);
        ResponseEntity<WipValidatorResponse> responseEntity = subject.validate("", USER, STATUS);
        assertTrue(MSG_ASSERT_WIP_SHOULD_HAVE_EXCEEDED, responseEntity.getBody().isWipExceeded);
        assertEquals(MSG_ASSERT_RESPONSE_MESSAGE, MSG_EXPECTED_WIP_EXCEEDED, responseEntity.getBody().message);
        assertEquals(MSG_ASSERT_RESPONSE_STATUS_CODE, OK, responseEntity.getStatusCode());
    }

    @Test
    public void classOfServiceValueNull() {
        when(issueField.getValue()).thenReturn(null);
        ResponseEntity<WipValidatorResponse> responseEntity = subject.validate("", USER, STATUS);
        assertTrue(MSG_ASSERT_WIP_SHOULD_HAVE_EXCEEDED, responseEntity.getBody().isWipExceeded);
        assertEquals(MSG_ASSERT_RESPONSE_MESSAGE, MSG_EXPECTED_WIP_EXCEEDED, responseEntity.getBody().message);
        assertEquals(MSG_ASSERT_RESPONSE_STATUS_CODE, OK, responseEntity.getStatusCode());
    }

    @Test
    public void jsonClassOfServiceInvalid() throws JSONException {
        when(issueField.getValue()).thenReturn(new JSONObject("{valuee:Expedite}"));
        ResponseEntity<WipValidatorResponse> responseEntity = subject.validate("", USER, STATUS);
        assertTrue(MSG_ASSERT_WIP_SHOULD_HAVE_EXCEEDED, responseEntity.getBody().isWipExceeded);
        assertEquals(MSG_ASSERT_RESPONSE_MESSAGE, MSG_EXPECTED_WIP_EXCEEDED, responseEntity.getBody().message);
        assertEquals(MSG_ASSERT_RESPONSE_STATUS_CODE, OK, responseEntity.getStatusCode());
    }

    @Test
    public void throwedNullPointerException() {
        when(userTeamRepo.findByUserName(USER)).thenReturn(null);
        ResponseEntity<WipValidatorResponse> responseEntity = subject.validate("", USER, STATUS);
        assertFalse(MSG_ASSERT_WIP_SHOULDN_T_HAVE_EXCEEDED, responseEntity.getBody().isWipExceeded);
        assertEquals(MSG_ASSERT_RESPONSE_MESSAGE, "java.lang.NullPointerException", responseEntity.getBody().message);
        assertEquals(MSG_ASSERT_RESPONSE_STATUS_CODE, INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    }

    @Test
    public void sortWipConfig() {
        when(wipConfig2.getTeam()).thenReturn(TEAM_NAME);
        when(wipConfig2.getWip()).thenReturn(0);
        when(wipConfigRepo.findByTeamAndStatus(TEAM_NAME, STATUS)).thenReturn(asList(wipConfig, wipConfig2));
        ResponseEntity<WipValidatorResponse> responseEntity = subject.validate("I-1", USER, STATUS);
        assertTrue(MSG_ASSERT_WIP_SHOULD_HAVE_EXCEEDED, responseEntity.getBody().isWipExceeded);
        assertEquals(MSG_ASSERT_RESPONSE_MESSAGE,
                "You can't exceed your team's WIP limit (Team: Team, Actual: 1, Limit: 0)",
                responseEntity.getBody().message);
        assertEquals(MSG_ASSERT_RESPONSE_STATUS_CODE, OK, responseEntity.getStatusCode());
    }

    @Test
    public void noWipExceeded() {
        when(wipConfig.getWip()).thenReturn(2);
        ResponseEntity<WipValidatorResponse> responseEntity = subject.validate("I-1", USER, STATUS);
        assertFalse(MSG_ASSERT_WIP_SHOULDN_T_HAVE_EXCEEDED, responseEntity.getBody().isWipExceeded);
        assertEquals(MSG_ASSERT_RESPONSE_MESSAGE, "(Team: Team, Actual: 1, Limit: 2)", responseEntity.getBody().message);
        assertEquals(MSG_ASSERT_RESPONSE_STATUS_CODE, OK, responseEntity.getStatusCode());
    }

    @Test
    public void issueNotFoundThrowedNullPointerException() {
        when(jiraService.getIssueByKeyAsMaster(anyString())).thenThrow(new NullPointerException());
        ResponseEntity<WipValidatorResponse> responseEntity = subject.validate("", "", "");
        assertFalse(MSG_ASSERT_WIP_SHOULDN_T_HAVE_EXCEEDED, responseEntity.getBody().isWipExceeded);
        assertEquals(MSG_ASSERT_RESPONSE_MESSAGE, "Issue  not found (java.lang.NullPointerException)",
                responseEntity.getBody().message);
        assertEquals(MSG_ASSERT_RESPONSE_STATUS_CODE, PRECONDITION_FAILED, responseEntity.getStatusCode());
    }

    @Test
    public void throwedAnyException() {
        when(jiraSearchService.searchIssues(anyString())).thenThrow(new RuntimeException("Error"));
        ResponseEntity<WipValidatorResponse> responseEntity = subject.validate("I-1", USER, STATUS);
        assertFalse(MSG_ASSERT_WIP_SHOULDN_T_HAVE_EXCEEDED, responseEntity.getBody().isWipExceeded);
        assertEquals(MSG_ASSERT_RESPONSE_MESSAGE, "Error", responseEntity.getBody().message);
        assertEquals(MSG_ASSERT_RESPONSE_STATUS_CODE, INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    }

}
