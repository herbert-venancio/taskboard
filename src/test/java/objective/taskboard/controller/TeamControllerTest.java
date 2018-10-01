package objective.taskboard.controller;

import static objective.taskboard.testUtils.ControllerTestUtils.getDefaultMockMvc;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("deprecation")
public class TeamControllerTest {

    private MockMvc mockMvc;

    private TeamController subject = new TeamController();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockMvc = getDefaultMockMvc(subject);
    }

    @Test
    public void whenMakingAnyTeamApiRequest_returnTeamApiDiscontinuedException() throws Exception {
        MockHttpServletRequestBuilder getTeamsRequest = get("/api/teams");
        assertDiscontinued(getTeamsRequest);

        MockHttpServletRequestBuilder getTeamRequest = get("/api/teams/ANYTEAM");
        assertDiscontinued(getTeamRequest);

        MockHttpServletRequestBuilder updateTeamsRequest = patch("/api/teams")
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON)
                .content("[]");
        assertDiscontinued(updateTeamsRequest);

        MockHttpServletRequestBuilder updateTeamRequest = patch("/api/teams/ANYTEAM")
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON)
                .content("{}");
        assertDiscontinued(updateTeamRequest);
    }

    private void assertDiscontinued(MockHttpServletRequestBuilder request) throws Exception {
        mockMvc.perform(request)
            .andExpect(status().isGone())
            .andExpect(status().reason("Team API was discontinued."));
    }

}
