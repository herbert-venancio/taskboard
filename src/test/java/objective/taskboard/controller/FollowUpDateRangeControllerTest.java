package objective.taskboard.controller;

import static objective.taskboard.repository.PermissionRepository.DASHBOARD_OPERATIONAL;
import static objective.taskboard.repository.PermissionRepository.DASHBOARD_TACTICAL;
import static objective.taskboard.testUtils.ControllerTestUtils.asJsonStringResponseOnly;
import static objective.taskboard.testUtils.ControllerTestUtils.getDefaultMockMvc;
import static objective.taskboard.testUtils.ControllerTestUtils.getLocalDateSerialized;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import objective.taskboard.auth.Authorizer;
import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.FollowUpDateRangeProvider;
import objective.taskboard.followup.FollowUpDateRangeProvider.FollowUpProjectDataRangeDTO;
import objective.taskboard.jira.FrontEndMessageException;
import objective.taskboard.jira.ProjectService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@WebAppConfiguration
public class FollowUpDateRangeControllerTest {

    private static final String PROJECT_KEY = "TEST";

    private MockMvc mockMvc;

    @Mock
    private Authorizer authorizer;

    @Mock
    private ProjectService projectService;

    @Mock
    private FollowUpDateRangeProvider provider;

    @InjectMocks
    private FollowUpDateRangeController subject;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = getDefaultMockMvc(subject);
    }

    @Test
    public void ifProjectExitsAndUserHasPermission_returnValue() throws Exception {
        LocalDate startDate = LocalDate.of(2017, 1, 1);
        LocalDate deliveryDate = LocalDate.of(2018, 1, 1);

        ProjectFilterConfiguration taskboardProject = new ProjectFilterConfiguration();
        taskboardProject.setProjectKey(PROJECT_KEY);
        taskboardProject.setStartDate(startDate);
        taskboardProject.setDeliveryDate(deliveryDate);

        when(authorizer.hasPermissionInProject(DASHBOARD_TACTICAL, PROJECT_KEY)).thenReturn(true);
        when(authorizer.hasPermissionInProject(DASHBOARD_OPERATIONAL, PROJECT_KEY)).thenReturn(true);
        when(projectService.taskboardProjectExists(PROJECT_KEY)).thenReturn(true);
        when(provider.getDateRangeData(PROJECT_KEY)).thenReturn(new FollowUpProjectDataRangeDTO(taskboardProject));

        mockMvc.perform(get("/api/projects/"+PROJECT_KEY+"/followup/date-range"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{"
                        + "'projectKey':'"+ PROJECT_KEY +"',"
                        + "'startDate':"+ getLocalDateSerialized(startDate) +","
                        + "'deliveryDate':"+ getLocalDateSerialized(deliveryDate) +""
                        + "}"));
    }

    @Test
    public void ifThereIsNoStartOrDeliveryDate_returnInternalServerError() throws Exception {
        final String expectedError = "No \"Start Date\" or \"Delivery Date\" configuration found for project " + PROJECT_KEY + ".";

        when(authorizer.hasPermissionInProject(DASHBOARD_TACTICAL, PROJECT_KEY)).thenReturn(true);
        when(authorizer.hasPermissionInProject(DASHBOARD_OPERATIONAL, PROJECT_KEY)).thenReturn(true);
        when(projectService.taskboardProjectExists(PROJECT_KEY)).thenReturn(true);
        when(provider.getDateRangeData(PROJECT_KEY)).thenThrow(new FrontEndMessageException(expectedError));

        mockMvc.perform(get("/api/projects/"+PROJECT_KEY+"/followup/date-range"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json(asJsonStringResponseOnly(expectedError)));
    }

    @Test
    public void ifUserHasNoPermissionInTacticalOrOperational_returnResourceNotFound() throws Exception {
        final String expectedError = "Resource not found.";

        when(authorizer.hasPermissionInProject(DASHBOARD_TACTICAL, PROJECT_KEY)).thenReturn(true);
        when(authorizer.hasPermissionInProject(DASHBOARD_OPERATIONAL, PROJECT_KEY)).thenReturn(false);

        mockMvc.perform(get("/api/projects/"+PROJECT_KEY+"/followup/date-range"))
                .andExpect(status().isNotFound())
                .andExpect(content().json(asJsonStringResponseOnly(expectedError)));

        when(authorizer.hasPermissionInProject(DASHBOARD_TACTICAL, PROJECT_KEY)).thenReturn(false);
        when(authorizer.hasPermissionInProject(DASHBOARD_OPERATIONAL, PROJECT_KEY)).thenReturn(true);

        mockMvc.perform(get("/api/projects/"+PROJECT_KEY+"/followup/date-range"))
                .andExpect(status().isNotFound())
                .andExpect(content().json(asJsonStringResponseOnly(expectedError)));
    }

    @Test
    public void ifProjectDoesntExist_returnProjectNotFound() throws Exception {
        final String expectedError = "Project not found: " + PROJECT_KEY + ".";

        when(authorizer.hasPermissionInProject(DASHBOARD_TACTICAL, PROJECT_KEY)).thenReturn(true);
        when(authorizer.hasPermissionInProject(DASHBOARD_OPERATIONAL, PROJECT_KEY)).thenReturn(true);
        when(projectService.taskboardProjectExists(PROJECT_KEY)).thenReturn(false);

        mockMvc.perform(get("/api/projects/"+PROJECT_KEY+"/followup/date-range"))
                .andExpect(status().isNotFound())
                .andExpect(content().json(asJsonStringResponseOnly(expectedError)));
    }

}
