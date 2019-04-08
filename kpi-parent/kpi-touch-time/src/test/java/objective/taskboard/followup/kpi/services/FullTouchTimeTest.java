package objective.taskboard.followup.kpi.services;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.time.ZoneId;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.filters.KpiLevelFilter;
import objective.taskboard.followup.kpi.properties.KPIProperties;
import objective.taskboard.followup.kpi.services.DSLKpi;
import objective.taskboard.followup.kpi.services.DSLSimpleBehaviorWithAsserter;
import objective.taskboard.followup.kpi.services.IssueKpiService;
import objective.taskboard.followup.kpi.services.KpiEnvironment;
import objective.taskboard.followup.kpi.services.MockedServices;
import objective.taskboard.followup.kpi.transformer.IssueKpiDataItemAdapterFactory;
import objective.taskboard.issueBuffer.IssueBufferService;
import objective.taskboard.jira.properties.JiraProperties;
import objective.taskboard.utils.Clock;
import objective.taskboard.utils.DateTimeUtils;

@RunWith(MockitoJUnitRunner.class)
public class FullTouchTimeTest {

    private static final ZoneId ZONE_ID = ZoneId.systemDefault();

    @Test
    public void fullTest() {

        DSLKpi dsl = dsl();

        dsl.environment()
            .todayIs("2018-02-02")
            .services()
                .projects()
                    .withKey("PROJ")
                        .startAt("2018-01-01")
                        .deliveredAt("2018-02-02")
                    .eoP()
                .eoPs()
            .eoS()
            .givenDemand("PROJ-01")
                .type("Demand")
                .project("PROJ")
                .withTransitions()
                    .status("Open").date("2018-01-01")
                    .status("To Do").date("2018-01-05")
                    .status("Doing").date("2018-01-09")
                    .status("To UAT").date("2018-01-12")
                    .status("UATing").date("2018-01-15")
                    .status("Done").date("2018-01-30")
                    .status("Cancelled").date("2018-02-02")
                .eoT()
                .subtask("PROJ-06")
                    .type("UAT")
                    .project("PROJ")
                    .withTransitions()
                        .status("Open").date("2018-01-17")
                        .status("To Do").date("2018-01-18")
                        .status("Doing").date("2018-01-20")
                        .status("To Review").date("2018-01-25")
                        .status("Reviewing").date("2018-01-25")
                        .status("To Merge").noDate()
                        .status("Merging").noDate()
                        .status("Done").date("2018-02-02")
                        .status("Cancelled").noDate()
                    .eoT()
                    .worklogs()
                        .at("2018-01-20").timeSpentInHours(5.0)
                        .at("2018-01-25").timeSpentInHours(2.0)
                    .eoW();

        prepareBugs(dsl);
        prepareTask(dsl);
        prepareFeature(dsl);
        prepareContinuous(dsl);

        dsl
            .when()
                .appliesBehavior(buildAllIssues("PROJ",ZONE_ID))
            .then()
                .at(KpiLevel.DEMAND)
                    .hasSize(1)
                    .issue("PROJ-01")
                        .at("Doing").hasEffort(189.0)
                        .at("UATing").hasEffort(7.0)
                    .eoI()
                .eoL()
                .at(KpiLevel.FEATURES)
                    .hasSize(4)
                     .issue("PROJ-02")
                         .at("Planning").hasEffort(0.0)
                         .at("Developing").hasEffort(12.0)
                         .at("Internal QAing").hasEffort(0.0)
                         .at("QAing").hasEffort(0.0)
                     .eoI()
                     .issue("PROJ-03")
                         .at("Planning").hasEffort(0.0)
                         .at("Developing").hasEffort(12.0)
                         .at("Internal QAing").hasEffort(0.0)
                         .at("QAing").hasEffort(0.0)
                     .eoI()
                         .issue("PROJ-04")
                         .at("Planning").hasEffort(0.0)
                         .at("Developing").hasEffort(15.0)
                         .at("Internal QAing").hasEffort(0.0)
                         .at("QAing").hasEffort(0.0)
                     .eoI()
                         .issue("PROJ-05")
                         .at("Planning").hasEffort(13.0)
                         .at("Developing").hasEffort(51.0)
                         .at("Internal QAing").hasEffort(74.0)
                         .at("QAing").hasEffort(12.0)
                     .eoI()
                 .eoL()
                .at(KpiLevel.SUBTASKS)
                    .hasSize(14)
                    .issue("PROJ-06")
                        .at("Doing").hasEffort(5.0)
                        .at("Reviewing").hasEffort(2.0)
                        .at("Merging").hasEffort(0.0)
                    .eoI()
                    .issue("PROJ-07")
                        .at("Doing").hasEffort(7.0)
                        .at("Reviewing").hasEffort(4.0)
                        .at("Merging").hasEffort(1.0)
                    .eoI()
                    .issue("PROJ-08")
                        .at("Doing").hasEffort(10.0)
                        .at("Reviewing").hasEffort(2.0)
                        .at("Merging").hasEffort(0.0)
                    .eoI()
                    .issue("PROJ-09")
                        .at("Doing").hasEffort(10.0)
                        .at("Reviewing").hasEffort(2.0)
                        .at("Merging").hasEffort(3.0)
                    .eoI()
                    .issue("PROJ-10")
                        .at("Doing").hasEffort(6.0)
                        .at("Reviewing").hasEffort(0.0)
                        .at("Merging").hasEffort(0.0)
                    .eoI()
                    .issue("PROJ-11")
                        .at("Doing").hasEffort(5.0)
                        .at("Reviewing").hasEffort(0.0)
                        .at("Merging").hasEffort(2.0)
                    .eoI()
                    .issue("PROJ-12")
                        .at("Doing").hasEffort(10.0)
                        .at("Reviewing").hasEffort(4.0)
                        .at("Merging").hasEffort(1.0)
                    .eoI()
                    .issue("PROJ-13")
                        .at("Doing").hasEffort(8.0)
                        .at("Reviewing").hasEffort(0.0)
                        .at("Merging").hasEffort(2.0)
                    .eoI()
                    .issue("PROJ-14")
                        .at("Doing").hasEffort(12.0)
                        .at("Reviewing").hasEffort(0.0)
                        .at("Merging").hasEffort(6.0)
                    .eoI()
                    .issue("PROJ-15")
                        .at("Doing").hasEffort(20.0)
                        .at("Reviewing").hasEffort(5.0)
                        .at("Merging").hasEffort(1.0)
                    .eoI()
                    .issue("PROJ-16")
                        .at("Doing").hasEffort(6.0)
                        .at("Reviewing").hasEffort(1.0)
                        .at("Merging").hasEffort(1.0)
                    .eoI()
                    .issue("PROJ-17")
                        .at("Doing").hasEffort(25.0)
                        .at("Reviewing").hasEffort(0.0)
                        .at("Merging").hasEffort(0.0)
                    .eoI()
                    .issue("PROJ-18")
                        .at("Doing").hasEffort(21.0)
                        .at("Reviewing").hasEffort(2.0)
                        .at("Merging").hasEffort(0.0)
                    .eoI()
                    .issue("PROJ-19")
                        .at("Doing").hasEffort(12.0)
                        .at("Reviewing").hasEffort(0.0)
                        .at("Merging").hasEffort(0.0)
                    .eoI();
    }

    private void prepareContinuous(DSLKpi dsl) {
        dsl
        .environment()
            .givenDemand("PROJ-01")
                .feature("PROJ-20")
                    .type("Continuous")
                    .project("PROJ")
                    .unmappedLevel()
                    .withTransitions()
                        .status("Open").date("2018-01-01")
                        .status("Doing").date("2018-01-03")
                        .status("Done").date("2018-01-10")
                    .eoT()
                    .subtask("PROJ-21")
                        .type("Subtask Continuous")
                        .project("PROJ")
                        .unmappedLevel()
                        .withTransitions()
                            .status("Open").date("2018-01-01")
                            .status("Doing").date("2018-01-03")
                            .status("Done").date("2018-01-10")
                        .eoT()
                        .worklogs()
                            .at("2018-01-05").timeSpentInHours(8.0)
                        .eoW();
    }

    private void prepareFeature(DSLKpi dsl) {
        dsl
        .environment()
            .givenDemand("PROJ-01")
                .feature("PROJ-05")
                    .type("Feature")
                    .project("PROJ")
                    .withTransitions()
                        .status("Open").date("2018-01-02")
                        .status("To Plan").date("2018-01-02")
                        .status("Planning").date("2018-01-02")
                        .status("To Dev").date("2018-01-03")
                        .status("Developing").date("2018-01-03")
                        .status("To Internal QA").date("2018-01-10")
                        .status("Internal QAing").date("2018-01-10")
                        .status("To Deploy").date("2018-01-15")
                        .status("To QA").date("2018-01-15")
                        .status("QAing").date("2018-01-17")
                        .status("Done").date("2018-01-20")
                        .status("Cancelled").noDate()
                    .eoT()
                    .subtask("PROJ-10")
                        .type("Tech Planning")
                        .project("PROJ")
                        .withTransitions()
                            .status("Open").date("2018-01-03")
                            .status("To Do").date("2018-01-03")
                            .status("Doing").date("2018-01-03")
                            .status("To Review").noDate()
                            .status("Reviewing").noDate()
                            .status("To Merge").noDate()
                            .status("Merging").noDate()
                            .status("Done").date("2018-01-03")
                            .status("Cancelled").noDate()
                        .eoT()
                        .worklogs()
                            .at("2018-01-02").timeSpentInHours(6.0)
                        .eoW()
                    .endOfSubtask()
                    .subtask("PROJ-11")
                        .type("Feature Planning")
                        .project("PROJ")
                        .withTransitions()
                            .status("Open").date("2018-01-03")
                            .status("To Do").date("2018-01-03")
                            .status("Doing").date("2018-01-03")
                            .status("To Review").noDate()
                            .status("Reviewing").noDate()
                            .status("To Merge").noDate()
                            .status("Merging").noDate()
                            .status("Done").date("2018-01-03")
                            .status("Cancelled").noDate()
                        .eoT()
                        .worklogs()
                            .at("2018-01-02").timeSpentInHours(5.0)
                            .at("2018-01-04").timeSpentInHours(2.0)
                        .eoW()
                    .endOfSubtask()
                    .subtask("PROJ-12")
                        .type("Backend Development")
                        .project("PROJ")
                        .withTransitions()
                            .status("Open").date("2018-01-03")
                            .status("To Do").date("2018-01-03")
                            .status("Doing").date("2018-01-05")
                            .status("To Review").date("2018-01-07")
                            .status("Reviewing").date("2018-01-09")
                            .status("To Merge").date("2018-01-10")
                            .status("Merging").date("2018-01-10")
                            .status("Done").date("2018-01-10")
                            .status("Cancelled").noDate()
                        .eoT()
                        .worklogs()
                            .at("2018-01-05").timeSpentInHours(10.0)
                            .at("2018-01-09").timeSpentInHours(4.0)
                            .at("2018-01-10").timeSpentInHours(1.0)
                        .eoW()
                    .endOfSubtask()
                    .subtask("PROJ-13")
                        .type("Frontend Development")
                        .project("PROJ")
                        .withTransitions()
                            .status("Open").date("2018-01-05")
                            .status("To Do").date("2018-01-06")
                            .status("Doing").date("2018-01-07")
                            .status("To Review").date("2018-01-10")
                            .status("Reviewing").date("2018-01-10")
                            .status("To Merge").date("2018-01-10")
                            .status("Merging").date("2018-01-10")
                            .status("Done").date("2018-01-11")
                            .status("Cancelled").noDate()
                        .eoT()
                        .worklogs()
                            .at("2018-01-07").timeSpentInHours(2.0)
                            .at("2018-01-08").timeSpentInHours(6.0)
                            .at("2018-01-10").timeSpentInHours(1.0)
                            .at("2018-01-12").timeSpentInHours(1.0)
                        .eoW()
                    .endOfSubtask()
                    .subtask("PROJ-14")
                        .type("UX")
                        .project("PROJ")
                        .withTransitions()
                            .status("Open").date("2018-01-07")
                            .status("To Do").date("2018-01-07")
                            .status("Doing").date("2018-01-08")
                            .status("To Review").date("2018-01-10")
                            .status("Reviewing").date("2018-01-12")
                            .status("To Merge").date("2018-01-12")
                            .status("Merging").date("2018-01-12")
                            .status("Done").date("2018-01-12")
                            .status("Cancelled").noDate()
                        .eoT()
                        .worklogs()
                            .at("2018-01-08").timeSpentInHours(12.0)
                            .at("2018-01-12").timeSpentInHours(6.0)
                        .eoW()
                    .endOfSubtask()
                    .subtask("PROJ-15")
                        .type("Alpha Test")
                        .project("PROJ")
                        .withTransitions()
                            .status("Open").date("2018-01-10")
                            .status("To Do").date("2018-01-10")
                            .status("Doing").date("2018-01-12")
                            .status("To Review").date("2018-01-14")
                            .status("Reviewing").date("2018-01-14")
                            .status("To Merge").date("2018-01-16")
                            .status("Merging").date("2018-01-16")
                            .status("Done").date("2018-01-16")
                            .status("Cancelled").noDate()
                        .eoT()
                        .worklogs()
                            .at("2018-01-12").timeSpentInHours(20.0)
                            .at("2018-01-14").timeSpentInHours(5.0)
                            .at("2018-01-16").timeSpentInHours(1.0)
                        .eoW()
                    .endOfSubtask()
                    .subtask("PROJ-16")
                        .type("Alpha Bug")
                        .project("PROJ")
                        .withTransitions()
                            .status("Open").date("2018-01-09")
                            .status("To Do").date("2018-01-09")
                            .status("Doing").date("2018-01-11")
                            .status("To Review").date("2018-01-13")
                            .status("Reviewing").date("2018-01-13")
                            .status("To Merge").date("2018-01-16")
                            .status("Merging").date("2018-01-16")
                            .status("Done").date("2018-01-17")
                            .status("Cancelled").noDate()
                        .eoT()
                        .worklogs()
                            .at("2018-01-11").timeSpentInHours(6.0)
                            .at("2018-01-13").timeSpentInHours(1.0)
                            .at("2018-01-16").timeSpentInHours(1.0)
                        .eoW()
                    .endOfSubtask()
                    .subtask("PROJ-17")
                        .type("Functional Test")
                        .project("PROJ")
                        .withTransitions()
                            .status("Open").date("2018-01-10")
                            .status("To Do").date("2018-01-10")
                            .status("Doing").date("2018-01-11")
                            .status("To Review").noDate()
                            .status("Reviewing").noDate()
                            .status("To Merge").noDate()
                            .status("Merging").noDate()
                            .status("Done").date("2018-01-15")
                            .status("Cancelled").noDate()
                        .eoT()
                        .worklogs()
                            .at("2018-01-11").timeSpentInHours(25.0)
                        .eoW()
                    .endOfSubtask()
                    .subtask("PROJ-18")
                        .type("Functional Review")
                        .project("PROJ")
                        .withTransitions()
                            .status("Open").date("2018-01-10")
                            .status("To Do").date("2018-01-10")
                            .status("Doing").date("2018-01-16")
                            .status("To Review").date("2018-01-18")
                            .status("Reviewing").date("2018-01-20")
                            .status("To Merge").noDate()
                            .status("Merging").noDate()
                            .status("Done").noDate()
                            .status("Cancelled").noDate()
                        .eoT()
                        .worklogs()
                            .at("2018-01-16").timeSpentInHours(21.0)
                            .at("2018-01-20").timeSpentInHours(2.0)
                        .eoW()
                    .endOfSubtask()
                    .subtask("PROJ-19")
                        .type("QA")
                        .project("PROJ")
                        .withTransitions()
                            .status("Open").date("2018-01-15")
                            .status("To Do").date("2018-01-17")
                            .status("Doing").date("2018-01-17")
                            .status("To Review").noDate()
                            .status("Reviewing").noDate()
                            .status("To Merge").noDate()
                            .status("Merging").noDate()
                            .status("Done").date("2018-01-20")
                            .status("Cancelled").noDate()
                        .eoT()
                        .worklogs()
                            .at("2018-01-17").timeSpentInHours(10.0)
                            .at("2018-01-18").timeSpentInHours(2.0)
                        .eoW()
                    .endOfSubtask();
    }

    private void prepareTask(DSLKpi dsl) {
        dsl.environment()
            .givenDemand("PROJ-01")
                .feature("PROJ-04")
                    .type("Task")
                    .project("PROJ")
                    .withTransitions()
                        .status("Open").date("2018-01-03")
                        .status("To Plan").date("2018-01-03")
                        .status("Planning").date("2018-01-04")
                        .status("To Dev").date("2018-01-04")
                        .status("Developing").date("2018-01-04")
                        .status("To Internal QA").noDate()
                        .status("Internal QAing").noDate()
                        .status("To Deploy").noDate()
                        .status("To QA").noDate()
                        .status("QAing").noDate()
                        .status("Done").date("2018-01-05")
                        .status("Cancelled").noDate()
                    .eoT()
                    .subtask("PROJ-09")
                        .type("Subtask")
                        .project("PROJ")
                        .withTransitions()
                            .status("Open").date("2018-01-04")
                            .status("To Do").date("2018-01-04")
                            .status("Doing").date("2018-01-04")
                            .status("To Review").date("2018-01-05")
                            .status("Reviewing").date("2018-01-05")
                            .status("To Merge").date("2018-01-05")
                            .status("Merging").date("2018-01-06")
                            .status("Done").date("2018-01-06")
                            .status("Cancelled").noDate()
                        .eoT()
                        .worklogs()
                            .at("2018-01-04").timeSpentInHours(10.0)
                            .at("2018-01-05").timeSpentInHours(2.0)
                            .at("2018-01-06").timeSpentInHours(3.0)
                        .eoW()
                    .endOfSubtask()
                .endOfFeature();
    }

    private void prepareBugs(DSLKpi dsl) {
        dsl.environment()
            .givenDemand("PROJ-01")
                .feature("PROJ-02")
                    .type("Bug")
                    .project("PROJ")
                    .withTransitions()
                        .status("Open").date("2018-01-01")
                        .status("To Plan").date("2018-01-01")
                        .status("Planning").noDate()
                        .status("To Dev").date("2018-01-02")
                        .status("Developing").date("2018-01-03")
                        .status("To Internal QA").date("2018-01-04")
                        .status("Internal QAing").date("2018-01-04")
                        .status("To Deploy").date("2018-01-05")
                        .status("To QA").date("2018-01-05")
                        .status("QAing").date("2018-01-06")
                        .status("Done").date("2018-01-07")
                        .status("Cancelled").noDate()
                    .eoT()
                    .subtask("PROJ-07")
                        .type("Frontend Development")
                        .project("PROJ")
                        .withTransitions()
                            .status("Open").date("2018-01-02")
                            .status("To Do").date("2018-01-02")
                            .status("Doing").date("2018-01-03")
                            .status("To Review").date("2018-01-03")
                            .status("Reviewing").date("2018-01-03")
                            .status("To Merge").date("2018-01-04")
                            .status("Merging").date("2018-01-04")
                            .status("Done").date("2018-01-07")
                            .status("Cancelled").noDate()
                        .eoT()
                        .worklogs()
                            .at("2018-01-02").timeSpentInHours(7.0)
                            .at("2018-01-03").timeSpentInHours(4.0)
                            .at("2018-01-05").timeSpentInHours(1.0)
                        .eoW()
                    .endOfSubtask()
                .endOfFeature()
                .feature("PROJ-03")
                    .type("Bug")
                    .project("PROJ")
                        .withTransitions()
                            .status("Open").date("2018-01-04")
                            .status("To Plan").noDate()
                            .status("Planning").noDate()
                            .status("To Dev").date("2018-01-04")
                            .status("Developing").noDate()
                            .status("To Internal QA").noDate()
                            .status("Internal QAing").noDate()
                            .status("To Deploy").noDate()
                            .status("To QA").noDate()
                            .status("QAing").noDate()
                            .status("Done").noDate()
                            .status("Cancelled").date("2018-01-09")
                        .eoT()
                        .subtask("PROJ-08")
                            .type("Backend Development")
                            .project("PROJ")
                            .withTransitions()
                                .status("Open").date("2018-01-04")
                                .status("To Do").date("2018-01-04")
                                .status("Doing").date("2018-01-05")
                                .status("To Review").date("2018-01-05")
                                .status("Reviewing").date("2018-01-06")
                                .status("To Merge").noDate()
                                .status("Merging").noDate()
                                .status("Done").noDate()
                                .status("Cancelled").date("2018-01-09")
                            .eoT()
                        .worklogs()
                            .at("2018-01-05").timeSpentInHours(10.0)
                            .at("2018-01-06").timeSpentInHours(2.0)
                        .eoW()
                    .endOfSubtask()
                .endOfFeature();
    }

    private DSLKpi dsl() {
        DSLKpi dsl = new DSLKpi();

        dsl.environment()
                .types()
                    .addDemand("Demand")
                    .addFeatures("Feature", "Bug", "Task","Continuous")
                    .addSubtasks("UAT", "Frontend Development", "Backend Development")
                    .addSubtasks("Subtask", "Tech Planning", "Feature Planning")
                    .addSubtasks("UX", "Alpha Test", "Alpha Bug")
                    .addSubtasks("Functional Test", "Functional Review", "QA")
                    .addSubtasks("Subtask Continuous")
                .eoT()
                .withKpiProperties()
                    .atFeatureHierarchy("QAing")
                        .withChildrenType("QA")
                    .eoH()
                    .atFeatureHierarchy("Internal QAing")
                        .withChildrenType("Alpha Test")
                        .withChildrenType("Functional Test")
                        .withChildrenType("Functional Review")
                        .eoH()
                    .atFeatureHierarchy("Developing")
                        .withChildrenType("Alpha Bug")
                        .withChildrenType("Subtask")
                        .withChildrenType("Backend Development")
                        .withChildrenType("Frontend Development")
                        .withChildrenType("UX")
                    .eoH()
                    .atFeatureHierarchy("Planning")
                        .withChildrenType("Feature Planning")
                        .withChildrenType("Tech Planning")
                    .eoH()
                    .atDemandHierarchy("UATing")
                        .withChildrenType("UAT")
                    .eoH()
                    .atDemandHierarchy("Doing")
                        .withChildrenStatus("Planning")
                        .withChildrenStatus("To Dev")
                        .withChildrenStatus("Developing")
                        .withChildrenStatus("To Internal QA")
                        .withChildrenStatus("Internal QAing")
                        .withChildrenStatus("QAing")
                    .eoH()
                .eoKP()
                .statuses()
                    .withProgressingStatuses("Doing", "UATing", "Planning")
                    .withProgressingStatuses("Developing", "Internal QAing", "QAing")
                    .withProgressingStatuses("Reviewing", "Merging")
                    .withNotProgressingStatuses("Open", "To Do", "To UAT")
                    .withNotProgressingStatuses("Done", "Cancelled")
                    .withNotProgressingStatuses("To Plan", "To Dev", "To Internal QA", "To Deploy")
                    .withNotProgressingStatuses("To QA", "To Review", "To Merge")
                .eoS();
        return dsl;
    }

    private AllIssuesBehavior buildAllIssues(String project, ZoneId zoneId) {
        return new AllIssuesBehavior(project,zoneId);
    }

    private class AllIssuesBehavior implements DSLSimpleBehaviorWithAsserter<EffortAsserter>{

        private String project;
        private ZoneId zoneId;
        private Map<KpiLevel,List<IssueKpi>> allIssues = new EnumMap<>(KpiLevel.class);

        private AllIssuesBehavior(String project, ZoneId zoneId) {
            this.project = project;
            this.zoneId = zoneId;
        }

        @Override
        public void behave(KpiEnvironment environment) {

            environment.withTimezone(zoneId);
            JiraProperties jiraProperties = environment.getJiraProperties();
            KPIProperties kpiProperties = environment.getKPIProperties();
            Clock clock = environment.getClock();

            MockedServices services = environment.services();

            IssueBufferService issueBufferService = services.issuesBuffer().getService();
            IssueKpiDataItemAdapterFactory factory = services.itemAdapterFactory().getComponent();

            IssueKpiService subject = new IssueKpiService(issueBufferService, jiraProperties, kpiProperties, clock, factory);
            List<IssueKpi> issuesFromCurrentState = subject.getIssuesFromCurrentState(project, zoneId);

            for (KpiLevel level : KpiLevel.values()) {
                List<IssueKpi> issuesFromLevel = issuesFromCurrentState.stream()
                        .filter(new KpiLevelFilter(level)).collect(Collectors.toList());
                allIssues.put(level, issuesFromLevel);
            }
        }

        @Override
        public EffortAsserter then() {
            return new EffortAsserter(allIssues);
        }

    }

    private class EffortAsserter {

        private Map<KpiLevel, LevelAsserter> allLevelAsserter = new EnumMap<>(KpiLevel.class);

        public EffortAsserter(Map<KpiLevel, List<IssueKpi>> allIssues) {
            allIssues.entrySet().forEach( entry -> allLevelAsserter.put(entry.getKey(), new LevelAsserter(entry.getValue())));
        }

        public LevelAsserter at(KpiLevel level) {
            return allLevelAsserter.get(level);
        }

        private class LevelAsserter {

            private List<IssueKpi> issues;

            public LevelAsserter(List<IssueKpi> issues) {
                this.issues = issues;
            }

            public LevelAsserter hasSize(int size) {
                assertThat(issues.size(),is(size));
                return this;
            }

            public IssueEffortAsserter issue(String pkey) {
                Optional<IssueKpi> issue = issues.stream().filter(i -> pkey.equals(i.getIssueKey())).findFirst();
                if(!issue.isPresent())
                    Assert.fail(String.format("Issue %s not found", pkey));
                return new IssueEffortAsserter(issue.get());
            }

            public EffortAsserter eoL() {
                return EffortAsserter.this;
            }

            private class IssueEffortAsserter {

                private IssueKpi issueKpi;
                private String status;

                public IssueEffortAsserter(IssueKpi issueKpi) {
                    this.issueKpi = issueKpi;
                }

                public IssueEffortAsserter at(String status) {
                    this.status = status;
                    return this;
                }

                public IssueEffortAsserter hasEffort(double hours) {
                    assertThat(issueKpi.getEffort(status),is(DateTimeUtils.hoursToSeconds(hours)));
                    return this;
                }

                public LevelAsserter eoI() {
                    return LevelAsserter.this;
                }

            }

        }
    }
}
