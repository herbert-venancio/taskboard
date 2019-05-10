package objective.taskboard.followup.kpi.services;

import static java.util.Collections.emptyList;
import static objective.taskboard.followup.kpi.KpiLevel.DEMAND;
import static objective.taskboard.followup.kpi.KpiLevel.FEATURES;
import static objective.taskboard.followup.kpi.KpiLevel.SUBTASKS;
import static objective.taskboard.followup.kpi.KpiLevel.UNMAPPED;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.Mockito;

import objective.taskboard.followup.AnalyticsTransitionsDataRow;
import objective.taskboard.followup.AnalyticsTransitionsDataSet;
import objective.taskboard.followup.FollowUpData;
import objective.taskboard.followup.FollowUpSnapshot;
import objective.taskboard.followup.FollowUpSnapshotService;
import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.filters.KpiFilterService;
import objective.taskboard.followup.kpi.services.snapshot.SnapshotGenerator;
import objective.taskboard.jira.ProjectService;

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
                .calledSnapshotFromCurrentState()
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
    public void getFollowupSnapshot_passingDate() {
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
                .appliesBehavior(getSnapshot("PROJ").withDate("2017-09-25"))
            .then()
                .calledSnapshotWithDate("2017-09-25")
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
    public void getIssues_currentState_withDefaultFilters() {
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
                .appliesBehavior(getIssuesFromCurrentStateWithDefaultFilters("PROJ", DEMAND))
            .then()
                .amountOfIssueIs(0);
        
        context
            .when()
                .appliesBehavior(getIssuesFromCurrentStateWithDefaultFilters("PROJ", FEATURES))
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
                .appliesBehavior(getIssuesFromCurrentStateWithDefaultFilters("PROJ", SUBTASKS))
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
            .appliesBehavior(getIssuesFromCurrentStateWithDefaultFilters("PROJ", UNMAPPED))
            .then()
                .amountOfIssueIs(0);
    }
    

    @Test
    public void getIssuesFromCurrentState_filteringClosedIssues() {
        KpiEnvironment context = dsl().environment()
                .services()
                    .projects()
                        .withKey("PROJ")
                            .startAt("2020-01-06")
                            .deliveredAt("2020-01-10")
                        .eoP()
                    .eoPs()
                .eoS()
                .todayIs("2020-01-12")
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
                    .subtask("I-3")
                        .type("Alpha")
                        .project("PROJ")
                        .withTransitions()
                            .status("Open").date("2020-01-01")
                            .status("To Do").date("2020-01-02")
                            .status("Doing").date("2020-01-07")
                            .status("Done").noDate()
                        .eoT()
                    .endOfSubtask()
                    .subtask("I-4")
                        .type("Alpha")
                        .project("PROJ")
                        .withTransitions()
                            .status("Open").date("2020-01-01")
                            .status("To Do").date("2020-01-02")
                            .status("Doing").noDate()
                            .status("Done").noDate()
                        .eoT()
                        .worklogs()
                            .at("2020-01-09").timeSpentInHours(5.0)
                        .eoW()
                    .endOfSubtask()
                .eoI();
        
            context
                .when()
                    .appliesBehavior(getIssuesFromCurrentStateFilteringByProjectRange("PROJ", FEATURES))
                .then()
                    .amountOfIssueIs(1)
                    .givenIssue("I-1")
                        .hasLevel(FEATURES)
                        .hasType("Task")
                        .atDate("2020-01-12").isOnStatus("Doing").eoDc()
                        .atStatus("Doing").hasTotalEffortInHours(8.0).eoSa()
                        .hasChild("I-2")
                        .hasChild("I-3")
                        .hasChild("I-4");
            
            context
                .when()
                    .appliesBehavior(getIssuesFromCurrentStateFilteringByProjectRange("PROJ", SUBTASKS))
                .then()
                    .amountOfIssueIs(2)
                    .givenIssue("I-3")
                        .hasLevel(SUBTASKS)
                        .hasType("Alpha")
                        .atDate("2020-01-12").isOnStatus("Doing").eoDc()
                        .atStatus("To Do").hasNoEffort().eoSa()
                        .atStatus("Doing").hasNoEffort().eoSa()
                        .atStatus("Done").hasNoEffort().eoSa()
                    .eoIA()
                    .givenIssue("I-4")
                        .hasLevel(SUBTASKS)
                        .hasType("Alpha")
                        .atDate("2020-01-12").isOnStatus("To Do").eoDc()
                        .atStatus("To Do").hasNoEffort().eoSa()
                        .atStatus("Doing").hasTotalEffortInHours(5.0).eoSa()
                        .atStatus("Done").hasNoEffort().eoSa()
                    .eoIA();
    }
    
    @Test
    public void getIssuesFromCurrentState_filteringOpen() {
        KpiEnvironment context = dsl().environment()
                .services()
                    .projects()
                        .withKey("PROJ")
                            .startAt("2020-01-04")
                            .deliveredAt("2020-01-10")
                        .eoP()
                    .eoPs()
                .eoS()
                .todayIs("2020-01-12")
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
                    .subtask("I-3")
                        .type("Alpha")
                        .project("PROJ")
                        .withTransitions()
                            .status("Open").date("2020-01-01")
                            .status("To Do").date("2020-01-02")
                            .status("Doing").noDate()
                            .status("Done").noDate()
                        .eoT()
                    .endOfSubtask()
                    .subtask("I-4")
                        .type("Alpha")
                        .project("PROJ")
                        .withTransitions()
                            .status("Open").date("2020-01-01")
                            .status("To Do").date("2020-01-02")
                            .status("Doing").noDate()
                            .status("Done").noDate()
                        .eoT()
                        .worklogs()
                            .at("2020-01-03").timeSpentInHours(5.0)
                        .eoW()
                    .endOfSubtask()
                    .subtask("I-5")
                        .type("Alpha")
                        .project("PROJ")
                        .withTransitions()
                            .status("Open").date("2020-01-01")
                            .status("To Do").noDate()
                            .status("Doing").noDate()
                            .status("Done").noDate()
                        .eoT()
                    .endOfSubtask()
                .eoI();
        
            context
                .when()
                    .appliesBehavior(getIssuesFromCurrentStateFilteringByProjectRange("PROJ", FEATURES))
                .then()
                    .amountOfIssueIs(1)
                    .givenIssue("I-1")
                        .hasLevel(FEATURES)
                        .hasType("Task")
                        .atDate("2020-01-12").isOnStatus("Doing").eoDc()
                        .atStatus("Doing").hasTotalEffortInHours(8.0).eoSa()
                        .hasChild("I-2")
                        .hasChild("I-3")
                        .hasChild("I-4");
            
            context
                .when()
                    .appliesBehavior(getIssuesFromCurrentStateFilteringByProjectRange("PROJ", SUBTASKS))
                .then()
                    .amountOfIssueIs(2)
                    .givenIssue("I-2")
                        .hasLevel(SUBTASKS)
                        .hasType("Alpha")
                        .atDate("2020-01-12").isOnStatus("Done").eoDc()
                        .atStatus("To Do").hasNoEffort().eoSa()
                        .atStatus("Doing").hasTotalEffortInHours(3.0).eoSa()
                        .atStatus("Done").hasNoEffort().eoSa()
                    .eoIA()
                    .givenIssue("I-4")
                        .hasLevel(SUBTASKS)
                        .hasType("Alpha")
                        .atDate("2020-01-12").isOnStatus("To Do").eoDc()
                        .atStatus("To Do").hasNoEffort().eoSa()
                        .atStatus("Doing").hasTotalEffortInHours(5.0).eoSa()
                        .atStatus("Done").hasNoEffort().eoSa()
                    .eoIA();
    }
    
    @Test
    public void getIssuesFromCurrentState_allIssues() {
        dsl().environment()
                .services()
                    .projects()
                        .withKey("PROJ")
                            .startAt("2020-01-04")
                            .deliveredAt("2020-01-10")
                        .eoP()
                    .eoPs()
                .eoS()
                .todayIs("2020-01-12")
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
                    .subtask("I-3")
                        .type("Alpha")
                        .project("PROJ")
                        .withTransitions()
                            .status("Open").date("2020-01-01")
                            .status("To Do").date("2020-01-02")
                            .status("Doing").noDate()
                            .status("Done").noDate()
                        .eoT()
                    .endOfSubtask()
                    .subtask("I-4")
                        .type("Alpha")
                        .project("PROJ")
                        .withTransitions()
                            .status("Open").date("2020-01-01")
                            .status("To Do").date("2020-01-02")
                            .status("Doing").noDate()
                            .status("Done").noDate()
                        .eoT()
                        .worklogs()
                            .at("2020-01-03").timeSpentInHours(5.0)
                        .eoW()
                    .endOfSubtask()
                    .subtask("I-5")
                        .type("Alpha")
                        .project("PROJ")
                        .withTransitions()
                            .status("Open").date("2020-01-01")
                            .status("To Do").noDate()
                            .status("Doing").noDate()
                            .status("Done").noDate()
                        .eoT()
                    .endOfSubtask()
                .eoI()
                .when()
                    .appliesBehavior(getAllIssuesFromCurrrentState("PROJ"))
                .then()
                    .amountOfIssueIs(5)
                    .givenIssue("I-1")
                        .hasLevel(FEATURES)
                        .hasType("Task")
                        .hasLastTransitedStatus("Doing")
                        .hasChild("I-2").hasChild("I-3")
                        .hasChild("I-4").hasChild("I-5")
                    .eoIA()
                    .givenIssue("I-2")
                        .hasLevel(SUBTASKS)
                        .hasType("Alpha")
                        .hasLastTransitedStatus("Done")
                        .atStatus("To Do").hasNoEffort().eoSa()
                        .atStatus("Doing").hasTotalEffortInHours(3.0).eoSa()
                        .atStatus("Done").hasNoEffort().eoSa()
                    .eoIA()
                    .givenIssue("I-3")
                        .hasLevel(SUBTASKS)
                        .hasType("Alpha")
                        .hasLastTransitedStatus("To Do")
                        .atStatus("Doing").hasNoEffort().eoSa()
                        .atStatus("To Do").hasNoEffort().eoSa()
                        .atStatus("Done").hasNoEffort().eoSa()
                    .eoIA()
                    .givenIssue("I-4")
                        .hasLevel(SUBTASKS)
                        .hasType("Alpha")
                        .hasLastTransitedStatus("To Do")
                        .atStatus("To Do").hasNoEffort().eoSa()
                        .atStatus("Doing").hasTotalEffortInHours(5.0).eoSa()
                        .atStatus("Done").hasNoEffort().eoSa()
                    .eoIA()
                    .givenIssue("I-5")
                        .hasLevel(SUBTASKS)
                        .hasType("Alpha")
                        .hasLastTransitedStatus("Open")
                        .atStatus("To Do").hasNoEffort().eoSa()
                        .atStatus("Doing").hasNoEffort().eoSa()
                        .atStatus("Done").hasNoEffort().eoSa()
                    .eoIA();
    }
    
    private ServeAllIssuesFromCurrentState getAllIssuesFromCurrrentState(String projectKey) {
        return new ServeAllIssuesFromCurrentState(projectKey);
    }

    private FollowupSnapshotBehavior getSnapshot(String projectKey) {
        return new FollowupSnapshotBehavior(projectKey);
    }

    private ServeIssuesFromCurrentState getIssuesFromCurrentStateWithDefaultFilters(String projectKey, KpiLevel level) {
        return new ServeIssuesWithDefaultFilter(projectKey, level);
    }
    
    private ServeIssuesFromCurrentState getIssuesFromCurrentStateFilteringByProjectRange(String projectKey, KpiLevel level) {
        return new ServeIssuesFilteringByProject(projectKey, level);
    }
    
    
    private abstract class ServeIssuesFromCurrentState implements DSLSimpleBehaviorWithAsserter<IssuesAsserter> {
        protected String projectKey;
        private IssuesAsserter asserter;
        
        public ServeIssuesFromCurrentState(String projectKey) {
            this.projectKey = projectKey;
        }

        @Override
        public void behave(KpiEnvironment environment) {
            
            KpiDataService subject = getService(environment);
            ZoneId timezone = environment.getTimezone();
            
            List<IssueKpi> issues = getIssues(subject, timezone);
            this.asserter = new IssuesAsserter(issues, environment);
        }
        
        protected KpiDataService getService(KpiEnvironment environment) {
            IssueKpiService issueKpiService = environment.services().issueKpi().getService();
            FollowUpSnapshotService followupSnapshotService = environment.services().followupSnapshot().getService();
            ProjectService projectService = environment.services().projects().getService();
            KpiFilterService filterService = environment.services().filters().getService();
            
            return new KpiDataService(issueKpiService, followupSnapshotService, projectService, filterService);
        }

        protected abstract List<IssueKpi> getIssues(KpiDataService subject, ZoneId timezone);

        @Override
        public IssuesAsserter then() {
            return asserter;
        }
    }
    
    private class ServeAllIssuesFromCurrentState extends ServeIssuesFromCurrentState {

        public ServeAllIssuesFromCurrentState(String projectKey) {
            super(projectKey);
        }

        @Override
        protected List<IssueKpi> getIssues(KpiDataService subject, ZoneId timezone) {
            return subject.getAllIssuesFromCurrentState(projectKey, timezone);
        }
        
    }

    private class ServeIssuesWithDefaultFilter extends ServeIssuesFromCurrentState {

        private KpiLevel level;

        private ServeIssuesWithDefaultFilter(String projectKey, KpiLevel level) {
            super(projectKey);
            this.level = level;
        }

        @Override
        protected List<IssueKpi> getIssues(KpiDataService subject, ZoneId timezone) {
            return subject.getIssuesFromCurrentStateWithDefaultFilters(projectKey, timezone,level);
        }

    }
    
    private class ServeIssuesFilteringByProject extends ServeIssuesFromCurrentState {
    
        private KpiLevel level;

        private ServeIssuesFilteringByProject(String projectKey, KpiLevel level) {
                super(projectKey);
                this.level = level;
        }
    
        @Override
        protected List<IssueKpi> getIssues(KpiDataService subject, ZoneId timezone) {
            return subject.getIssuesFromCurrentProjectRange(projectKey, timezone, level);
        }
        
    }

    private class FollowupSnapshotBehavior implements DSLSimpleBehaviorWithAsserter<SnapshotAsserter> {
        private SnapshotAsserter asserter;
        private String projectKey;
        private Optional<LocalDate> date = Optional.empty();

        public FollowupSnapshotBehavior(String projectKey) {
            this.projectKey = projectKey;
        }

        private FollowupSnapshotBehavior withDate(String date) {
            this.date = Optional.of(LocalDate.parse(date));
            return this;
        }

        @Override
        public void behave(KpiEnvironment environment) {

            SnapshotGenerator snapshoGenerator = new SnapshotGenerator(environment);
            ZoneId timezone = environment.getTimezone();
            
            environment.services().followupSnapshot().prepareForGenerator(snapshoGenerator,projectKey, timezone);

            FollowUpSnapshotService followupSnapshotService = environment.services().followupSnapshot().getService();
            IssueKpiService issueKpiService = environment.services().issueKpi().getService();
            ProjectService projectService = environment.services().projects().getService();
            KpiFilterService filterService = environment.services().filters().getService();

            KpiDataService subject = new KpiDataService(issueKpiService, followupSnapshotService, projectService, filterService); 
            this.asserter = new SnapshotAsserter(projectKey,timezone, environment, getSnapshot(timezone, subject));
        }

        private FollowUpSnapshot getSnapshot(ZoneId timezone, KpiDataService subject) {
            if(date.isPresent())
                return subject.getSnapshot(projectKey, timezone, date);
            return subject.getSnapshotFromCurrentState(projectKey,timezone);
        }

        @Override
        public SnapshotAsserter then() {
            return this.asserter;
        }
    }
    
    private class SnapshotAsserter {

        private Map<KpiLevel, AnalyticsTransitionsDataSet> analyticSets = new EnumMap<>(KpiLevel.class);
        private FollowUpData data;
        private KpiEnvironment environment;
        private String projectKey;
        private ZoneId timezone;

        public SnapshotAsserter(String projectKey, ZoneId timezone, KpiEnvironment environment,FollowUpSnapshot snapshot) {
            this.projectKey = projectKey;
            this.timezone = timezone;
            this.environment = environment;
            this.data = snapshot.getData();
            fillAnalyticsSets();
        }

        public SnapshotAsserter calledSnapshotWithDate(String date) {
            FollowUpSnapshotService service = environment.services().followupSnapshot().getService();
            Mockito.verify(service,times(1)).get(eq(Optional.of(LocalDate.parse(date))), eq(timezone), eq(projectKey));
            Mockito.verify(service,times(0)).getFromCurrentState(timezone,projectKey);
            return this;
        }
        
        public SnapshotAsserter calledSnapshotFromCurrentState() {
            FollowUpSnapshotService service = environment.services().followupSnapshot().getService();
            Mockito.verify(service,times(0)).get(Mockito.any(), Mockito.eq(timezone), Mockito.eq(projectKey));
            Mockito.verify(service,times(1)).getFromCurrentState(timezone,projectKey);
            return this;
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
            
            private SnapshotAsserter eoA() {
                return SnapshotAsserter.this;
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
