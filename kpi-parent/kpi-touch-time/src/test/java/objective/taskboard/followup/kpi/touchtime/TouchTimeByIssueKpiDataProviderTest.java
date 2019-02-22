package objective.taskboard.followup.kpi.touchtime;

import static objective.taskboard.utils.DateTimeUtils.parseDateTime;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.time.ZoneId;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.followup.kpi.IssueKpiService;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.enviroment.DSLKpi;
import objective.taskboard.followup.kpi.enviroment.DSLSimpleBehaviorWithAsserter;
import objective.taskboard.followup.kpi.enviroment.KpiEnvironment;
import objective.taskboard.followup.kpi.properties.KPIProperties;
import objective.taskboard.followup.kpi.touchtime.TouchTimeByIssueKpiDataSet;
import objective.taskboard.followup.kpi.touchtime.TouchTimeByIssueKpiDataPoint;
import objective.taskboard.followup.kpi.touchtime.TouchTimeByIssueKpiDataProvider;
import objective.taskboard.jira.properties.JiraProperties;

@RunWith(MockitoJUnitRunner.class)
public class TouchTimeByIssueKpiDataProviderTest {

    private static final ZoneId ZONE_ID = ZoneId.systemDefault();

    @Test
    public void getTouchTimeChartDataSet_happyPath() {
        DSLKpi dsl = new DSLKpi();
        dsl.environment()

            .withStatus("Open").isNotProgressing()
            .withStatus("To Plan").isNotProgressing()
            .withStatus("Planning").isProgressing()
            .withStatus("To Dev").isNotProgressing()
            .withStatus("Developing").isProgressing()

            .withStatus("To Do").isNotProgressing()
            .withStatus("Doing").isProgressing()
            .withStatus("To Review").isNotProgressing()
            .withStatus("Reviewing").isProgressing()

            .withStatus("Done").isNotProgressing()

            .withFeatureType("Task")
            .withSubtaskType("Backend Development")
            .withSubtaskType("Tech Analysis")

            .todayIs("2018-12-17")
        .withJiraProperties()
            .withSubtaskStatusPriorityOrder("Cancelled", "Done", "Reviewing", "To Review", "Doing", "To Do", "Open")
        .eoJp()
        .givenIssue("I-1")
            .isFeature()
            .project("TASKB")
            .type("Task")
            .withTransitions()
                .status("Open").date("2018-11-05")
                .status("To Plan").date("2018-11-05")
                .status("Planning").date("2018-11-05")
                .status("To Dev").date("2018-11-06")
                .status("Developing").date("2018-11-07")
                .status("Done").date("2018-11-09")
            .eoT()
            .subtask("I-2")
                .type("Tech Analysis")
                .withTransitions()
                    .status("Open").date("2018-11-05")
                    .status("To Do").date("2018-11-05")
                    .status("Doing").date("2018-11-05")
                    .status("Done").date("2018-11-06")
                .eoT()
                .worklogs()
                    .at("2018-11-05").timeSpentInHours(8.0)
                    .at("2018-11-06").timeSpentInHours(2.5)
                .eoW()
            .endOfSubtask()
        .eoI()
        .givenIssue("I-3")
            .isDemand()
            .project("TASKB")
            .type("Demand")
            .withTransitions()
                .status("Open").date("2018-11-05")
                .status("To Plan").date("2018-11-05")
                .status("Planning").date("2018-11-05")
                .status("To Dev").date("2018-11-06")
                .status("Developing").date("2018-11-07")
                .status("Done").date("2018-11-09")
            .eoT()
            .subtask("I-4")
                .type("Backend Development")
                .withTransitions()
                    .status("Open").date("2018-11-05")
                    .status("To Do").date("2018-11-06")
                    .status("Doing").date("2018-11-07")
                    .status("To Review").date("2018-11-08")
                    .status("Reviewing").date("2018-11-09")
                    .status("Done").date("2018-11-09")
                .eoT()
                .worklogs()
                    .at("2018-11-07").timeSpentInHours(2.0)
                    .at("2018-11-09").timeSpentInHours(4.0)
                .eoW()
            .endOfSubtask()
        .eoI()
        .when()
            .appliesBehavior(generateDataSetFor("TASKB", KpiLevel.SUBTASKS, ZONE_ID))
        .then()
            .assertSize(4)
            .assertPoints()
                .hasIssueKey("I-2").hasIssueType("Tech Analysis").hasIssueStatus("Doing").hasWorkInHours(10.5)
                    .hasStartProgressingDate("2018-11-05").hasEndProgressingDate("2018-11-06").and()
                .hasIssueKey("I-2").hasIssueType("Tech Analysis").hasIssueStatus("Reviewing").hasWorkInHours(0)
                    .hasStartProgressingDate("2018-11-05").hasEndProgressingDate("2018-11-06").and()
                .hasIssueKey("I-4").hasIssueType("Backend Development").hasIssueStatus("Doing").hasWorkInHours(2.0)
                      .hasStartProgressingDate("2018-11-07").hasEndProgressingDate("2018-11-09").and()
                .hasIssueKey("I-4").hasIssueType("Backend Development").hasIssueStatus("Reviewing").hasWorkInHours(4.0)
                      .hasStartProgressingDate("2018-11-07").hasEndProgressingDate("2018-11-09");
    }

    @Test
    public void getTouchTimeChartDataSet_whenNoIssues_thenEmptyDataSet() {
        DSLKpi dsl = new DSLKpi();
        dsl.environment()

            .withStatus("Open").isNotProgressing()
            .withStatus("To Plan").isNotProgressing()
            .withStatus("Planning").isProgressing()
            .withStatus("To Dev").isNotProgressing()
            .withStatus("Developing").isProgressing()

            .withStatus("To Do").isNotProgressing()
            .withStatus("Doing").isProgressing()
            .withStatus("To Review").isNotProgressing()
            .withStatus("Reviewing").isProgressing()

            .withStatus("Done").isNotProgressing()

            .withFeatureType("Task")
            .withSubtaskType("Backend Development")
            .withSubtaskType("Tech Analysis")

            .todayIs("2018-12-17")
        .withJiraProperties()
            .withSubtaskStatusPriorityOrder("Cancelled", "Done", "Reviewing", "To Review", "Doing", "To Do", "Open")
        .eoJp()
        .when()
            .appliesBehavior(generateDataSetFor("TASKB", KpiLevel.SUBTASKS, ZONE_ID))
        .then()
            .assertSize(0);
    }

    private TouchTimeDataProviderBehavior generateDataSetFor(String projectKey, KpiLevel issueLevel, ZoneId timezone) {
        return new TouchTimeDataProviderBehavior(projectKey, issueLevel, timezone);
    }

    private class TouchTimeDataProviderBehavior implements DSLSimpleBehaviorWithAsserter<TouchTimeDataSetAsserter> {

        private String projectKey;
        private KpiLevel issueLevel;
        private ZoneId timezone;
        private TouchTimeByIssueKpiDataSet dataset;

        public TouchTimeDataProviderBehavior(String projectKey, KpiLevel issueLevel, ZoneId timezone) {
            this.projectKey = projectKey;
            this.issueLevel = issueLevel;
            this.timezone = timezone;
        }

        @Override
        public void behave(KpiEnvironment environment) {
            KPIProperties kpiProperties = environment.getKPIProperties();
            JiraProperties jiraProperties = environment.getJiraProperties();
            IssueKpiService issueKpiService = environment.services().issueKpi().getService();
            TouchTimeByIssueKpiDataProvider suject = new TouchTimeByIssueKpiDataProvider(issueKpiService, jiraProperties, kpiProperties);
            this.dataset = suject.getDataSet(projectKey, issueLevel, timezone);
        }

        @Override
        public TouchTimeDataSetAsserter then() {
            return new TouchTimeDataSetAsserter(dataset);
        }
    }

    private class TouchTimeDataSetAsserter {

        private TouchTimeByIssueKpiDataSet dataset;

        public TouchTimeDataSetAsserter(TouchTimeByIssueKpiDataSet dataset) {
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

            private TouchTimeByIssueKpiDataPoint currentPoint;
            private Iterator<TouchTimeByIssueKpiDataPoint> iterator;

            public TouchTimePointsAsserter(List<TouchTimeByIssueKpiDataPoint> points) {
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

            public TouchTimePointsAsserter hasStartProgressingDate(String date) {
                assertThat(currentPoint.startProgressingDate, is(parseDateTime(date).toInstant()));
                return this;
            }

            public TouchTimePointsAsserter hasEndProgressingDate(String date) {
                assertThat(currentPoint.endProgressingDate, is(parseDateTime(date).toInstant()));
                return this;
            }
        }
    }
}
