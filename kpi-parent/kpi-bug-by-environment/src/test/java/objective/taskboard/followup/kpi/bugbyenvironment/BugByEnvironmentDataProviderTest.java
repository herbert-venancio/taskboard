package objective.taskboard.followup.kpi.bugbyenvironment;

import static objective.taskboard.followup.kpi.properties.KpiBugByEnvironmentMocker.withBugTypes;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Range;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import objective.taskboard.followup.ProjectDatesNotConfiguredException;
import objective.taskboard.followup.kpi.properties.KpiBugByEnvironmentProperties;
import objective.taskboard.followup.kpi.services.DSLKpi;
import objective.taskboard.followup.kpi.services.DSLKpi.BehaviorFactory;
import objective.taskboard.followup.kpi.services.DSLSimpleBehaviorWithAsserter;
import objective.taskboard.followup.kpi.services.KpiDataService;
import objective.taskboard.followup.kpi.services.KpiEnvironment;
import objective.taskboard.jira.MetadataService;
import objective.taskboard.jira.ProjectService;
import objective.taskboard.utils.RangeUtils;

public class BugByEnvironmentDataProviderTest {

    @Test
    public void givenProjectRange_withCompleteThreeWeeks_allBugsAppears() {
        given()
            .preConfiguredBugs()
            .projectWithRangeOf("3 Weeks Configuration") //x2
        .when()
            .appliesBehavior(requestBugByEnvironmentData())
        .then()
            .totalOfPointsIs(12)
            .givenPointsOfWeekStartingOn("2019-03-31")
                .hasTotalOfPoints(4)
                .at("Alpha").hasValue(1l)
                .at("Production").hasValue(3l)
                .at("Bug").hasValue(2l)
                .at("Alpha Bug").hasValue(2l)
            .eoW()
            .givenPointsOfWeekStartingOn("2019-04-07")
                .hasTotalOfPoints(4)
                .at("Alpha").hasValue(1l)
                .at("Production").hasValue(3l)
                .at("Bug").hasValue(1l)
                .at("Alpha Bug").hasValue(4l)
            .eoW()
            .givenPointsOfWeekStartingOn("2019-04-14")
                .hasTotalOfPoints(4)
                .at("Alpha").hasValue(2l)
                .at("Production").hasValue(3l)
                .at("Bug").hasValue(1l)
                .at("Alpha Bug").hasValue(2l)
            .eoW();
    }
    
    @Test
    public void givenProjectRange_fromMidleWeekOne_toCompleteWeekThree_thenAllBugsShouldReturn() {
        given()
            .preConfiguredBugs()
            .projectWithRangeOf("3 Weeks Configuration - Starting middle Week 1") //x3
        .when()
            .appliesBehavior(requestBugByEnvironmentData())
        .then()
            .totalOfPointsIs(12)
            .givenPointsOfWeekStartingOn("2019-03-31")
                .hasTotalOfPoints(4)
                .at("Alpha").hasValue(1l)
                .at("Production").hasValue(3l)
                .at("Bug").hasValue(2l)
                .at("Alpha Bug").hasValue(2l)
            .eoW()
            .givenPointsOfWeekStartingOn("2019-04-07")
                .hasTotalOfPoints(4)
                .at("Alpha").hasValue(1l)
                .at("Production").hasValue(3l)
                .at("Bug").hasValue(1l)
                .at("Alpha Bug").hasValue(4l)
            .eoW()
            .givenPointsOfWeekStartingOn("2019-04-14")
                .hasTotalOfPoints(4)
                .at("Alpha").hasValue(2l)
                .at("Production").hasValue(3l)
                .at("Bug").hasValue(1l)
                .at("Alpha Bug").hasValue(2l)
            .eoW();
    }
    
    @Test
    public void givenProjectRange_fromStartWeekOne_toMiddleWeekThree_thenAllBugsShouldReturn() {
        given()
            .preConfiguredBugs()
            .projectWithRangeOf("3 Weeks Configuration - Finishing middle Week 3") //x4
        .when()
            .appliesBehavior(requestBugByEnvironmentData())
        .then()
            .totalOfPointsIs(12)
            .givenPointsOfWeekStartingOn("2019-03-31")
                .hasTotalOfPoints(4)
                .at("Alpha").hasValue(1l)
                .at("Production").hasValue(3l)
                .at("Bug").hasValue(2l)
                .at("Alpha Bug").hasValue(2l)
            .eoW()
            .givenPointsOfWeekStartingOn("2019-04-07")
                .hasTotalOfPoints(4)
                .at("Alpha").hasValue(1l)
                .at("Production").hasValue(3l)
                .at("Bug").hasValue(1l)
                .at("Alpha Bug").hasValue(4l)
            .eoW()
            .givenPointsOfWeekStartingOn("2019-04-14")
                .hasTotalOfPoints(4)
                .at("Alpha").hasValue(2l)
                .at("Production").hasValue(3l)
                .at("Bug").hasValue(1l)
                .at("Alpha Bug").hasValue(2l)
            .eoW();
    }
    
    @Test
    public void givenProjectRange_fromStartWeekOne_toEndWeekTwo_thenBugsOpenedOnWeekThreeShouldBeFiltered() {
        given()
            .preConfiguredBugs()
            .projectWithRangeOf("2 Weeks Configuration") //x5
        .when()
            .appliesBehavior(requestBugByEnvironmentData())
        .then()
            .totalOfPointsIs(8)
            .givenPointsOfWeekStartingOn("2019-03-31")
                .hasTotalOfPoints(4)
                .at("Alpha").hasValue(1l)
                .at("Production").hasValue(3l)
                .at("Bug").hasValue(2l)
                .at("Alpha Bug").hasValue(2l)
            .eoW()
            .givenPointsOfWeekStartingOn("2019-04-07")
                .hasTotalOfPoints(4)
                .at("Alpha").hasValue(1l)
                .at("Production").hasValue(3l)
                .at("Bug").hasValue(1l)
                .at("Alpha Bug").hasValue(4l)
            .eoW();
    }

    @Test
    public void givenProjectRange_fromStartWeekOne_toMiddleWeekTwo_thenBugsOpenedOnWeekThreeShouldBeFiltered() {
        given()
            .preConfiguredBugs()
            .projectWithRangeOf("2 Weeks Configuration - Finishing middle Week 2") //x6
        .when()
            .appliesBehavior(requestBugByEnvironmentData())
        .then()
            .totalOfPointsIs(8)
            .givenPointsOfWeekStartingOn("2019-03-31")
                .hasTotalOfPoints(4)
                .at("Alpha").hasValue(1l)
                .at("Production").hasValue(3l)
                .at("Bug").hasValue(2l)
                .at("Alpha Bug").hasValue(2l)
            .eoW()
            .givenPointsOfWeekStartingOn("2019-04-07")
                .hasTotalOfPoints(4)
                .at("Alpha").hasValue(1l)
                .at("Production").hasValue(3l)
                .at("Bug").hasValue(1l)
                .at("Alpha Bug").hasValue(4l)
            .eoW();
    }
    
    @Test
    public void givenProjectRange_startAndFinishingOnWeekTwo_thenOnlyBugsOpenedAtWeekTwoShouldAppear() {
        given()
            .preConfiguredBugs()
            .projectWithRangeOf("Start and Finishing middle Week 2") //x7
        .when()
            .appliesBehavior(requestBugByEnvironmentData())
        .then()
            .totalOfPointsIs(4)
            .givenPointsOfWeekStartingOn("2019-04-07")
                .hasTotalOfPoints(4)
                .at("Alpha").hasValue(1l)
                .at("Production").hasValue(3l)
                .at("Bug").hasValue(1l)
                .at("Alpha Bug").hasValue(4l)
            .eoW();
    }
    
    @Test
    public void givenProjectRange_fullWeekThree_thenOnlyBugsOpenedAtWeekThreeShouldAppear() {
        given()
            .preConfiguredBugs()
            .projectWithRangeOf("Full Week 3") //x8
        .when()
            .appliesBehavior(requestBugByEnvironmentData())
        .then()
            .totalOfPointsIs(4)
            .givenPointsOfWeekStartingOn("2019-04-14")
                .hasTotalOfPoints(4)
                .at("Alpha").hasValue(2l)
                .at("Production").hasValue(3l)
                .at("Bug").hasValue(1l)
                .at("Alpha Bug").hasValue(2l)
            .eoW();
    }
    
    @Test
    public void givenProjectRange_startingAlmostEndOfWeekOne_finishingAlmostStartWeekThree_allBugsAppears() {
        given()
            .preConfiguredBugs()
            .projectWithRangeOf("Starting almost end of Week 1- Finishing early on Week 3") //x9
        .when()
            .appliesBehavior(requestBugByEnvironmentData())
        .then()
            .totalOfPointsIs(12)
            .givenPointsOfWeekStartingOn("2019-03-31")
                .hasTotalOfPoints(4)
                .at("Alpha").hasValue(1l)
                .at("Production").hasValue(3l)
                .at("Bug").hasValue(2l)
                .at("Alpha Bug").hasValue(2l)
            .eoW()
            .givenPointsOfWeekStartingOn("2019-04-07")
                .hasTotalOfPoints(4)
                .at("Alpha").hasValue(1l)
                .at("Production").hasValue(3l)
                .at("Bug").hasValue(1l)
                .at("Alpha Bug").hasValue(4l)
            .eoW()
            .givenPointsOfWeekStartingOn("2019-04-14")
                .hasTotalOfPoints(4)
                .at("Alpha").hasValue(2l)
                .at("Production").hasValue(3l)
                .at("Bug").hasValue(1l)
                .at("Alpha Bug").hasValue(2l)
            .eoW();
    }
    
    @Test
    public void givenNoConfiguredProject_thenThrowsException() {
        given()
            .preConfiguredBugs()
            .withNoProjectRange()
        .when()
            .expectExceptionFromBehavior(requestBugByEnvironmentData())
        .then()
            .isFromException(ProjectDatesNotConfiguredException.class);
    }
    
    @Test
    public void givenProjectRange_noEndOfRangeConfigured_thenThrowsException() {
        given()
            .preConfiguredBugs()
            .withOpenEndProjectRange()
        .when()
            .expectExceptionFromBehavior(requestBugByEnvironmentData())
        .then()
            .isFromException(ProjectDatesNotConfiguredException.class);
    }
    
    @Test
    public void givenProjectRange_noStartOfRangeConfigured_thenThrowsException() {
        given()
            .preConfiguredBugs()
            .withOpenStartProjectRange()
        .when()
            .expectExceptionFromBehavior(requestBugByEnvironmentData())
        .then()
            .isFromException(ProjectDatesNotConfiguredException.class);
    }
    
    private ProvideBugsCalculated requestBugByEnvironmentData() {
        return new ProvideBugsCalculated();
    }

    private InnerContext given() {
        return new InnerContext();
    }

    private class ProvideBugsCalculated implements DSLSimpleBehaviorWithAsserter<BugByEnvrionmentDataPointsAsserter>{

        private BugByEnvrionmentDataPointsAsserter asserter;
        
        @Override
        public void behave(KpiEnvironment environment) {
            
            KpiDataService kpiDataService = environment.services().kpiDataService().getService();
            MetadataService metadataService = environment.services().metadata().getService();
            KpiBugByEnvironmentProperties properties = environment.getKPIProperties(KpiBugByEnvironmentProperties.class);
            ProjectService projectService = environment.services().projects().getService();
            
            BugByEnvironmentDataProvider subject = new BugByEnvironmentDataProvider(kpiDataService,metadataService,properties,projectService);
            List<BugByEnvironmentDataPoint> dataPoints = subject.getDataSet("PROJ", environment.getTimezone());
            
            this.asserter = new BugByEnvrionmentDataPointsAsserter(environment.getTimezone(),dataPoints);
        }

        @Override
        public BugByEnvrionmentDataPointsAsserter then() {
            return this.asserter;
        }
        
    }
    
    private class BugByEnvrionmentDataPointsAsserter {

        private List<BugByEnvironmentDataPoint> dataPoints;
        private ZoneId timezone;

        private BugByEnvrionmentDataPointsAsserter(ZoneId timezone, List<BugByEnvironmentDataPoint> dataPoints) {
            this.timezone = timezone;
            this.dataPoints = dataPoints;
        }

        private WeekPoints givenPointsOfWeekStartingOn(String startOfWeek) {
            List<BugByEnvironmentDataPoint> points = dataPoints.stream()
                                                        .filter(dp -> filterFromWeek(dp,startOfWeek))
                                                        .collect(Collectors.toList());
            return new WeekPoints(points);
        }
        
        private boolean filterFromWeek(BugByEnvironmentDataPoint dataPoint, String startOfWeek) {
            LocalDate weekFromPoint = getLocalDate(dataPoint);
            return weekFromPoint.equals(LocalDate.parse(startOfWeek));
        }
        private LocalDate getLocalDate(BugByEnvironmentDataPoint dataPoint) {
            return dataPoint.date.atZone(timezone).toLocalDate();
        }

        private BugByEnvrionmentDataPointsAsserter totalOfPointsIs(int size) {
            Assertions.assertThat(dataPoints).hasSize(size);
            return this;
        }
        
        
        private class WeekPoints {

            private List<BugByEnvironmentDataPoint> points;
            
            private WeekPoints(List<BugByEnvironmentDataPoint> points) {
                this.points = points;
            }

            private PointAsserter at(String key) {
                Optional<BugByEnvironmentDataPoint> possiblePoint = points.stream().filter(dp -> key.equals(dp.bugCategory)).findFirst();
                return new PointAsserter(possiblePoint);
            }

            private WeekPoints hasTotalOfPoints(int size) {
                Assertions.assertThat(points).hasSize(size);
                return this;
            }
            
            private BugByEnvrionmentDataPointsAsserter eoW() {
                return BugByEnvrionmentDataPointsAsserter.this;
            }

            private class PointAsserter {
                
                private Optional<BugByEnvironmentDataPoint> dataPoint;

                private PointAsserter(Optional<BugByEnvironmentDataPoint> dataPoint) {
                    this.dataPoint = dataPoint;
                }

                private WeekPoints hasValue(long value) {
                    Assertions.assertThat(dataPoint).hasValueSatisfying(dp -> Assertions.assertThat(dp.totalOfBugs).isEqualTo(value));
                    return WeekPoints.this;
                }
                
             }
        }
    }

    private class InnerContext {
        
        private Map<String,Range<LocalDate>> projectRangeConfigurations = new HashMap<>();
        
        private DSLKpi context;
        
        private InnerContext() {
            initializeProjectRanges();
            this.context = dsl();
            initializeTransitions();
        }
        
        private InnerContext projectWithRangeOf(String weekConfiguration) {
            Range<LocalDate> projectRange = projectRangeConfigurations.get(weekConfiguration); 
            context
                .environment()
                    .services()
                        .projects()
                            .withKey("PROJ")
                            .startAt(projectRange.getMinimum().toString())
                            .deliveredAt(projectRange.getMaximum().toString())
                        .eoP()
                    .eoPs();
            return this;
        }
        
        private InnerContext withNoProjectRange() {
            context
            .environment()
                .services()
                    .projects()
                        .withKey("PROJ")
                    .eoP()
                .eoPs();
            return this;
        }
        
        private InnerContext withOpenEndProjectRange() {
            context
            .environment()
                .services()
                    .projects()
                        .withKey("PROJ")
                        .startAt("2019-03-31")
                    .eoP()
                .eoPs();
            return this;
        }
        
        private InnerContext withOpenStartProjectRange() {
            context
            .environment()
                .services()
                    .projects()
                        .withKey("PROJ")
                        .deliveredAt("2019-04-20")
                    .eoP()
                .eoPs();
            return this;
        }

        private BehaviorFactory when() {
            return context.when();
        }

        private InnerContext preConfiguredBugs() {
            context
                .environment()
                    .givenFeature("I-1")
                        .project("PROJ")
                        .type("Bug")
                        .withPreconfiguredTransition("Issue Closing on Week 1")
                    .eoI()
                    .givenFeature("1-2")
                        .project("PROJ")
                        .type("Bug")
                        .fields()
                            .field("clientEnvironment").value("Production")
                        .eoF()
                        .withPreconfiguredTransition("Issue Opening on Week 1 - Closing on Week 2")
                    .eoI()
                    .givenFeature("I-3")
                        .project("PROJ")
                        .type("Feature")
                        .withPreconfiguredTransition("Default Feature Transitions")
                        .subtask("I-4")
                            .type("Alpha Bug")
                            .withPreconfiguredTransition("Issue Opening on Week 2 - Closing on Week 3")
                        .endOfSubtask()
                        .subtask("I-5")
                            .type("Alpha Bug")
                            .fields()
                                .field("clientEnvironment").value("Alpha")
                            .eoF()
                            .withPreconfiguredTransition("Issue Opening on Week 3 - Not Closing")
                        .endOfSubtask()
                    .eoI()
                    .givenFeature("I-6")
                        .project("PROJ")
                        .type("Bug")
                        .fields()
                            .field("clientEnvironment").value("Production")
                        .eoF()
                        .withPreconfiguredTransition("Issue Opening and Closing on Week 1")
                        .subtask("I-7")
                            .type("Alpha Bug")
                            .withPreconfiguredTransition("Issue Opening and Closing on Week 2")
                        .endOfSubtask()
                    .eoI()
                    .givenFeature("I-8")
                        .project("PROJ")
                        .type("Bug")
                        .fields()
                            .field("clientEnvironment").value("Production")
                        .eoF()
                        .withPreconfiguredTransition("Issue Opening and Closing on Week 3")
                    .eoI()
                    .givenFeature("I-9")
                        .type("Feature")
                        .project("PROJ")
                        .withPreconfiguredTransition("Default Feature Transitions")
                        .subtask("I-10")
                            .type("Alpha Bug")
                            .fields()
                                .field("clientEnvironment").value("Alpha")
                            .eoF()
                            .withPreconfiguredTransition("Issue Opening on Week 1 - Closing on Week 3")
                        .endOfSubtask()
                    .eoI()
                    .givenFeature("I-11")
                        .project("PROJ")
                        .type("Bug")
                        .withPreconfiguredTransition("Issue Opening on Week 1 - Not Closing")
                    .eoI()
                    .givenFeature("I-12")
                        .project("PROJ")
                        .type("Bug")
                        .fields()
                            .field("clientEnvironment").value("Production")
                        .eoF()
                        .withPreconfiguredTransition("Issue Opening on Week 2 - Not Closing")
                    .eoI()
                    .givenFeature("I-13")
                        .project("PROJ")
                        .type("Feature")
                        .withPreconfiguredTransition("Default Feature Transitions")
                        .subtask("I-14")
                            .type("Alpha Bug")
                            .withPreconfiguredTransition("Issue Opening Before Week 1 - Closing on Week 2")
                        .endOfSubtask()
                        .subtask("I-15")
                            .type("Alpha Bug")
                            .withPreconfiguredTransition("Issue Opening Before Week 1 - Closing on Week 3")
                        .endOfSubtask()
                    .eoI()
                    .givenFeature("I-16")
                        .project("PROJ")
                        .type("Bug")
                        .fields()
                            .field("clientEnvironment").value("Production")
                        .eoF()
                        .withPreconfiguredTransition("Issue Open Before Week 1- Not Closing")
                    .eoI()
                .eoE();
            
            return this;
        }

        private void initializeTransitions() {
            context.environment()
                .preConfigureTransitions("Default Feature Transitions")//t default
                    .status("Open").date("2019-04-05")
                    .status("To Do").date("2019-04-05")
                    .status("Doing").date("2019-04-06")
                    .status("Done").noDate()
                .eoSt()
                .preConfigureTransitions("Issue Closing on Week 1") //t1
                    .status("Open").date("2019-03-20")
                    .status("To Do").date("2019-03-21")
                    .status("Doing").date("2019-03-31")
                    .status("Done").date("2019-04-02")
                .eoSt()
                .preConfigureTransitions("Issue Opening on Week 1 - Closing on Week 2") //t2
                    .status("Open").date("2019-04-02")
                    .status("To Do").date("2019-04-03")
                    .status("Doing").date("2019-04-06")
                    .status("Done").date("2019-04-08")
                .eoSt()
                .preConfigureTransitions("Issue Opening on Week 2 - Closing on Week 3")//t3
                    .status("Open").date("2019-04-08")
                    .status("To Do").date("2019-04-09")
                    .status("Doing").date("2019-04-12")
                    .status("Done").date("2019-04-14")
                .eoSt()
                .preConfigureTransitions("Issue Opening on Week 3 - Not Closing")//t4
                    .status("Open").date("2019-04-16")
                    .status("To Do").date("2019-04-18")
                    .status("Doing").date("2019-04-20")
                    .status("Done").noDate()
                .eoSt()
                .preConfigureTransitions("Issue Opening and Closing on Week 1")//t5
                    .status("Open").date("2019-04-02")
                    .status("To Do").date("2019-04-03")
                    .status("Doing").date("2019-04-04")
                    .status("Done").date("2019-04-05")
                .eoSt()
                .preConfigureTransitions("Issue Opening and Closing on Week 2")//t6
                    .status("Open").date("2019-04-09")
                    .status("To Do").date("2019-04-10")
                    .status("Doing").date("2019-04-11")
                    .status("Done").date("2019-04-12")
                .eoSt()
                .preConfigureTransitions("Issue Opening and Closing on Week 3")//t7
                    .status("Open").date("2019-04-16")
                    .status("To Do").date("2019-04-17")
                    .status("Doing").date("2019-04-18")
                    .status("Done").date("2019-04-19")
                .eoSt()
                .preConfigureTransitions("Issue Opening on Week 1 - Closing on Week 3")//t8
                    .status("Open").date("2019-04-02")
                    .status("To Do").date("2019-04-10")
                    .status("Doing").date("2019-04-12")
                    .status("Done").date("2019-04-18")
                .eoSt()
                .preConfigureTransitions("Issue Opening on Week 1 - Not Closing")//t9
                    .status("Open").date("2019-04-02")
                    .status("To Do").date("2019-04-03")
                    .status("Doing").date("2019-04-06")
                    .status("Done").noDate()
                .eoSt()
                .preConfigureTransitions("Issue Opening on Week 2 - Not Closing")//t10
                    .status("Open").date("2019-04-08")
                    .status("To Do").date("2019-04-10")
                    .status("Doing").noDate()
                    .status("Done").noDate()
                .eoSt()
                .preConfigureTransitions("Issue Opening Before Week 1 - Closing on Week 2")//t11
                    .status("Open").date("2019-03-28")
                    .status("To Do").date("2019-04-04")
                    .status("Doing").date("2019-04-06")
                    .status("Done").date("2019-04-08")
                .eoSt()
                .preConfigureTransitions("Issue Opening Before Week 1 - Closing on Week 3")//t12
                    .status("Open").date("2019-03-30")
                    .status("To Do").date("2019-04-03")
                    .status("Doing").date("2019-04-08")
                    .status("Done").date("2019-04-15")
                .eoSt()
                .preConfigureTransitions("Issue Open Before Week 1- Not Closing")//t13
                    .status("Open").date("2019-03-30")
                    .status("To Do").date("2019-04-08")
                    .status("Doing").date("2019-04-14")
                    .status("Done").noDate()
                .eoSt();
        }

        private void initializeProjectRanges() {
            projectRangeConfigurations.put("3 Weeks Configuration", getRange("2019-03-31","2019-04-20")); //x2
            projectRangeConfigurations.put("3 Weeks Configuration - Starting middle Week 1", getRange("2019-04-03","2019-04-20")); //x3
            projectRangeConfigurations.put("3 Weeks Configuration - Finishing middle Week 3", getRange("2019-03-31","2019-04-17")); //x4
            projectRangeConfigurations.put("2 Weeks Configuration", getRange("2019-03-31","2019-04-13")); //x5
            projectRangeConfigurations.put("2 Weeks Configuration - Finishing middle Week 2", getRange("2019-03-31","2019-04-10"));//x6
            projectRangeConfigurations.put("Start and Finishing middle Week 2", getRange("2019-04-09","2019-04-12"));//x7
            projectRangeConfigurations.put("Full Week 3", getRange("2019-04-14","2019-04-20"));//x8
            projectRangeConfigurations.put("Starting almost end of Week 1- Finishing early on Week 3", getRange("2019-04-05","2019-04-15"));//x9
        }
        
        private Range<LocalDate> getRange(String startOfWeek, String endOfWeek){
            return RangeUtils.between(LocalDate.parse(startOfWeek), LocalDate.parse(endOfWeek));
        }

        private DSLKpi dsl() {
            return new DSLKpi()
                        .environment()
                            .types()
                                .addFeatures("Bug","Feature")
                                .addSubtasks("Alpha Bug","Development")
                            .eoT()
                            .statuses()
                                .withNotProgressingStatuses("Open","To Do","Done")
                                .withProgressingStatuses("Doing")
                           .eoS()
                           .withJiraProperties()
                               .followUp().withExcludedStatuses("Open").eof()
                           .eoJp()
                           .withKpiProperties()
                               .environmentField("clientEnvironment")
                           .eoKP()
                           .withKpiProperties(withBugTypes("Bug","Alpha Bug"))
                        .eoE();
        }
        
    }
    
}
