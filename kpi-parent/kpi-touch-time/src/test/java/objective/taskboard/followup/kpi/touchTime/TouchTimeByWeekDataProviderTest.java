package objective.taskboard.followup.kpi.touchTime;

import static objective.taskboard.followup.kpi.properties.KpiTouchTimePropertiesMocker.withTouchTimeSubtaskConfig;
import static objective.taskboard.utils.DateTimeUtils.parseDateTime;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.time.ZoneId;
import java.util.Iterator;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.followup.ProjectDatesNotConfiguredException;
import objective.taskboard.followup.kpi.IssueKpiService;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.enviroment.DSLKpi;
import objective.taskboard.followup.kpi.enviroment.DSLSimpleBehaviorWithAsserter;
import objective.taskboard.followup.kpi.enviroment.KpiEnvironment;
import objective.taskboard.followup.kpi.properties.KPIProperties;
import objective.taskboard.followup.kpi.properties.KpiTouchTimeProperties;
import objective.taskboard.followup.kpi.properties.KpiTouchTimePropertiesMocker;
import objective.taskboard.jira.ProjectService;
import objective.taskboard.jira.properties.JiraProperties;

@RunWith(MockitoJUnitRunner.class)
public class TouchTimeByWeekDataProviderTest {

    private static final ZoneId ZONE_ID = ZoneId.systemDefault();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void getDataSet_happyDay_twoWeeks_fourIssues() {
        dsl().environment()
            .withJiraProperties()
                .withSubtaskStatusPriorityOrder("Done","Reviewing","To Review","Doing","To Do","Open")
            .eoJp()
            .withKpiProperties()
                .atFeatureHierarchy("Doing")
                    .withChildrenType("Backend Development")
                    .withChildrenType("Alpha Bug")
                .eoH()
            .eoKP()
            .withKpiProperties(
                withTouchTimeSubtaskConfig()
                    .withChartStack("Development")
                        .types("Backend Development")
                        .statuses("Doing")
                    .eoS()
                    .withChartStack("Review")
                        .statuses("Reviewing")
                    .eoS()
                .eoTTSC()
            )
            .services()
                .projects()
                    .withKey("TASKB")
                        .startAt("2018-11-12")
                        .deliveredAt("2018-11-26")
                    .eoP()
                .eoPs()
            .eoS()
            .givenSubtask("I-1")
                .project("TASKB")
                .type("Backend Development")
                .withTransitions()
                    .status("Open").date("2018-11-08")
                    .status("To Do").date("2018-11-08")
                    .status("Doing").date("2018-11-10")
                    .status("To Review").date("2018-11-16")
                    .status("Reviewing").date("2018-11-20")
                    .status("Done").date("2018-11-27")
                .eoT()
                .worklogs()
                    .at("2018-11-10").timeSpentInHours(1.0)
                    .at("2018-11-15").timeSpentInHours(2.0)
                    .at("2018-11-19").timeSpentInHours(3.0)
                    .at("2018-11-23").timeSpentInHours(4.0)
                .eoW()
            .eoI()
            .givenSubtask("I-2")
                .project("TASKB")
                .type("Backend Development")
                .withTransitions()
                    .status("Open").date("2018-11-10")
                    .status("To Do").date("2018-11-10")
                    .status("Doing").date("2018-11-13")
                    .status("To Review").date("2018-11-14")
                    .status("Reviewing").date("2018-11-15")
                    .status("Done").date("2018-11-22")
                .eoT()
                .worklogs()
                    .at("2018-11-14").timeSpentInHours(1.0)
                    .at("2018-11-16").timeSpentInHours(2.0)
                    .at("2018-11-20").timeSpentInHours(4.0)
                .eoW()
            .eoI()
            .givenSubtask("I-3")
                .project("TASKB")
                .type("Backend Development")
                .withTransitions()
                    .status("Open").date("2018-11-10")
                    .status("To Do").date("2018-11-10")
                    .status("Doing").date("2018-11-12")
                    .status("To Review").date("2018-11-13")
                    .status("Reviewing").date("2018-11-14")
                    .status("Done").date("2018-11-16")
                .eoT()
                .worklogs()
                    .at("2018-11-12").timeSpentInHours(2.0)
                    .at("2018-11-15").timeSpentInHours(1.0)
                .eoW()
            .eoI()
            .givenSubtask("I-4")
                .project("TASKB")
                .type("Alpha Bug")
                .withTransitions()
                    .status("Open").date("2018-11-18")
                    .status("To Do").date("2018-11-18")
                    .status("Doing").date("2018-11-19")
                    .status("To Review").date("2018-11-21")
                    .status("Reviewing").date("2018-11-22")
                    .status("Done").date("2018-11-24")
                .eoT()
                .worklogs()
                    .at("2018-11-20").timeSpentInHours(2.0)
                    .at("2018-11-23").timeSpentInHours(2.0)
                .eoW()
            .eoI()
        .when()
            .appliesBehavior(generateDataSetFor("TASKB", KpiLevel.SUBTASKS, ZONE_ID))
        .then()
            .assertSize(6)
            .assertPoints()
            .hasDate("2018-11-11").hasStackName("Development").hasWorkInHours(3.0).and()
            .hasDate("2018-11-11").hasStackName("Review").hasNoHoursWorked().and()
            .hasDate("2018-11-18").hasStackName("Development").hasWorkInHours(6.33).and()
            .hasDate("2018-11-18").hasStackName("Review").hasWorkInHours(0.66).and()
            .hasDate("2018-11-25").hasStackName("Development").hasWorkInHours(10.0).and()
            .hasDate("2018-11-25").hasStackName("Review").hasNoHoursWorked();
    }

    @Test
    public void getDataSet_restrictingByTimeLine() {
        dsl().environment()
            .withJiraProperties()
                .withSubtaskStatusPriorityOrder("Done","Reviewing","To Review","Doing","To Do","Open")
            .eoJp()
            .withKpiProperties()
                .atFeatureHierarchy("Doing")
                    .withChildrenType("Backend Development")
                    .withChildrenType("Alpha Bug")
                .eoH()
            .eoKP()
            .withKpiProperties(
                withTouchTimeSubtaskConfig()
                    .withChartStack("Development")
                        .types("Backend Development")
                        .statuses("Doing")
                    .eoS()
                    .withChartStack("Review")
                        .statuses("Reviewing")
                    .eoS()
                .eoTTSC()
            )
            .services()
                .projects()
                    .withKey("TASKB")
                        .startAt("2018-11-12")
                        .deliveredAt("2018-11-16")
                    .eoP()
                .eoPs()
            .eoS()
            .givenSubtask("I-1")
                .project("TASKB")
                .type("Backend Development")
                .withTransitions()
                    .status("Open").date("2018-11-08")
                    .status("To Do").date("2018-11-08")
                    .status("Doing").date("2018-11-10")
                    .status("To Review").date("2018-11-16")
                    .status("Reviewing").date("2018-11-20")
                    .status("Done").date("2018-11-27")
                .eoT()
                .worklogs()
                    .at("2018-11-10").timeSpentInHours(1.0)
                    .at("2018-11-15").timeSpentInHours(2.0)
                    .at("2018-11-19").timeSpentInHours(3.0)
                    .at("2018-11-23").timeSpentInHours(4.0)
                .eoW()
            .eoI()
            .givenSubtask("I-2")
                .project("TASKB")
                .type("Backend Development")
                .withTransitions()
                    .status("Open").date("2018-11-10")
                    .status("To Do").date("2018-11-10")
                    .status("Doing").date("2018-11-13")
                    .status("To Review").date("2018-11-14")
                    .status("Reviewing").date("2018-11-15")
                    .status("Done").date("2018-11-22")
                .eoT()
                .worklogs()
                    .at("2018-11-14").timeSpentInHours(1.0)
                    .at("2018-11-16").timeSpentInHours(2.0)
                    .at("2018-11-20").timeSpentInHours(4.0)
                .eoW()
            .eoI()
            .givenSubtask("I-3")
                .project("TASKB")
                .type("Backend Development")
                .withTransitions()
                    .status("Open").date("2018-11-10")
                    .status("To Do").date("2018-11-10")
                    .status("Doing").date("2018-11-12")
                    .status("To Review").date("2018-11-13")
                    .status("Reviewing").date("2018-11-14")
                    .status("Done").date("2018-11-16")
                .eoT()
                .worklogs()
                    .at("2018-11-12").timeSpentInHours(2.0)
                    .at("2018-11-15").timeSpentInHours(1.0)
                .eoW()
            .eoI()
            .givenSubtask("I-4")
                .project("TASKB")
                .type("Alpha Bug")
                .withTransitions()
                    .status("Open").date("2018-11-18")
                    .status("To Do").date("2018-11-18")
                    .status("Doing").date("2018-11-19")
                    .status("To Review").date("2018-11-21")
                    .status("Reviewing").date("2018-11-22")
                    .status("Done").date("2018-11-24")
                .eoT()
                .worklogs()
                    .at("2018-11-20").timeSpentInHours(2.0)
                    .at("2018-11-23").timeSpentInHours(2.0)
                .eoW()
            .eoI()
        .when()
            .appliesBehavior(generateDataSetFor("TASKB", KpiLevel.SUBTASKS, ZONE_ID))
        .then()
            .assertSize(2)
            .assertPoints()
                .hasDate("2018-11-11").hasStackName("Development").hasWorkInHours(3.0).and()
                .hasDate("2018-11-11").hasStackName("Review").hasNoHoursWorked();
    }

    @Test
    public void getDataSet_whenLevelFeature_thenHappyPath() {
        dsl().environment()
            .services()
                .projects()
                    .withKey("TASKB")
                        .startAt("2019-01-01")
                        .deliveredAt("2019-01-31")
                    .eoP()
                .eoPs()
            .eoS()
            .withJiraProperties()
                .withFeaturesStatusPriorityOrder("Done","Doing","To Do","Planning","To Plan","Open")
            .eoJp()
            .withKpiProperties()
                .atFeatureHierarchy("Planning")
                    .withChildrenType("Tech Analysis")
                .eoH()
                .atFeatureHierarchy("Doing")
                    .withChildrenType("Backend Development")
                .eoH()
            .eoKP()
            .withKpiProperties(
                    withTouchTimeSubtaskConfig()
            )
            .givenFeature("I-1")
                .project("TASKB")
                .type("Task")
                .withTransitions()
                    .status("Open").date("2019-01-02")
                    .status("To Plan").date("2019-01-02")
                    .status("Planning").date("2019-01-02")
                    .status("To Do").date("2019-01-02")
                    .status("Doing").date("2019-01-02")
                    .status("Done").date("2019-01-03")
                .eoT()
                .subtask("I-2")
                    .type("Tech Analysis")
                    .withTransitions()
                        .status("Open").date("2019-01-02")
                        .status("To Do").date("2019-01-02")
                        .status("Doing").date("2019-01-02")
                        .status("Done").date("2019-01-02")
                    .eoT()
                    .worklogs()
                        .at("2019-01-02").timeSpentInHours(8.0)
                    .eoW()
                .endOfSubtask()
                .subtask("I-3")
                    .type("Backend Development")
                    .withTransitions()
                        .status("Open").date("2019-01-02")
                        .status("To Do").date("2019-01-02")
                        .status("Doing").date("2019-01-03")
                        .status("Done").date("2019-01-03")
                    .eoT()
                    .worklogs()
                        .at("2019-01-03").timeSpentInHours(4.0)
                    .eoW()
                .endOfSubtask()
            .eoI()
        .when()
            .appliesBehavior(generateDataSetFor("TASKB", KpiLevel.FEATURES, ZONE_ID))
        .then()
            .assertSize(10)
            .assertPoints()
                .hasDate("2018-12-30").hasStackName("Planning").hasWorkInHours(8.0).and()
                .hasDate("2018-12-30").hasStackName("Doing").hasWorkInHours(4.0).and()
                .hasDate("2019-01-06").hasStackName("Planning").hasNoHoursWorked().and()
                .hasDate("2019-01-06").hasStackName("Doing").hasNoHoursWorked().and()
                .hasDate("2019-01-13").hasStackName("Planning").hasNoHoursWorked().and()
                .hasDate("2019-01-13").hasStackName("Doing").hasNoHoursWorked().and()
                .hasDate("2019-01-20").hasStackName("Planning").hasNoHoursWorked().and()
                .hasDate("2019-01-20").hasStackName("Doing").hasNoHoursWorked().and()
                .hasDate("2019-01-27").hasStackName("Planning").hasNoHoursWorked().and()
                .hasDate("2019-01-27").hasStackName("Doing").hasNoHoursWorked();
    }

    @Test
    public void getDataSet_whenNoProgressingStatusConfiguredAndRequestFeatureLevel_thenShouldReturnEmptyDataset() {
        dsl().environment()
            .services()
                .projects()
                    .withKey("TASKB")
                        .startAt("2019-01-01")
                        .deliveredAt("2019-01-31")
                    .eoP()
                .eoPs()
            .eoS()
            .withJiraProperties()
                .withFeaturesStatusPriorityOrder("Done","Doing","To Do","Planning","To Plan","Open")
            .eoJp()
            .withKpiProperties(
                    withTouchTimeSubtaskConfig()
                        .withNoProgressingStatusesConfigured()
            )
            .givenFeature("I-1")
                .project("TASKB")
                .type("Task")
                .withTransitions()
                    .status("Open").date("2019-01-02")
                    .status("To Plan").date("2019-01-02")
                    .status("Planning").date("2019-01-02")
                    .status("To Do").date("2019-01-02")
                    .status("Doing").date("2019-01-02")
                    .status("Done").date("2019-01-03")
                .eoT()
                .subtask("I-2")
                    .type("Tech Analysis")
                    .withTransitions()
                        .status("Open").date("2019-01-02")
                        .status("To Do").date("2019-01-02")
                        .status("Doing").date("2019-01-02")
                        .status("Done").date("2019-01-02")
                    .eoT()
                    .worklogs()
                        .at("2019-01-02").timeSpentInHours(8.0)
                    .eoW()
                .endOfSubtask()
                .subtask("I-3")
                    .type("Backend Development")
                    .withTransitions()
                        .status("Open").date("2019-01-02")
                        .status("To Do").date("2019-01-02")
                        .status("Doing").date("2019-01-03")
                        .status("Done").date("2019-01-03")
                    .eoT()
                    .worklogs()
                        .at("2019-01-03").timeSpentInHours(4.0)
                    .eoW()
                .endOfSubtask()
            .eoI()
        .when()
            .appliesBehavior(generateDataSetFor("TASKB", KpiLevel.FEATURES, ZONE_ID))
        .then()
            .assertSize(0);
    }

    @Test
    public void getDataSet_whenNoIssuesReturnedFromService_thenShouldReturnEmptyDataSet() {
        dsl().environment()
            .withJiraProperties()
                .withSubtaskStatusPriorityOrder("Done","Reviewing","To Review","Doing","To Do","Open")
            .eoJp()
            .withKpiProperties()
                .atFeatureHierarchy("Doing")
                    .withChildrenType("Backend Development")
                    .withChildrenType("Alpha Bug")
                .eoH()
            .eoKP()
            .withKpiProperties(
                withTouchTimeSubtaskConfig()
                    .withChartStack("Development")
                        .types("Backend Development")
                        .statuses("Doing")
                    .eoS()
                    .withChartStack("Review")
                        .statuses("Reviewing")
                    .eoS()
                .eoTTSC()
            )
            .services()
                .projects()
                    .withKey("TASKB")
                        .startAt("2018-11-12")
                        .deliveredAt("2018-11-26")
                    .eoP()
                .eoPs()
            .eoS()
        .when()
            .appliesBehavior(generateDataSetFor("TASKB", KpiLevel.SUBTASKS, ZONE_ID))
        .then()
            .assertSize(0);
    }

    @Test
    public void getDataSet_whenProjectRangeUnconfigured_thenShouldThrowProjectDatesNotConfigured() {
        dsl().environment()
            .services()
                .projects()
                    .withKey("TASKB")
                    .eoP()
                .eoPs()
            .eoS()
            .withJiraProperties()
                .withFeaturesStatusPriorityOrder("Done","Doing","To Do","Planning","To Plan","Open")
            .eoJp()
            .withKpiProperties()
                .atFeatureHierarchy("Planning")
                    .withChildrenType("Tech Analysis")
                .eoH()
                .atFeatureHierarchy("Doing")
                    .withChildrenType("Backend Development")
                .eoH()
            .eoKP()
            .givenFeature("I-1")
                .project("TASKB")
                .type("Task")
                .withTransitions()
                    .status("Open").date("2019-01-02")
                    .status("To Plan").date("2019-01-02")
                    .status("Planning").date("2019-01-02")
                    .status("To Do").date("2019-01-02")
                    .status("Doing").date("2019-01-02")
                    .status("Done").date("2019-01-03")
                .eoT()
                .subtask("I-2")
                    .type("Tech Analysis")
                    .withTransitions()
                        .status("Open").date("2019-01-02")
                        .status("To Do").date("2019-01-02")
                        .status("Doing").date("2019-01-02")
                        .status("Done").date("2019-01-02")
                    .eoT()
                    .worklogs()
                        .at("2019-01-02").timeSpentInHours(8.0)
                    .eoW()
                .endOfSubtask()
                .subtask("I-3")
                    .type("Backend Development")
                    .withTransitions()
                        .status("Open").date("2019-01-02")
                        .status("To Do").date("2019-01-02")
                        .status("Doing").date("2019-01-03")
                        .status("Done").date("2019-01-03")
                    .eoT()
                    .worklogs()
                        .at("2019-01-03").timeSpentInHours(4.0)
                    .eoW()
                .endOfSubtask()
            .eoI()
        .when()
            .expectExceptionFromBehavior(generateDataSetFor("TASKB", KpiLevel.FEATURES, ZONE_ID))
        .then()
            .isFromException(ProjectDatesNotConfiguredException.class);
    }

    @Test
    public void projectWithWrongRangeConfigured_noStartDate() {
        dsl().environment()
            .services()
                .projects()
                    .withKey("TASKB")
                        .deliveredAt("2019-01-15")
                    .eoP()
                .eoPs()
            .eoS()
            .withJiraProperties()
                .withFeaturesStatusPriorityOrder("Done","Doing","To Do","Planning","To Plan","Open")
            .eoJp()
            .withKpiProperties()
                .atFeatureHierarchy("Planning")
                    .withChildrenType("Tech Analysis")
                .eoH()
                .atFeatureHierarchy("Doing")
                    .withChildrenType("Backend Development")
                .eoH()
            .eoKP()
            .givenFeature("I-1")
                .project("TASKB")
                .type("Task")
                .withTransitions()
                    .status("Open").date("2019-01-02")
                    .status("To Plan").date("2019-01-02")
                    .status("Planning").date("2019-01-02")
                    .status("To Do").date("2019-01-02")
                    .status("Doing").date("2019-01-02")
                    .status("Done").date("2019-01-03")
                .eoT()
                .subtask("I-2")
                    .type("Tech Analysis")
                    .withTransitions()
                        .status("Open").date("2019-01-02")
                        .status("To Do").date("2019-01-02")
                        .status("Doing").date("2019-01-02")
                        .status("Done").date("2019-01-02")
                    .eoT()
                    .worklogs()
                        .at("2019-01-02").timeSpentInHours(8.0)
                    .eoW()
                .endOfSubtask()
                .subtask("I-3")
                    .type("Backend Development")
                    .withTransitions()
                        .status("Open").date("2019-01-02")
                        .status("To Do").date("2019-01-02")
                        .status("Doing").date("2019-01-03")
                        .status("Done").date("2019-01-03")
                    .eoT()
                    .worklogs()
                        .at("2019-01-03").timeSpentInHours(4.0)
                    .eoW()
                .endOfSubtask()
            .eoI()
        .when()
            .expectExceptionFromBehavior(generateDataSetFor("TASKB", KpiLevel.FEATURES, ZONE_ID))
        .then()
            .isFromException(ProjectDatesNotConfiguredException.class);
    }

    @Test
    public void projectWithWrongRangeConfigured_noDeliveryDate() {
        dsl().environment()
            .services()
                .projects()
                    .withKey("TASKB")
                        .startAt("2019-01-15")
                    .eoP()
                .eoPs()
            .eoS()
            .withJiraProperties()
                .withFeaturesStatusPriorityOrder("Done","Doing","To Do","Planning","To Plan","Open")
            .eoJp()
            .withKpiProperties()
                .atFeatureHierarchy("Planning")
                    .withChildrenType("Tech Analysis")
                .eoH()
                .atFeatureHierarchy("Doing")
                    .withChildrenType("Backend Development")
                .eoH()
            .eoKP()
            .givenFeature("I-1")
                .project("TASKB")
                .type("Task")
                .withTransitions()
                    .status("Open").date("2019-01-02")
                    .status("To Plan").date("2019-01-02")
                    .status("Planning").date("2019-01-02")
                    .status("To Do").date("2019-01-02")
                    .status("Doing").date("2019-01-02")
                    .status("Done").date("2019-01-03")
                .eoT()
                .subtask("I-2")
                    .type("Tech Analysis")
                    .withTransitions()
                        .status("Open").date("2019-01-02")
                        .status("To Do").date("2019-01-02")
                        .status("Doing").date("2019-01-02")
                        .status("Done").date("2019-01-02")
                    .eoT()
                    .worklogs()
                        .at("2019-01-02").timeSpentInHours(8.0)
                    .eoW()
                .endOfSubtask()
                .subtask("I-3")
                    .type("Backend Development")
                    .withTransitions()
                        .status("Open").date("2019-01-02")
                        .status("To Do").date("2019-01-02")
                        .status("Doing").date("2019-01-03")
                        .status("Done").date("2019-01-03")
                    .eoT()
                    .worklogs()
                        .at("2019-01-03").timeSpentInHours(4.0)
                    .eoW()
                .endOfSubtask()
            .eoI()
        .when()
            .expectExceptionFromBehavior(generateDataSetFor("TASKB", KpiLevel.FEATURES, ZONE_ID))
        .then()
            .isFromException(ProjectDatesNotConfiguredException.class);
    }

    private TouchTimeByWeekDataProviderBehavior generateDataSetFor(String projectKey, KpiLevel issueLevel, ZoneId timezone) {
        return new TouchTimeByWeekDataProviderBehavior(projectKey, issueLevel, timezone);
    }

    private DSLKpi dsl() {
        DSLKpi dsl = new DSLKpi();
        dsl.environment()
            .types()
                .addFeatures("Task")
                .addSubtasks("Backend Development", "Alpha Bug", "Tech Analysis")
            .eoT()
            .statuses()
                .withNotProgressingStatuses("Open", "To Plan", "To Do", "To Review", "Done")
                .withProgressingStatuses("Planning", "Doing", "Reviewing")
            .eoS();
        return dsl;
    }

    private class TouchTimeByWeekDataProviderBehavior implements DSLSimpleBehaviorWithAsserter<TouchTimeDataSetAsserter> {

        private String projectKey;
        private KpiLevel issueLevel;
        private ZoneId timezone;
        private TouchTimeChartByWeekDataSet dataset;

        public TouchTimeByWeekDataProviderBehavior(String projectKey, KpiLevel issueLevel, ZoneId timezone) {
            this.projectKey = projectKey;
            this.issueLevel = issueLevel;
            this.timezone = timezone;
        }

        @Override
        public void behave(KpiEnvironment environment) {
            KpiTouchTimeProperties kpiProperties = environment.getKPIProperties(KpiTouchTimeProperties.class);
            JiraProperties jiraProperties = environment.getJiraProperties();
            IssueKpiService issueKpiService = environment.services().issueKpi().getService();
            ProjectService projectService = environment.services().projects().getService();
            TouchTimeByWeekDataProvider subject = new TouchTimeByWeekDataProvider(issueKpiService, projectService, kpiProperties, jiraProperties);
            this.dataset = subject.getDataSet(projectKey, issueLevel, timezone);
        }

        @Override
        public TouchTimeDataSetAsserter then() {
            return new TouchTimeDataSetAsserter(dataset);
        }
    }

    private class TouchTimeDataSetAsserter {

        private TouchTimeChartByWeekDataSet dataset;

        public TouchTimeDataSetAsserter(TouchTimeChartByWeekDataSet dataset) {
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

            private TouchTimeChartByWeekDataPoint currentPoint;
            private Iterator<TouchTimeChartByWeekDataPoint> iterator;

            public TouchTimePointsAsserter(List<TouchTimeChartByWeekDataPoint> points) {
                iterator = points.iterator();
                currentPoint = iterator.next();
            }

            public TouchTimePointsAsserter hasNoHoursWorked() {
                return hasWorkInHours(0.0);
            }

            public TouchTimePointsAsserter hasStackName(String stackName) {
                assertThat(currentPoint.stackName, is(stackName));
                return this;
            }

            public TouchTimePointsAsserter hasDate(String date) {
                assertThat(currentPoint.date, is(parseDateTime(date, "00:00:00", ZONE_ID).toInstant()));
                return this;
            }

            public TouchTimePointsAsserter hasWorkInHours(double workInHours) {
                assertThat(currentPoint.effortInHours, is(closeTo(workInHours, 0.01)));
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
