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
import objective.taskboard.jira.JiraService;
import objective.taskboard.jira.WebhookSubtaskCreatorService;
import objective.taskboard.jira.data.WebhookEvent;
import objective.taskboard.repository.FilterCachedRepository;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;
import objective.taskboard.task.IssueEventProcessScheduler;
import objective.taskboard.task.IssueTypeEventProcessorFactory;
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
        public IssueTypeEventProcessorFactory issueTypeEventProcessorFactory() {
            return new IssueTypeEventProcessorFactory();
        }

        @Bean
        public IssueEventProcessScheduler issueEventProcessScheduler() {
            return new IssueEventProcessScheduler();
        }
    }

    @MockBean
    private ProjectFilterConfigurationCachedRepository projectFilterConfigurationCachedRepository;

    @MockBean
    private JiraService jiraService;

    @MockBean
    private FilterCachedRepository filterCachedRepository;

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
                projectFilterConfigurationCachedRepository,
                filterCachedRepository,
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

    private void givenJiraSend(WebhookControllerTestDSL.WebhookPayloadBuilder... builders) {
        dsl.jiraSend(builders);
    }

    private void whenProcessItems() {
        dsl.whenProcessItems();
    }

    private WebhookControllerTestDSL.IssueBufferServiceAssert then(IssueBufferService issueBufferService) {
        return dsl.then(issueBufferService);
    }

}
