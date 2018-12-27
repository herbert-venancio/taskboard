package objective.taskboard.followup.kpi;

import org.junit.Test;

import objective.taskboard.followup.kpi.enviroment.DSLBehavior;
import objective.taskboard.followup.kpi.enviroment.DSLKpi;
import objective.taskboard.followup.kpi.enviroment.DSLKpi.BehaviorFactory.IssueBehavior;
import objective.taskboard.followup.kpi.enviroment.IssueKpiMocker;
import objective.taskboard.followup.kpi.enviroment.KpiPropertiesMocker;

public class FeatureTouchTimeTest {

    private DSLWrapper dslWrapper = dsl();

    @Test
    public void happyDay() {
        withProperties()
            .atFeatureHierarchy("QAing")
                .withChildrenType("QA")
            .eoH()
            .atFeatureHierarchy("Internal QAing")
                .withChildrenType("Alpha Test")
                .withChildrenType("Functional Test")
                .withChildrenType("Feature Review")
            .eoH()
            .atFeatureHierarchy("Developing")
                .withChildrenType("Alpha Bug")
                .withChildrenType("Sub-Task")
                .withChildrenType("Backend Development")
                .withChildrenType("Frontend Development")
                .withChildrenType("UX")
            .eoH()
            .atFeatureHierarchy("Planning")
                .withChildrenType("Feature Planning")
                .withChildrenType("Tech Planning");

        withFeature("PROJ-01")
            .withSubtask("Feature Planning").withWorklog(2.0).eoS()
            .withSubtask("Tech Planning").withWorklog(3.0).eoS()
            .withSubtask("Alpha Bug").withWorklog(4.0).eoS()
            .withSubtask("Sub-Task").withWorklog(5.0).eoS()
            .withSubtask("Backend Development").withWorklog(6.0).eoS()
            .withSubtask("Frontend Development").withWorklog(7.0).eoS()
            .withSubtask("UX").withWorklog(8.0).eoS()
            .withSubtask("Alpha Test").withWorklog(9.0).eoS()
            .withSubtask("Functional Test").withWorklog(10.0).eoS()
            .withSubtask("Feature Review").withWorklog(11.5).eoS()
            .withSubtask("QA").withWorklog(12.0);

        when("PROJ-01")
            .appliesBehavior(distributeWorklogs())
        .then()
            .assertThat()
                .issueKpi("PROJ-01")
                    .atStatus("Open").hasTotalEffortInHours(0.0).eoSa()
                    .atStatus("To Plan").hasTotalEffortInHours(0.0).eoSa()
                    .atStatus("Planning").hasTotalEffortInHours(5.0).eoSa()
                    .atStatus("To Dev").hasTotalEffortInHours(0.0).eoSa()
                    .atStatus("Developing").hasTotalEffortInHours(30.0).eoSa()
                    .atStatus("To Internal QA").hasTotalEffortInHours(0.0).eoSa()
                    .atStatus("Internal QAing").hasTotalEffortInHours(30.5).eoSa()
                    .atStatus("To Deploy").hasTotalEffortInHours(0.0).eoSa()
                    .atStatus("To QA").hasTotalEffortInHours(0.0).eoSa()
                    .atStatus("QAing").hasTotalEffortInHours(12.0).eoSa()
                    .atStatus("Done").hasTotalEffortInHours(0.0).eoSa()
                    .atStatus("Cancelled").hasTotalEffortInHours(0.0).eoSa();
    }

    @Test
    public void variousSubtasksStatuses() {
        withProperties()
            .atFeatureHierarchy("QAing")
                .withChildrenType("QA")
            .eoH()
            .atFeatureHierarchy("Internal QAing")
                .withChildrenType("Alpha Test")
                .withChildrenType("Functional Test")
                .withChildrenType("Feature Review")
            .eoH()
            .atFeatureHierarchy("Developing")
                .withChildrenType("Alpha Bug")
                .withChildrenType("Sub-Task")
                .withChildrenType("Backend Development")
                .withChildrenType("Frontend Development")
                .withChildrenType("UX")
            .eoH()
            .atFeatureHierarchy("Planning")
                .withChildrenType("Feature Planning")
                .withChildrenType("Tech Planning");

        withFeature("PROJ-01")
            .withSubtaskOpen("Feature Planning").withWorklog(1.0).eoS()
            .withSubtaskClosedSameDayStarted("Alpha Bug").withWorklog(1.0).eoS()
            .withSubtaskSkippingQueueStatus("Backend Development")
                .worklogAt("2020-01-04").withHours(1.0)
            .eoS()
            .withSubtaskWithTwoProgressingStatusAtSameDay("Alpha Test").withWorklog(1.0);

        when("PROJ-01")
            .appliesBehavior(distributeWorklogs())
        .then()
            .assertThat()
                .issueKpi("PROJ-01")
                    .atStatus("Open").hasTotalEffortInHours(0.0).eoSa()
                    .atStatus("To Plan").hasTotalEffortInHours(0.0).eoSa()
                    .atStatus("Planning").hasTotalEffortInHours(1.0).eoSa()
                    .atStatus("To Dev").hasTotalEffortInHours(0.0).eoSa()
                    .atStatus("Developing").hasTotalEffortInHours(2.0).eoSa()
                    .atStatus("To Internal QA").hasTotalEffortInHours(0.0).eoSa()
                    .atStatus("Internal QAing").hasTotalEffortInHours(1.0).eoSa()
                    .atStatus("To Deploy").hasTotalEffortInHours(0.0).eoSa()
                    .atStatus("To QA").hasTotalEffortInHours(0.0).eoSa()
                    .atStatus("QAing").hasTotalEffortInHours(0.0).eoSa()
                    .atStatus("Done").hasTotalEffortInHours(0.0).eoSa()
                    .atStatus("Cancelled").hasTotalEffortInHours(0.0).eoSa();
    }

    @Test
    public void multipleWorklogs() {
        withProperties()
            .atFeatureHierarchy("QAing")
                .withChildrenType("QA")
            .eoH()
            .atFeatureHierarchy("Internal QAing")
                .withChildrenType("Alpha Test")
                .withChildrenType("Functional Test")
                .withChildrenType("Feature Review")
            .eoH()
            .atFeatureHierarchy("Developing")
                .withChildrenType("Alpha Bug")
                .withChildrenType("Sub-Task")
                .withChildrenType("Backend Development")
                .withChildrenType("Frontend Development")
                .withChildrenType("UX")
            .eoH()
            .atFeatureHierarchy("Planning")
                .withChildrenType("Feature Planning")
                .withChildrenType("Tech Planning");

        withFeature("PROJ-01")
            .withSubtaskOpen("Feature Planning")
            .worklogAt("2020-01-03").withHours(1.0)
            .worklogAt("2020-01-06").withHours(3.0);

        when("PROJ-01")
        .appliesBehavior(distributeWorklogs())
        .then()
            .assertThat()
                .issueKpi("PROJ-01")
                    .atStatus("Open").hasTotalEffortInHours(0.0).eoSa()
                    .atStatus("To Plan").hasTotalEffortInHours(0.0).eoSa()
                    .atStatus("Planning").hasTotalEffortInHours(4.0).eoSa()
                    .atStatus("To Dev").hasTotalEffortInHours(0.0).eoSa()
                    .atStatus("Developing").hasTotalEffortInHours(0.0).eoSa()
                    .atStatus("To Internal QA").hasTotalEffortInHours(0.0).eoSa()
                    .atStatus("Internal QAing").hasTotalEffortInHours(0.0).eoSa()
                    .atStatus("To Deploy").hasTotalEffortInHours(0.0).eoSa()
                    .atStatus("To QA").hasTotalEffortInHours(0.0).eoSa()
                    .atStatus("QAing").hasTotalEffortInHours(0.0).eoSa()
                    .atStatus("Done").hasTotalEffortInHours(0.0).eoSa()
                    .atStatus("Cancelled").hasTotalEffortInHours(0.0).eoSa();
    }

    private DSLBehavior<IssueKpi> distributeWorklogs() {

        return (environment, subject) ->
                ChildrenWorklogDistributor.distributeWorklogs(environment.getKPIProperties().getFeaturesHierarchy(), subject);
    }

    private KpiPropertiesMocker withProperties() {
        return dslWrapper.getKpiPropertiesMocker();
    }

    private IssueBehavior when(String pKey) {
        return dslWrapper.getBehavior(pKey);
    }

    private DSLWrapper withFeature(String pKey) {
        return dslWrapper.prepareToCreateSubtask(pKey);
    }

    private DSLWrapper dsl() {
        return new DSLWrapper()
                    .configureTypes()
                    .configureStatuses();
    }

    private class DSLWrapper {
        private DSLKpi dsl = new DSLKpi();
        private int subtaskNumber = 2;
        private String currentFather;

        public SubtaskBuilder withSubtask(String type) {
            return buildSubtask(type).simpleWorklow();
        }

        public KpiPropertiesMocker getKpiPropertiesMocker() {
            return dsl.environment().withKpiProperties();
        }

        public SubtaskBuilder withSubtaskWithTwoProgressingStatusAtSameDay(String type) {
            return buildSubtask(type).sameProgressingDay();
        }

        public SubtaskBuilder withSubtaskSkippingQueueStatus(String type) {
            return buildSubtask(type).skippingQueue();
        }

        public SubtaskBuilder withSubtaskClosedSameDayStarted(String type) {
            return buildSubtask(type).straightToClose();
        }

        private SubtaskBuilder buildSubtask(String type) {
            return new SubtaskBuilder(currentFather,getPKey(),type);
        }

        public SubtaskBuilder withSubtaskOpen(String type) {
            return buildSubtask(type).openWorkflow();
        }

        public IssueBehavior getBehavior(String pKey) {
            return dsl.when().givenIssueKpi(pKey);
        }

        public DSLWrapper prepareToCreateSubtask(String pKey) {
            currentFather = pKey;
            givenFeature(pKey)
                .type("Feature")
                .withTransitions()
                    .status("Open").date("2020-01-01")
                    .status("To Plan").date("2020-01-02")
                    .status("Planning").date("2020-01-03")
                    .status("To Dev").date("2020-01-04")
                    .status("Developing").date("2020-01-05")
                    .status("To Internal QA").date("2020-01-06")
                    .status("Internal QAing").date("2020-01-07")
                    .status("To Deploy").date("2020-01-08")
                    .status("To QA").date("2020-01-09")
                    .status("QAing").date("2020-01-10")
                    .status("Done").date("2020-01-11")
                    .status("Cancelled").noDate();
            return this;
        }

        private String getPKey() {
            return String.format("PROJ-%d", subtaskNumber++);
        }

        public DSLWrapper configureTypes() {
            dsl.environment()
                .withDemandType("Demand")
                .withFeatureType("Feature")
                .withSubtaskType("QA").withSubtaskType("UAT").withSubtaskType("Alpha Test").withSubtaskType("Functional Test")
                .withSubtaskType("Feature Review").withSubtaskType("Alpha Bug").withSubtaskType("Sub-Task").withSubtaskType("Backend Development")
                .withSubtaskType("Frontend Development").withSubtaskType("UX").withSubtaskType("Feature Planning").withSubtaskType("Tech Planning");
            return this;
        }

        public IssueKpiMocker givenFeature(String pkey) {
            return dsl.environment().givenIssue(pkey).isFeature();
        }

        public DSLWrapper configureStatuses() {
            dsl.environment()
                .statuses()
                    .withNotProgressingStatuses("Open","To Plan","To Dev")
                    .withNotProgressingStatuses("To Internal QA","To Deploy","To QA")
                    .withNotProgressingStatuses("Done","Cancelled","To Do")
                    .withNotProgressingStatuses("To Review","To Merge")
                    .withProgressingStatuses("Planning","Developing","Internal QAing")
                    .withProgressingStatuses("QAing","Doing","Reviewing","Merging");
            return this;
        }

        private class SubtaskBuilder {
            private IssueKpiMocker subtask;
            private String worklogDate;

            public SubtaskBuilder(String father, String key, String type) {
                this.subtask = dsl.environment()
                            .givenIssue(father)
                            .subtask(key)
                            .type(type);
            }

            public SubtaskBuilder sameProgressingDay() {
                subtask
                    .withTransitions()
                        .status("Open").date("2020-01-01")
                        .status("To Do").noDate()
                        .status("Doing").date("2020-01-03")
                        .status("To Review").noDate()
                        .status("Reviewing").date("2020-01-03")
                        .status("To Merge").noDate()
                        .status("Merging").noDate()
                        .status("Done").date("2020-01-05")
                        .status("Cancelled");
                return this;
            }

            public SubtaskBuilder withHours(double timeInHours) {
                subtask.worklogs()
                    .at(worklogDate).timeSpentInHours(timeInHours);
                return this;
            }

            public SubtaskBuilder worklogAt(String date) {
                worklogDate = date;
                return this;
            }

            public SubtaskBuilder skippingQueue() {
                subtask
                    .withTransitions()
                        .status("Open").date("2020-01-01")
                        .status("To Do").noDate()
                        .status("Doing").date("2020-01-03")
                        .status("To Review").noDate()
                        .status("Reviewing").date("2020-01-04")
                        .status("To Merge").noDate()
                        .status("Merging").noDate()
                        .status("Done").date("2020-01-05")
                        .status("Cancelled").noDate();
                return this;
            }

            public SubtaskBuilder straightToClose() {
                subtask
                    .withTransitions()
                        .status("Open").date("2020-01-01")
                        .status("To Do").noDate()
                        .status("Doing").date("2020-01-03")
                        .status("To Review").noDate()
                        .status("Reviewing").noDate()
                        .status("To Merge").noDate()
                        .status("Merging").noDate()
                        .status("Done").date("2020-01-03")
                        .status("Cancelled").noDate();
                return this;
            }

            public SubtaskBuilder openWorkflow() {
                subtask
                    .withTransitions()
                        .status("Open").date("2020-01-01")
                        .status("To Do").noDate()
                        .status("Doing").noDate()
                        .status("To Review").noDate()
                        .status("Reviewing").noDate()
                        .status("To Merge").noDate()
                        .status("Merging").noDate()
                        .status("Done").noDate()
                        .status("Cancelled");
                return this;
            }

            public DSLWrapper eoS() {
                return DSLWrapper.this;
            }

            public SubtaskBuilder withWorklog(double timeSpent) {
                subtask.worklogs()
                    .at("2020-01-03").timeSpentInHours(timeSpent);
                return this;
            }

            public SubtaskBuilder simpleWorklow() {
                subtask
                    .withTransitions()
                        .status("Open").date("2020-01-01")
                        .status("To Do").date("2020-01-02")
                        .status("Doing").date("2020-01-03")
                        .status("To Review").date("2020-01-04")
                        .status("Reviewing").date("2020-01-05")
                        .status("To Merge").date("2020-01-06")
                        .status("Merging").date("2020-01-07")
                        .status("Done").date("2020-01-11")
                        .status("Cancelled").noDate()
                    .eoT();

                return this;
            }
        }

    }

}
