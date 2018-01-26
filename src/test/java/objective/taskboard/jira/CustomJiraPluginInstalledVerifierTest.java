package objective.taskboard.jira;

import objective.taskboard.jira.data.plugin.UserDetail;
import objective.taskboard.jira.endpoint.JiraEndpointAsMaster;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedString;

import static java.util.Collections.emptyList;
import static objective.taskboard.jira.AuthorizedJiraEndpointTest.JIRA_MASTER_USERNAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Matchers.eq;

@RunWith(MockitoJUnitRunner.class)
public class CustomJiraPluginInstalledVerifierTest {

    private static final String JIRA_PLUGIN_URL = "http://localhost:4567/rest/projectbuilder/1.0/user/master";
    private static final String ERROR_BODY = "{\"message\":\"null for uri: " + JIRA_PLUGIN_URL + "\",\"status-code\":404}";

    @Mock
    private UserDetail userDetail;

    @Mock
    private JiraProperties jiraProperties;

    @Mock
    private UserDetail.Service userDetailService;

    @Mock
    private JiraEndpointAsMaster jiraEndpointAsMaster;

    @InjectMocks
    private CustomJiraPluginInstalledVerifier subject;

    @Before
    public void setup() {
        willReturn(userDetailService).given(jiraEndpointAsMaster).request(eq(UserDetail.Service.class));

        JiraProperties.Lousa lousa = new JiraProperties.Lousa();
        lousa.setUsername(JIRA_MASTER_USERNAME);
        willReturn(lousa).given(jiraProperties).getLousa();
    }

    @Test
    public void givenCustomPluginEndpointNotFound_thenThrowException() {
        // given
        Response response = new Response(JIRA_PLUGIN_URL, 404, "not found", emptyList(), new TypedString(ERROR_BODY));
        willThrow(RetrofitError.httpError(JIRA_PLUGIN_URL, response, null, null))
                .given(userDetailService).get(eq(JIRA_MASTER_USERNAME));

        try {
            // when
            subject.checkCustomJiraPlugin();
            fail();

            // then
        } catch (CustomJiraPluginInstalledVerifier.CustomJiraPluginNotInstalledException ex) {
            assertThat(ex.getExitCode()).isNotEqualTo(0);
        }
    }

    @Test
    public void givenCustomPluginInstalled_thenLifeIsPeachy() {
        // given
        willReturn(userDetail).given(userDetailService).get(eq(JIRA_MASTER_USERNAME));

        // when
        subject.checkCustomJiraPlugin();

        // then
        assertThat(getLife(), is("peachy"));
    }

    private String getLife() {
        String life = "peachy";
        return life;
    }
}
