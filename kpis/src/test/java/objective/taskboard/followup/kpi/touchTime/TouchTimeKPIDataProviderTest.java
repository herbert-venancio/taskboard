package objective.taskboard.followup.kpi.touchTime;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.time.ZoneId;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.IssueKpiService;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.enviroment.KPIEnvironmentBuilder;
import objective.taskboard.followup.kpi.properties.KPIProperties;
import objective.taskboard.jira.properties.JiraProperties;
import objective.taskboard.jira.properties.StatusConfiguration.StatusPriorityOrder;

@RunWith(MockitoJUnitRunner.class)
public class TouchTimeKPIDataProviderTest {

    private static final ZoneId ZONE_ID = ZoneId.systemDefault();

    private TouchTimeKPIDataProvider subject;

    @Test
    public void getTouchTimeChartDataSet_happyPath() {

        subject = configureTestEnvironmentForProject("TASKB")
                .withSubtaskStatusPriorityOrder("Cancelled", "Done", "Reviewing", "To Review", "Doing", "To Do", "Open")
                    .withStatus("Open").isNotProgressing()
                    .withStatus("To Do").isNotProgressing()
                    .withStatus("Doing").isProgressing()
                    .withStatus("To Review").isNotProgressing()
                    .withStatus("Reviewing").isProgressing()
                    .withStatus("Done").isNotProgressing()
                    .withStatus("Cancelled").isNotProgressing()
                .withSubtaskType("Backend Development")
                .withSubtaskType("Alpha Test")
                .withSubtaskIssue()
                    .withKey("I-1")
                    .hasType("Backend Development")
                    .transitedToStatus("Open").on("2018-11-05")
                    .transitedToStatus("To Do").on("2018-11-06")
                    .transitedToStatus("Doing").on("2018-11-07")
                    .transitedToStatus("To Review").on("2018-11-08")
                    .transitedToStatus("Reviewing").on("2018-11-09")
                    .transitedToStatus("Done").on("2018-11-10")
                    .withWorkInHours(2.0).on("2018-11-07")
                    .withWorkInHours(4.0).on("2018-11-09")
                .done()
                .withSubtaskIssue()
                    .withKey("I-2")
                    .hasType("Alpha Test")
                    .transitedToStatus("Open").on("2018-11-05")
                    .transitedToStatus("To Do").on("2018-11-06")
                    .transitedToStatus("Doing").on("2018-11-07")
                    .transitedToStatus("To Review").on("2018-11-08")
                    .transitedToStatus("Reviewing").on("2018-11-09")
                    .transitedToStatus("Done").on("2018-11-10")
                    .withWorkInHours(8.0).on("2018-11-07")
                    .withWorkInHours(2.5).on("2018-11-08")
                    .withWorkInHours(8.0).on("2018-11-09")
                    .withWorkInHours(4.0).on("2018-11-10")
                .done()
                .configureDataProvider();

        TouchTimeChartDataSet actual = subject.getDataSet("TASKB", KpiLevel.SUBTASKS, ZONE_ID);
        
        givenDataSet(actual)
            .assertSize(4)
            .assertPoints()
                .hasIssueKey("I-1").hasIssueType("Backend Development").hasIssueStatus("Doing").hasWorkInHours(2.0).and()
                .hasIssueKey("I-1").hasIssueType("Backend Development").hasIssueStatus("Reviewing").hasWorkInHours(4.0).and()
                .hasIssueKey("I-2").hasIssueType("Alpha Test").hasIssueStatus("Doing").hasWorkInHours(10.5).and()
                .hasIssueKey("I-2").hasIssueType("Alpha Test").hasIssueStatus("Reviewing").hasWorkInHours(12.0);
    }

    @Test
    public void getTouchTimeChartDataSet_whenNoIssues_thenEmptyDataSet() {

        subject = configureTestEnvironmentForProject("TASKB")
                .withSubtaskStatusPriorityOrder("Cancelled", "Done", "Reviewing", "To Review", "Doing", "To Do", "Open")
                    .withStatus("Open").isNotProgressing()
                    .withStatus("To Do").isProgressing()
                    .withStatus("To Review").isNotProgressing()
                    .withStatus("Reviewing").isProgressing()
                    .withStatus("Done").isNotProgressing()
                    .withStatus("Cancelled").isNotProgressing()
                .withSubtaskType("Backend Development")
                .withSubtaskType("Alpha Test")
                .configureDataProvider();

        TouchTimeChartDataSet actual = subject.getDataSet("TASKB", KpiLevel.SUBTASKS, ZONE_ID);

        givenDataSet(actual)
            .assertSize(0);
    }
    

    private TestEnvironmentDSL configureTestEnvironmentForProject(String projectKey) {
        return new TestEnvironmentDSL(projectKey);
    }

    private TouchTimeDataSetAsserter givenDataSet(TouchTimeChartDataSet actual) {
        return new TouchTimeDataSetAsserter(actual);
    }

    private class TestEnvironmentDSL {

        private KPIProperties kpiProperties = Mockito.mock(KPIProperties.class);

        private JiraProperties jiraProperties = Mockito.mock(JiraProperties.class);

        private IssueKpiService issueKpiService = Mockito.mock(IssueKpiService.class);

        private String projectKey;

        private String[] statusesOrdered;

        private List<Status> statuses = new LinkedList<>();

        private List<String> subtaskTypes = new LinkedList<>();

        private List<Issue> issues = new LinkedList<>();

        public TestEnvironmentDSL(String projectKey) {
            this.projectKey = projectKey;
        }

        public TestEnvironmentDSL withSubtaskStatusPriorityOrder(String... statusesOrdered) {
            this.statusesOrdered = statusesOrdered;
            return this;
        }

        public Issue withSubtaskIssue() {
            return new Issue(KpiLevel.SUBTASKS);
        }

        public TestEnvironmentDSL withSubtaskType(String type) {
            this.subtaskTypes.add(type);
            return this;
        }

        public Status withStatus(String statusName) {
            return new Status(statusName);
        }

        public TouchTimeKPIDataProvider configureDataProvider() {
            mockKpiProperties();
            mockJiraProperties();
            mockIssueKpiService();
            return new TouchTimeKPIDataProvider(issueKpiService, jiraProperties, kpiProperties);
        }

        private void mockKpiProperties() {
            List<String> progressingStatuses = getProgressingStatuses();
            Mockito.when(kpiProperties.getProgressingStatuses()).thenReturn(progressingStatuses);
        }

        private void mockJiraProperties() {
            StatusPriorityOrder statusOrder = new StatusPriorityOrder();
            statusOrder.setSubtasks(statusesOrdered);
            Mockito.when(jiraProperties.getStatusPriorityOrder()).thenReturn(statusOrder);
        }

        private void mockIssueKpiService() {
            KPIEnvironmentBuilder builder = new KPIEnvironmentBuilder().withKpiProperties(kpiProperties);
            statuses.forEach(s -> builder.addStatus(s.id, s.name, s.isProgressing));
            for (int i = 0; i < subtaskTypes.size(); i++) {
                String type = subtaskTypes.get(i);
                builder.addSubtaskType((long) i, type);
            }

            issues.forEach(issue -> {
                builder.withMockingIssue(issue.key, issue.type, issue.level)
                    .setProjectKeyToCurrentIssue(projectKey)
                    .setCurrentStatusToCurrentIssue(issue.getCurrentStatus());
                issue.transitions.forEach(t -> builder.addTransition(t.toStatus, t.date));
                issue.worklogs.forEach(w -> builder.addWorklog(w.date, w.effortInHours));
            });

            final List<IssueKpi> issues = builder.buildAllIssuesAsKpi();
            Mockito.when(issueKpiService.getIssuesFromCurrentState(this.projectKey, ZONE_ID, KpiLevel.SUBTASKS))
                    .thenReturn(issues);
        }

        private List<String> getProgressingStatuses() {
            return statuses.stream()
                    .filter(s -> s.isProgressing)
                    .map(status -> status.name)
                    .collect(Collectors.toList());
        }

        private void addIssue(Issue issue) {
            this.issues.add(issue);
        }

        private void addStatus(Status status) {
            this.statuses.add(status);
            status.setId(this.statuses.size());
        }

        private class Status {
            private final String name;
            private long id;
            private boolean isProgressing = false;

            public Status(String name) {
                this.name = name;
            }

            public void setId(int id) {
                this.id = id;
            }

            private TestEnvironmentDSL isProgressing() {
                this.isProgressing = true;
                TestEnvironmentDSL.this.addStatus(this);
                return TestEnvironmentDSL.this;
            }

            private TestEnvironmentDSL isNotProgressing() {
                TestEnvironmentDSL.this.addStatus(this);
                return TestEnvironmentDSL.this;
            }
        }

        private class Issue {

            private KpiLevel level;
            private String key;
            private String type;
            private List<Transition> transitions = new LinkedList<>();
            private List<Work> worklogs = new LinkedList<>();

            public Issue(KpiLevel level) {
                this.level = level;
            }

            public String getCurrentStatus() {
                return transitions.get(transitions.size() - 1).toStatus;
            }

            public Issue withKey(String key) {
                this.key = key;
                return this;
            }

            public Issue hasType(String type) {
                this.type = type;
                return this;
            }

            public Transition transitedToStatus(String statusName) {
                return new Transition(statusName);
            }

            public Work withWorkInHours(double hours) {
                return new Work(hours);
            }

            public TestEnvironmentDSL done() {
                TestEnvironmentDSL.this.addIssue(this);
                return TestEnvironmentDSL.this;
            }

            public void addWork(Work work) {
                this.worklogs.add(work);

            }

            private void addTransition(Transition transition) {
                this.transitions.add(transition);
            }

            private class Transition {

                private String toStatus;
                private String date;

                public Transition(String statusName) {
                    this.toStatus = statusName;
                }

                public Issue on(String date) {
                    this.date = date;
                    Issue.this.addTransition(this);
                    return Issue.this;
                }
            }

            private class Work {

                private double effortInHours;
                private String date;

                private Work(double effortInHours) {
                    this.effortInHours = effortInHours;
                }

                public Issue on(String date) {
                    this.date = date;
                    Issue.this.addWork(this);
                    return Issue.this;
                }
            }
        }
    }
    
    private class TouchTimeDataSetAsserter {

        private TouchTimeChartDataSet dataset;

        public TouchTimeDataSetAsserter(TouchTimeChartDataSet dataset) {
            this.dataset = dataset;
        }

        public TouchTimePointsAsserter assertPoints() {
            return new TouchTimePointsAsserter(dataset.points);
        }

        public TouchTimeDataSetAsserter assertSize(int size) {
            assertThat(dataset.points.size(), is(size));
            return this;
        }
        
        private class TouchTimePointsAsserter {

            private TouchTimeDataPoint currentPoint;
            private Iterator<TouchTimeDataPoint> iterator;

            public TouchTimePointsAsserter(List<TouchTimeDataPoint> points) {
                iterator = points.iterator();
                currentPoint = iterator.next();
            }

            public TouchTimePointsAsserter hasIssueKey(String issueKey) {
                assertThat(currentPoint.issueKey, is(issueKey));
                return this;
            }

            public TouchTimePointsAsserter hasIssueType(String issueType) {
                assertThat(currentPoint.issueType, is(issueType));
                return this;
            }

            public TouchTimePointsAsserter hasIssueStatus(String status) {
                assertThat(currentPoint.issueStatus, is(status));
                return this;
            }

            public TouchTimePointsAsserter hasWorkInHours(double workInHours) {
                assertThat(currentPoint.effortInHours, is(workInHours));
                return this;
            }

            public TouchTimePointsAsserter and() {
                if (iterator.hasNext()) {
                    this.currentPoint = iterator.next();
                }
                return this;
            }
        }
    }
}