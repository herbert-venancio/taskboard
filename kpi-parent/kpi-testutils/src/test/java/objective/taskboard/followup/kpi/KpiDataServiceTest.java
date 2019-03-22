package objective.taskboard.followup.kpi;

import static java.util.Collections.emptyList;
import static objective.taskboard.followup.kpi.KpiLevel.DEMAND;
import static objective.taskboard.followup.kpi.KpiLevel.FEATURES;
import static objective.taskboard.followup.kpi.KpiLevel.SUBTASKS;
import static objective.taskboard.followup.kpi.KpiLevel.UNMAPPED;

import java.time.ZoneId;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import objective.taskboard.followup.AnalyticsTransitionsDataRow;
import objective.taskboard.followup.AnalyticsTransitionsDataSet;
import objective.taskboard.followup.FollowUpData;
import objective.taskboard.followup.FollowUpSnapshot;
import objective.taskboard.followup.FollowUpSnapshotService;
import objective.taskboard.followup.kpi.enviroment.DSLKpi;
import objective.taskboard.followup.kpi.enviroment.DSLSimpleBehaviorWithAsserter;
import objective.taskboard.followup.kpi.enviroment.IssuesAsserter;
import objective.taskboard.followup.kpi.enviroment.KpiEnvironment;
import objective.taskboard.followup.kpi.enviroment.snapshot.GenerateSnapshot;

public class KpiDataServiceTest {

    @Test
    public void getFollowupSnapshot() {
        dsl()
            .environment()
                .givenDemand("I-1")
                    .project("PROJ")
                    .type("Demand")
                    .withTransitions()
                        .status("To Do").date("2017-09-25")
                        .status("Doing").noDate()
                        .status("Done").noDate()
                    .eoT()
                    .feature("I-2")
                        .type("Feature")
                        .withTransitions()
                            .status("To Do").date("2017-09-25")
                            .status("Doing").date("2017-09-26")
                            .status("Done").noDate()
                        .eoT()
                        .subtask("I-3")
                        .type("Subtask")
                        .withTransitions()
                            .status("To Do").date("2017-09-25")
                            .status("Doing").date("2017-09-26")
                            .status("Done").date("2017-09-27")
                        .eoT()
                .eoI()
            .when()
                .appliesBehavior(getSnapshot("PROJ"))
            .then()
                .analyticLevel(DEMAND)
                    .hasSize(1)
                    .hasIssue("I-1")
                .eoA()
                .analyticLevel(FEATURES)
                    .hasSize(1)
                    .hasIssue("I-2")
                .eoA()
                .analyticLevel(SUBTASKS)
                    .hasSize(1)
                    .hasIssue("I-3")
                .eoA();
    }


    @Test
    public void getIssues_currentState() {
        DSLKpi context = dsl()
        .environment()
            .givenFeature("I-1")
                .project("PROJ")
                .type("Task")
                .withTransitions()
                    .status("Open").date("2020-01-01")
                    .status("To Do").date("2020-01-02")
                    .status("Doing").date("2020-01-03")
                    .status("Done").noDate()
                .eoT()
                .subtask("I-2")
                    .type("Alpha")
                    .project("PROJ")
                    .withTransitions()
                        .status("Open").date("2020-01-01")
                        .status("To Do").date("2020-01-02")
                        .status("Doing").date("2020-01-03")
                        .status("Done").date("2020-01-04")
                    .eoT()
                    .worklogs()
                        .at("2020-01-03").timeSpentInHours(3.0)
                    .eoW()
                .endOfSubtask()
            .eoI()
        .eoE();
        context
            .when()
                .appliesBehavior(getIssuesFromCurrentStateForProjectAndLevel("PROJ", DEMAND))
            .then()
                .amountOfIssueIs(0);
        
        context
            .when()
                .appliesBehavior(getIssuesFromCurrentStateForProjectAndLevel("PROJ", FEATURES))
            .then()
                .amountOfIssueIs(1)
                .givenIssue("I-1")
                    .hasLevel(FEATURES)
                    .hasType("Task")
                    .atDate("2020-01-04").isOnStatus("Doing").eoDc()
                    .atStatus("Doing").hasTotalEffortInHours(3.0).eoSa()
                    .hasChild("I-2")
                .eoIA();
        context
            .when()
                .appliesBehavior(getIssuesFromCurrentStateForProjectAndLevel("PROJ", SUBTASKS))
            .then()
                .amountOfIssueIs(1)
                .givenIssue("I-2")
                    .hasLevel(SUBTASKS)
                    .hasType("Alpha")
                    .atDate("2020-01-04").isOnStatus("Done").eoDc()
                    .atStatus("To Do").hasNoEffort().eoSa()
                    .atStatus("Doing").hasTotalEffortInHours(3.0).eoSa()
                    .atStatus("Done").hasNoEffort().eoSa();
        context
            .when()
            .appliesBehavior(getIssuesFromCurrentStateForProjectAndLevel("PROJ", UNMAPPED))
            .then()
                .amountOfIssueIs(0);
    }
    
    private FollowupSnapshotBehavior getSnapshot(String projectKey) {
        return new FollowupSnapshotBehavior(projectKey);
    }

    private ServeIssuesFromCurrentState getIssuesFromCurrentStateForProjectAndLevel(String projectKey, KpiLevel level) {
        return new ServeIssuesFromCurrentState(projectKey, level);
    }

    private class ServeIssuesFromCurrentState implements DSLSimpleBehaviorWithAsserter<IssuesAsserter> {
        private String projectKey;
        private KpiLevel level;
        private IssuesAsserter asserter;
        
        public ServeIssuesFromCurrentState(String projectKey, KpiLevel level) {
            this.projectKey = projectKey;
            this.level = level;
        }

        @Override
        public void behave(KpiEnvironment environment) {
            IssueKpiService issueKpiService = environment.services().issueKpi().getService();
            FollowUpSnapshotService followupSnapshotService = environment.services().followupSnapshot().getService();
            
            KpiDataService subject = new KpiDataService(issueKpiService, followupSnapshotService);
            ZoneId timezone = environment.getTimezone();
            
            List<IssueKpi> issues = subject.getIssuesFromCurrentState(projectKey, timezone , level);
            this.asserter = new IssuesAsserter(issues, environment);
        }

        @Override
        public IssuesAsserter then() {
            return asserter;
        }
    }
    
    private class FollowupSnapshotBehavior implements DSLSimpleBehaviorWithAsserter<SnahpsotAsserter> {
        private SnahpsotAsserter asserter;
        private String projectKey;

        public FollowupSnapshotBehavior(String projectKey) {
            this.projectKey = projectKey;
        }

        @Override
        public void behave(KpiEnvironment environment) {

            GenerateSnapshot datasetFactory = new GenerateSnapshot(environment);
            ZoneId timezone = environment.getTimezone();
            
            environment.services().followupSnapshot().prepareForFactory(datasetFactory,projectKey, timezone);

            FollowUpSnapshotService followupSnapshotService = environment.services().followupSnapshot().getService();
            IssueKpiService issueKpiService = environment.services().issueKpi().getService();
            KpiDataService subject = new KpiDataService(issueKpiService, followupSnapshotService); 
            this.asserter = new SnahpsotAsserter(subject.getSnapshotFromCurrentState(timezone,projectKey));
        }

        @Override
        public SnahpsotAsserter then() {
            return this.asserter;
        }
    }
    
    private class SnahpsotAsserter {

        private Map<KpiLevel, AnalyticsTransitionsDataSet> analyticSets = new EnumMap<>(KpiLevel.class);
        private FollowUpData data;

        public SnahpsotAsserter(FollowUpSnapshot snapshot) {
            this.data = snapshot.getData();
            fillAnalyticsSets();
        }

        private void fillAnalyticsSets() {
            analyticSets.put(KpiLevel.DEMAND, data.analyticsTransitionsDsList.get(0));
            analyticSets.put(KpiLevel.FEATURES, data.analyticsTransitionsDsList.get(1));
            analyticSets.put(KpiLevel.SUBTASKS, data.analyticsTransitionsDsList.get(2));
            analyticSets.put(KpiLevel.UNMAPPED, new AnalyticsTransitionsDataSet("Unmapped", emptyList(), emptyList()));
        }

        public AnalyticAsserter analyticLevel(KpiLevel level) {
            return new AnalyticAsserter(analyticSets.get(level));
            
        }
        
        private class AnalyticAsserter {

            private AnalyticsTransitionsDataSet subject;

            private AnalyticAsserter(AnalyticsTransitionsDataSet subject) {
                this.subject = subject;
            }

            private AnalyticAsserter hasSize(int size) {
                Assertions.assertThat(subject.rows).hasSize(size);
                return this;
            }
            
            private AnalyticAsserter hasIssue(String issue) {
                Optional<AnalyticsTransitionsDataRow> issueRow = subject.rows.stream().filter(r -> issue.equalsIgnoreCase(r.issueKey)).findFirst();
                Assertions.assertThat(issueRow).as("Issue not found: %s",issue).isPresent();
                return this;
            }
            
            private SnahpsotAsserter eoA() {
                return SnahpsotAsserter.this;
            }
                        
        }
        
    }

    private DSLKpi dsl() {
        return new DSLKpi().environment()
                    .withJiraProperties()
                        .followUp()
                            .withExcludedStatuses("Open")
                        .eof()
                        .withDemandStatusPriorityOrder("Done","Doing","To Do")
                        .withFeaturesStatusPriorityOrder("Done","Doing","To Do")
                        .withSubtaskStatusPriorityOrder("Done","Doing","To Do")
                    .eoJp()
                    .withKpiProperties()
                        .atFeatureHierarchy("Doing")
                            .withChildrenType("Alpha")
                        .eoH()
                    .eoKP()
                    .types()
                        .addDemand("Demand")
                        .addFeatures("Task","OS","Feature")
                        .addSubtasks("Alpha","Subtask")
                    .eoT()
                    .statuses()
                        .withProgressingStatuses("Doing")
                        .withNotProgressingStatuses("Open", "To Do", "Done")
                    .eoS()
            .eoE();
    }

}
