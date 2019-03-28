package objective.taskboard.issueBuffer;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static objective.taskboard.issueBuffer.IssueBufferServiceTest.payload;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.mockito.ArgumentCaptor;

import objective.taskboard.MockBuilder;
import objective.taskboard.controller.WebhookController;
import objective.taskboard.data.Issue;
import objective.taskboard.domain.Filter;
import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.jira.JiraService;
import objective.taskboard.jira.client.JiraIssueDto;
import objective.taskboard.jira.data.WebHookBody;
import objective.taskboard.jira.data.WebhookEvent;
import objective.taskboard.repository.FilterCachedRepository;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;
import objective.taskboard.task.IssueEventProcessScheduler;

public class WebhookControllerTestDSL {

    private final JiraService jiraService;
    private final IssueEventProcessScheduler issueEventProcessScheduler;
    private final WebhookController webhookController;

    private final List<ProjectFilterConfiguration> projects = new ArrayList<>();
    private final List<Issue> issues = new ArrayList<>();

    public static WebhookPayloadBuilder webHook(String payloadFileName) {
        return new WebhookPayloadBuilder().fromFile(payloadFileName);
    }

    public static <A> CallArgumentsAssert withArguments(A a) {
        return new CallArgumentsAssert(a);
    }

    public static <A, B, C> CallArgumentsAssert withArguments(A a, B b, C c) {
        return new CallArgumentsAssert(a, b, c);
    }

    public WebhookControllerTestDSL(
            IssueBufferService issueBufferService,
            ProjectFilterConfigurationCachedRepository projectFilterConfigurationCachedRepository,
            FilterCachedRepository filterCachedRepository,
            JiraService jiraService,
            IssueEventProcessScheduler issueEventProcessScheduler,
            WebhookController webhookController
    ) {
        this.jiraService = jiraService;
        this.issueEventProcessScheduler = issueEventProcessScheduler;
        this.webhookController = webhookController;

        willReturn(projects).given(projectFilterConfigurationCachedRepository).getProjects();
        willAnswer(invocation -> {
            String projectKey = invocation.getArgumentAt(0, String.class);
            return projects.stream().anyMatch(p -> Objects.equals(p.getProjectKey(), projectKey));
        }).given(projectFilterConfigurationCachedRepository).exists(any());
        givenDefaultProjects();

        willReturn(issues).given(issueBufferService)
                .getAllIssues();
        willAnswer(invocation -> {
            String issueKey = invocation.getArgumentAt(0, String.class);
            return issues.stream()
                    .filter(i -> Objects.equals(issueKey, i.getIssueKey()))
                    .findFirst()
                    .orElse(null);
        }).given(issueBufferService).getIssueByKey(any());

        Filter taskFilter = new Filter();
        taskFilter.setIssueTypeId(10000L);

        Filter subtaskFilter = new Filter();
        subtaskFilter.setIssueTypeId(10001L);

        Filter demand = new Filter();
        demand.setIssueTypeId(10600L);

        Filter feature = new Filter();
        feature.setIssueTypeId(10601L);

        willReturn(asList(taskFilter, subtaskFilter, demand, feature))
            .given(filterCachedRepository).getCache();
    }

    public ProjectMockBuilder project() {
        return new ProjectMockBuilder();
    }

    public IssueMockBuilder issue() {
        return new IssueMockBuilder();
    }

    public void given(ProjectMockBuilder... builders) {
        stream(builders)
                .forEach(b -> projects.add(b.build()));
    }

    public void given(IssueMockBuilder... builders) {
        stream(builders)
                .forEach(b -> issues.add(b.build()));
    }

    public void jiraSend(WebhookPayloadBuilder... builders) {
        stream(builders)
                .forEach(b -> b.build(jiraService, webhookController));
    }

    public void whenProcessItems() {
        issueEventProcessScheduler.processItems();
    }

    public IssueBufferServiceAssert then(IssueBufferService issueBufferService) {
        return new IssueBufferServiceAssert(issueBufferService);
    }

    private void givenDefaultProjects() {
        given(
                project().withKey("TASKB"),
                project().withKey("PROJ1")
        );
    }

    public static class WebhookPayloadBuilder {

        private String fileName;

        private WebhookPayloadBuilder() { }

        public WebhookPayloadBuilder fromFile(String fileName) {
            this.fileName = fileName;
            return this;
        }

        private void build(JiraService jiraService, WebhookController webhookController) {
            WebHookBody payload = payload(fileName);
            JiraIssueDto issue = payload.issue;
            willReturn(issue).given(jiraService).getIssueByKeyAsMaster(eq(issue.getKey()));
            webhookController.webhook(payload, issue.getProject().getKey());
        }
    }

    public static class IssueBufferServiceAssert {

        private final IssueBufferService issueBufferService;

        private IssueBufferServiceAssert(IssueBufferService issueBufferService) {
            this.issueBufferService = issueBufferService;
        }

        public IssueBufferUpdateByEventAssert updateByEvent() {
            return new IssueBufferUpdateByEventAssert(issueBufferService);
        }

        public IssueBufferRemoveIssueAndAddDeletionEventAssert removeIssueAndAddDeletionEvent() {
            return new IssueBufferRemoveIssueAndAddDeletionEventAssert(issueBufferService);
        }
    }

    public static class IssueBufferUpdateByEventAssert {

        private IssueBufferService issueBufferService;

        private IssueBufferUpdateByEventAssert(IssueBufferService issueBufferService) {
            this.issueBufferService = issueBufferService;
        }

        @SuppressWarnings("unchecked")
        public IssueBufferUpdateByEventAssert shouldHaveBeenCalled(CallArgumentsAssert... arguments) {
            ArgumentCaptor<WebhookEvent> event = ArgumentCaptor.forClass(WebhookEvent.class);
            ArgumentCaptor<String> issueKey = ArgumentCaptor.forClass(String.class);
            @SuppressWarnings("rawtypes")
            ArgumentCaptor<Optional<JiraIssueDto>> issue = (ArgumentCaptor) ArgumentCaptor.forClass(Optional.class);

            verify(issueBufferService, atLeastOnce()).updateByEvent(event.capture(), issueKey.capture(), issue.capture());
            for(int i = 0; i < arguments.length; ++i) {
                arguments[i].matches(event.getAllValues().get(i), issueKey.getAllValues().get(i), issue.getAllValues().get(i));
            }
            return this;
        }

        public IssueBufferUpdateByEventAssert shouldNotHaveBeenCalled() {
            verify(issueBufferService, never()).updateByEvent(any(), any(), any());
            return this;
        }
    }

    public static class IssueBufferRemoveIssueAndAddDeletionEventAssert {

        private IssueBufferService issueBufferService;

        private IssueBufferRemoveIssueAndAddDeletionEventAssert(IssueBufferService issueBufferService) {
            this.issueBufferService = issueBufferService;
        }

        public IssueBufferRemoveIssueAndAddDeletionEventAssert shouldHaveBeenCalled(CallArgumentsAssert... arguments) {
            ArgumentCaptor<String> issueKey = ArgumentCaptor.forClass(String.class);

            verify(issueBufferService, atLeastOnce()).removeIssueAndAddDeletionEvent(issueKey.capture());
            for(int i = 0; i < arguments.length; ++i) {
                arguments[i].matches(issueKey.getAllValues().get(i));
            }
            return this;
        }

        public IssueBufferRemoveIssueAndAddDeletionEventAssert shouldNotHaveBeenCalled() {
            verify(issueBufferService, never()).removeIssueAndAddDeletionEvent(any());
            return this;
        }
    }

    public static class CallArgumentsAssert {

        @SuppressWarnings("rawtypes")
        private final Matcher[] argumentMatchers;

        private CallArgumentsAssert(Object... arguments) {
            this.argumentMatchers = stream(arguments)
                    .map(CallArgumentsAssert::asMatcher)
                    .toArray(Matcher[]::new);
        }

        @SuppressWarnings("rawtypes")
        private static Matcher asMatcher(Object arg) {
            return (arg instanceof Matcher) ? (Matcher)arg : equalTo(arg);
        }

        @SuppressWarnings("unchecked")
        public void matches(Object... values) {
            assertThat(values).hasSize(argumentMatchers.length);
            for(int i = 0; i < argumentMatchers.length; ++i) {
                MatcherAssert.assertThat(values[i], argumentMatchers[i]);
            }
        }
    }

    public static class ProjectMockBuilder extends MockBuilder<ProjectFilterConfiguration> {

        public ProjectMockBuilder() {
            super(ProjectFilterConfiguration.class);
        }

        public ProjectMockBuilder withKey(String key) {
            config.put(ProjectFilterConfiguration::getProjectKey, key);
            return this;
        }
    }

    public static class IssueMockBuilder extends MockBuilder<Issue> {

        public IssueMockBuilder() {
            super(Issue.class);
        }

        public IssueMockBuilder withKey(String key) {
            config.put(Issue::getIssueKey, key);
            return this;
        }
    }
}
