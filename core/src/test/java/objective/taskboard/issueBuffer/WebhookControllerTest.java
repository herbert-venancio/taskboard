package objective.taskboard.issueBuffer;

import static objective.taskboard.issueBuffer.WebhookControllerTestDSL.webHook;
import static objective.taskboard.issueBuffer.WebhookControllerTestDSL.withArguments;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import objective.taskboard.controller.WebhookController;
import objective.taskboard.controller.WebhookHelper;
import objective.taskboard.jira.JiraService;
import objective.taskboard.jira.WebhookSubtaskCreatorService;
import objective.taskboard.jira.data.WebhookEvent;
import objective.taskboard.filter.LaneService;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;
import objective.taskboard.task.IssueEventProcessScheduler;
import objective.taskboard.task.IssueEventProcessorFactory;
import objective.taskboard.testUtils.OptionalAutowiredDependenciesInitializer;

@RunWith(SpringRunner.class)
@ContextConfiguration(initializers = OptionalAutowiredDependenciesInitializer.class, classes = WebhookControllerTest.Configuration.class)
public class WebhookControllerTest {

    public static class Configuration {

        @MockBean
        private WebhookSubtaskCreatorService webhookSubtaskCreatorService;

        @Bean
        public WebhookController webhookController() {
            return new WebhookController();
        }

        @Bean
        public IssueEventProcessorFactory issueTypeEventProcessorFactory() {
            return new IssueEventProcessorFactory();
        }

        @Bean
        public IssueEventProcessScheduler issueEventProcessScheduler() {
            return new IssueEventProcessScheduler();
        }

        @Bean
        public WebhookHelper webhookHelper() {
            return new WebhookHelper();
        }
    }

    @MockBean
    private ProjectFilterConfigurationCachedRepository projectFilterConfigurationCachedRepository;

    @MockBean
    private JiraService jiraService;

    @MockBean
    private LaneService laneService;

    @MockBean
    private IssueBufferService issueBufferService;

    @Autowired
    private WebhookController webhookController;

    @Autowired
    private IssueEventProcessScheduler issueEventProcessScheduler;

    private WebhookControllerTestDSL dsl;

    @Before
    public void setup() {
        dsl = new WebhookControllerTestDSL(
                issueBufferService,
                projectFilterConfigurationCachedRepository,
                laneService,
                jiraService,
                issueEventProcessScheduler,
                webhookController
        );
    }

    @Test
    public void create() {
        givenJiraSend(
                webHook("create-TASKB-1.json"),
                webHook("create-TASKB-2-subtaskof-TASKB-1.json")
        );

        whenProcessItems();

        then(issueBufferService).updateByEvent()
                .shouldHaveBeenCalled(
                        withArguments(equalTo(WebhookEvent.ISSUE_CREATED), equalTo("TASKB-1"), anything()),
                        withArguments(equalTo(WebhookEvent.ISSUE_CREATED), equalTo("TASKB-2"), anything())
                );
    }

    @Test
    public void update() {
        givenJiraSend(
                webHook("update-TASKB-2-converttotask.json"),
                webHook("update-TASKB-2-converttosubtaskof-TASKB-1.json")
        );

        whenProcessItems();

        then(issueBufferService).updateByEvent()
                .shouldHaveBeenCalled(
                        withArguments(equalTo(WebhookEvent.ISSUE_UPDATED), equalTo("TASKB-2"), anything()),
                        withArguments(equalTo(WebhookEvent.ISSUE_UPDATED), equalTo("TASKB-2"), anything())
                );
    }

    @Test
    public void delete() {
        givenJiraSend(
                webHook("delete-TASKB-2.json")
        );

        whenProcessItems();

        then(issueBufferService).updateByEvent()
                .shouldHaveBeenCalled(
                        withArguments(equalTo(WebhookEvent.ISSUE_DELETED), equalTo("TASKB-2"), anything())
                );
    }

    @Test
    public void whenIssueChangedProject_shouldRemoveIssueFromOldProjectAndAddToNewProject() {
        givenExistingIssue("TASKB-237");
        givenJiraSend(webHook("TASKB-237_movePayload.json"));

        whenProcessItems();

        then(issueBufferService).removeIssueAndAddDeletionEvent()
                .shouldHaveBeenCalled(withArguments("TASKB-237"));
        then(issueBufferService)
            .updateByEvent()
            .shouldHaveBeenCalled(
                withArguments(equalTo(WebhookEvent.ISSUE_UPDATED), equalTo("PROJ1-066"), anything())
            );
    }

    @Test
    public void whenIssueChangedToUnknownProject_shouldRemoveIssueFromOldProjectAndNotCallUpdateByEvent() {
        givenExistingIssue("TASKB-1");
        givenJiraSend(webHook("update-TASKB-1-changeproject-OTHER-1.json"));

        whenProcessItems();

        then(issueBufferService).removeIssueAndAddDeletionEvent()
                .shouldHaveBeenCalled(withArguments("TASKB-1"));
        then(issueBufferService).updateByEvent()
                .shouldNotHaveBeenCalled();
    }

    @Test
    public void whenIssueChangedFromUnknownToKnownProject_shouldNotCallRemoveIssue() {
        givenJiraSend(webHook("update-OTHER-1-changeproject-TASKB-1.json"));

        whenProcessItems();

        then(issueBufferService).removeIssueAndAddDeletionEvent()
                .shouldNotHaveBeenCalled();
        then(issueBufferService).updateByEvent()
                .shouldHaveBeenCalled(
                        withArguments(equalTo(WebhookEvent.ISSUE_UPDATED), equalTo("TASKB-1"), anything())
                );
    }

    private void givenJiraSend(WebhookControllerTestDSL.WebhookPayloadBuilder... builders) {
        dsl.jiraSend(builders);
    }

    private void givenExistingIssue(String issueKey) {
        dsl.given(dsl.issue().withKey(issueKey));
    }

    private void whenProcessItems() {
        dsl.whenProcessItems();
    }

    private WebhookControllerTestDSL.IssueBufferServiceAssert then(IssueBufferService issueBufferService) {
        return dsl.then(issueBufferService);
    }

}
