package objective.taskboard.followup.kpi;

import static java.time.DayOfWeek.SUNDAY;
import static java.time.LocalDate.parse;
import static java.util.Arrays.asList;
import static objective.taskboard.followup.kpi.KpiLevel.SUBTASKS;
import static objective.taskboard.utils.RangeUtils.between;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Range;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.ProjectDatesNotConfiguredException;
import objective.taskboard.followup.kpi.enviroment.KPIEnvironmentBuilder;
import objective.taskboard.followup.kpi.properties.KPIProperties;
import objective.taskboard.followup.kpi.touchTime.TouchTimeByWeekDataProvider;
import objective.taskboard.followup.kpi.touchTime.TouchTimeChartByWeekDataPoint;
import objective.taskboard.followup.kpi.touchTime.TouchTimeChartByWeekDataSet;
import objective.taskboard.followup.kpi.touchTime.TouchTimeFilter;
import objective.taskboard.jira.ProjectService;
import objective.taskboard.jira.properties.JiraProperties;
import objective.taskboard.jira.properties.StatusConfiguration.StatusPriorityOrder;
import objective.taskboard.utils.DateTimeUtils;

@RunWith(MockitoJUnitRunner.class)
public class TouchTimeByWeekDataProviderTest {
    
    private static final ZoneId ZONE_ID = ZoneId.systemDefault();
    
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private IssueKpiService issueKpiService;

    @Mock
    private ProjectService projectService;
    
    @Mock
    private KPIProperties kpiProperties;
    
    @Mock
    private JiraProperties jiraProperties;

    private TouchTimeByWeekDataProvider subject;
    
    @Test
    public void getDataSet_happyDay_twoWeeks_fourIssues() {
        
        mockProject("TASKB","2018-11-12","2018-11-26");
        KPIEnvironmentBuilder builder = buildEnvironment(asList("Doing","Reviewing"));
        buildSubtasks(builder);
        
        List<IssueKpi> issues = builder.buildAllIssuesAsKpi();
        
        IssuesAsserter allIssues = givenIssues(issues);
        allIssues
            .atWeekStartingAtSunday("2018-11-11")
                .endsAt("2018-11-17")
                .haveIssues("I-1","I-2","I-3")
                .with("I-1")
                    .on("Doing").hasEffort(3.0)
                    .on("Reviewing").hasEffort(0)
                .with("I-2")
                    .on("Doing").hasEffort(1.0)
                    .on("Reviewing").hasEffort(2.0)
                .with("I-3")
                    .on("Doing").hasEffort(2.0)
                    .on("Reviewing").hasEffort(1.0)
            .atWeekStartingAtSunday("2018-11-18")
                .endsAt("2018-11-24")
                .haveIssues("I-1","I-2","I-4")
                .with("I-1")
                    .on("Doing").hasEffort(6.0)
                    .on("Reviewing").hasEffort(4.0)
                .with("I-2")
                    .on("Doing").hasEffort(1.0)
                    .on("Reviewing").hasEffort(6.0)
                .with("I-4")
                    .on("Doing").hasEffort(2.0)
                    .on("Reviewing").hasEffort(2.0)
            .atWeekStartingAtSunday("2018-11-25")
                .endsAt("2018-12-01")
                .haveIssues("I-1")
                    .with("I-1")
                        .on("Doing").hasEffort(6.0)
                        .on("Reviewing").hasEffort(4.0);
                    
        when(issueKpiService.getIssuesFromCurrentState("TASKB", ZONE_ID,SUBTASKS)).thenReturn(issues);
        subject = getProvider();
        TouchTimeChartByWeekDataSet dataSet = subject.getDataSet("TASKB", KpiLevel.SUBTASKS, ZoneId.systemDefault());
        givenDataSet(dataSet)
            .hasSize(6).pointsIterator()
            .atDate("2018-11-11").andStatus("Doing").hasEffort(2.0).next()
            .atDate("2018-11-11").andStatus("Reviewing").hasEffort(1.0).next()
            .atDate("2018-11-18").andStatus("Doing").hasEffort(3.0).next()
            .atDate("2018-11-18").andStatus("Reviewing").hasEffort(4.0).next()
            .atDate("2018-11-25").andStatus("Doing").hasEffort(6.0).next()
            .atDate("2018-11-25").andStatus("Reviewing").hasEffort(4.0);
    }
    
    @Test
    public void getDataSet_restrictingByTimeLine() {
        
        mockProject("TASKB","2018-11-12","2018-11-16");
        KPIEnvironmentBuilder builder = buildEnvironment(asList("Doing","Reviewing"));
        buildSubtasks(builder);
        
        List<IssueKpi> issues = builder.buildAllIssuesAsKpi();
        IssuesAsserter allIssues = givenIssues(issues);
        allIssues
            .atWeekStartingAtSunday("2018-11-11")
                .endsAt("2018-11-17")
                .haveIssues("I-1","I-2","I-3")
                .with("I-1")
                    .on("Doing").hasEffort(3.0)
                    .on("Reviewing").hasEffort(0)
                .with("I-2")
                    .on("Doing").hasEffort(1.0)
                    .on("Reviewing").hasEffort(2.0)
                .with("I-3")
                    .on("Doing").hasEffort(2.0)
                    .on("Reviewing").hasEffort(1.0)
            .atWeekStartingAtSunday("2018-11-18")
                .endsAt("2018-11-24")
                .haveIssues("I-1","I-2","I-4")
                .with("I-1")
                    .on("Doing").hasEffort(6.0)
                    .on("Reviewing").hasEffort(4.0)
                .with("I-2")
                    .on("Doing").hasEffort(1.0)
                    .on("Reviewing").hasEffort(6.0)
                .with("I-4")
                    .on("Doing").hasEffort(2.0)
                    .on("Reviewing").hasEffort(2.0)
            .atWeekStartingAtSunday("2018-11-25")
                .endsAt("2018-12-01")
                .haveIssues("I-1")
                    .with("I-1")
                        .on("Doing").hasEffort(6.0)
                        .on("Reviewing").hasEffort(4.0);
        
        
        when(issueKpiService.getIssuesFromCurrentState("TASKB", ZONE_ID,SUBTASKS)).thenReturn(issues);
        subject = getProvider();
        TouchTimeChartByWeekDataSet dataSet = subject.getDataSet("TASKB", KpiLevel.SUBTASKS, ZoneId.systemDefault());
        
        givenDataSet(dataSet)
            .hasSize(2).pointsIterator()
            .atDate("2018-11-11").andStatus("Doing").hasEffort(2.0).next()
            .atDate("2018-11-11").andStatus("Reviewing").hasEffort(1.0);
    }
    
    @Test
    public void noProgressingStatusConfigured() {
        mockProject("TASKB","2018-11-12","2018-11-26");
        KPIEnvironmentBuilder builder = buildEnvironment(asList());
        buildSubtasks(builder);
        
        when(issueKpiService.getIssuesFromCurrentState("TASKB", ZONE_ID,SUBTASKS)).thenReturn(builder.buildAllIssuesAsKpi());
        subject = getProvider();
        TouchTimeChartByWeekDataSet dataSet = subject.getDataSet("TASKB", KpiLevel.SUBTASKS, ZoneId.systemDefault());
        List<TouchTimeChartByWeekDataPoint> points = dataSet.points;
        assertThat(points.size(),is(0));
        
    }

    @Test
    public void noDataPoints_whenNoIssuesReturnedFromService() {
        mockProject("TASKB","2018-11-12","2018-11-26");
        KPIEnvironmentBuilder builder = buildEnvironment(asList("Doing","Reviewing"));
        
        when(issueKpiService.getIssuesFromCurrentState("TASKB", ZONE_ID,SUBTASKS)).thenReturn(builder.buildAllIssuesAsKpi());
        
        subject = getProvider();
        TouchTimeChartByWeekDataSet dataSet = subject.getDataSet("TASKB", KpiLevel.SUBTASKS, ZoneId.systemDefault());
        List<TouchTimeChartByWeekDataPoint> points = dataSet.points;
        assertThat(points.size(),is(0));
    }
    
    @Test
    public void projectWithWrongRangeConfigured_noConfiguration() {
        
        mockProject("TASKB",Optional.empty(),Optional.empty());
        KPIEnvironmentBuilder builder = buildEnvironment(asList("Doing","Reviewing"));
        
        when(issueKpiService.getIssuesFromCurrentState("TASKB", ZONE_ID,SUBTASKS)).thenReturn(builder.buildAllIssuesAsKpi());
        
        subject = getProvider();
        expectedException.expect(ProjectDatesNotConfiguredException.class);
        subject.getDataSet("TASKB", KpiLevel.SUBTASKS, ZoneId.systemDefault());
    }
    
    @Test
    public void projectWithWrongRangeConfigured_noStartDate() {
        
        mockProject("TASKB",Optional.empty(),Optional.of(parse("2018-11-26")));
        KPIEnvironmentBuilder builder = buildEnvironment(asList("Doing","Reviewing"));
        
        when(issueKpiService.getIssuesFromCurrentState("TASKB", ZONE_ID,SUBTASKS)).thenReturn(builder.buildAllIssuesAsKpi());
        
        subject = getProvider();
        expectedException.expect(ProjectDatesNotConfiguredException.class);
        subject.getDataSet("TASKB", KpiLevel.SUBTASKS, ZoneId.systemDefault());
    }
    
    @Test
    public void projectWithWrongRangeConfigured_noDeliveryDate() {
        
        mockProject("TASKB",Optional.of(parse("2018-11-12")),Optional.empty());
        KPIEnvironmentBuilder builder = buildEnvironment(asList("Doing","Reviewing"));
        
        when(issueKpiService.getIssuesFromCurrentState("TASKB", ZONE_ID,SUBTASKS)).thenReturn(builder.buildAllIssuesAsKpi());
        
        subject = getProvider();
        expectedException.expect(ProjectDatesNotConfiguredException.class);
        subject.getDataSet("TASKB", SUBTASKS, ZoneId.systemDefault());
    }
    
    private IssuesAsserter givenIssues(List<IssueKpi> issues) {
        return new IssuesAsserter(issues);
    }
    
    private TouchTimeDataSetAsserter givenDataSet(TouchTimeChartByWeekDataSet ds) {
        return new TouchTimeDataSetAsserter(ds);
    }
   
    private void mockProject(String projectKey, String startDate, String deliveryDate) {
        mockProject(projectKey, Optional.of(parse(startDate)),Optional.of(parse(deliveryDate)));
    }
    
    private void mockProject(String projectKey, Optional<LocalDate> startDate, Optional<LocalDate> deliveryDate) {
        ProjectFilterConfiguration project = mock(ProjectFilterConfiguration.class);
        when(project.getStartDate()).thenReturn(startDate);
        when(project.getDeliveryDate()).thenReturn(deliveryDate);
        when(projectService.getTaskboardProjectOrCry(projectKey)).thenReturn(project);
    }
    
    private KPIEnvironmentBuilder buildEnvironment(List<String> progressingStatuses) {
        
        KPIEnvironmentBuilder builder = new KPIEnvironmentBuilder();
        builder.addSubtaskType(1l, "Dev")
                .addStatus(1l, "To Do", false)
                .addStatus(2l, "Doing", true)
                .addStatus(3l, "To Review", false)
                .addStatus(4l, "Reviewing", true)
                .addStatus(5l, "Done", false);
        
        builder.withKpiProperties(kpiProperties)
                .mockProgressingStatuses(progressingStatuses)
                .mockKpiProperties();
                
        
        StatusPriorityOrder order = new StatusPriorityOrder();
        order.setSubtasks(new String[] {"To Do","Doing","To Review","Reviewing","Done"});
        Mockito.when(jiraProperties.getStatusPriorityOrder()).thenReturn(order);
        return builder;
    }

    private void buildSubtasks(KPIEnvironmentBuilder builder) {
        builder.mockingSubtask("I-1", "Dev")
                .setProjectKeyToCurrentIssue("TASKB")
                .addTransition("To Do","2018-11-08")
                .addTransition("Doing","2018-11-10")
                .addTransition("To Review","2018-11-16")
                .addTransition("Reviewing","2018-11-20")
                .addTransition("Done","2018-11-27")
                .addWorklog("2018-11-10",1.0)
                .addWorklog("2018-11-15",2.0)
                .addWorklog("2018-11-19",3.0)
                .addWorklog("2018-11-23",4.0);
        
        builder.mockingSubtask("I-2", "Dev")
                .setProjectKeyToCurrentIssue("TASKB")
                .addTransition("To Do","2018-11-10")
                .addTransition("Doing","2018-11-13")
                .addTransition("To Review","2018-11-14")
                .addTransition("Reviewing","2018-11-15")
                .addTransition("Done","2018-11-22")
                .addWorklog("2018-11-14",1.0)
                .addWorklog("2018-11-16",2.0)
                .addWorklog("2018-11-20",4.0);
        
        builder.mockingSubtask("I-3", "Dev")
                .setProjectKeyToCurrentIssue("TASKB")
                .addTransition("To Do","2018-11-10")
                .addTransition("Doing","2018-11-12")
                .addTransition("To Review","2018-11-13")
                .addTransition("Reviewing","2018-11-14")
                .addTransition("Done","2018-11-16")
                .addWorklog("2018-11-12",2.0)
                .addWorklog("2018-11-15",1.0);
        
        builder.mockingSubtask("I-4", "Dev")
                .setProjectKeyToCurrentIssue("TASKB")
                .addTransition("To Do","2018-11-18")
                .addTransition("Doing","2018-11-19")
                .addTransition("To Review","2018-11-21")
                .addTransition("Reviewing","2018-11-22")
                .addTransition("Done","2018-11-24")
                .addWorklog("2018-11-20",2.0)
                .addWorklog("2018-11-23",2.0);
        
    }
    
    private TouchTimeByWeekDataProvider getProvider() {
        return new TouchTimeByWeekDataProvider(issueKpiService, projectService, kpiProperties, jiraProperties);
    }
    
    private class TouchTimeDataSetAsserter{
        private TouchTimeChartByWeekDataSet subject;
        private int index = 0;

        private TouchTimeDataSetAsserter(TouchTimeChartByWeekDataSet subject) {
            this.subject = subject;
        }
        
        private TouchTimeDataSetAsserter hasSize(int size) {
            assertThat(subject.points.size(),is(size));
            return this;
        }
        private TouchTimeByWeekPointAsserter pointsIterator() {
            return new TouchTimeByWeekPointAsserter(this.subject.points.get(index++));
        }
        
        private class TouchTimeByWeekPointAsserter{
            private TouchTimeChartByWeekDataPoint subject ;
            private String date;
            private String status;

            private TouchTimeByWeekPointAsserter(TouchTimeChartByWeekDataPoint subject) {
                this.subject = subject;
            }
            
            private TouchTimeByWeekPointAsserter andStatus(String status) {
                this.status = status;
                return this;
            }

            private TouchTimeByWeekPointAsserter atDate(String date) {
                this.date = date;
                return this;
            }
            
            private TouchTimeByWeekPointAsserter hasEffort(double effort) {
                assertThat(subject.date,is(DateTimeUtils.parseStringToDate(date)));
                assertThat(subject.status,is(status));
                assertThat(subject.effortInHours,is(effort));
                return this;
            }
            
            private TouchTimeByWeekPointAsserter next() {
                return TouchTimeDataSetAsserter.this.pointsIterator();
            }
        }
        
    }
    
    private class IssuesAsserter {
        private Map<String, IssueKpi> issues;
        

        private IssuesAsserter(List<IssueKpi> issues) {
            this.issues = issues.stream().collect(Collectors.toMap(IssueKpi::getIssueKey, Function.identity()));
        }

        private DateAsserter atWeekStartingAtSunday(String startOfWeek) {
            
            LocalDate startDate = LocalDate.parse(startOfWeek);
            assertThat(startDate.getDayOfWeek(),is(SUNDAY));
            LocalDate endDate = startDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
            Range<LocalDate> week = between(startDate, endDate);
            return new DateAsserter(week,filterIssues(week));
        }
        
        private Map<String,IssueKpi> filterIssues(Range<LocalDate> week) {
            TouchTimeFilter filter = new TouchTimeFilter(ZONE_ID, new WeekTimelineRange(week));
            return this.issues.values().stream().filter(filter).collect(Collectors.toMap(IssueKpi::getIssueKey, Function.identity()));
        }

        private class DateAsserter {
            
            private Range<LocalDate> week;
            private Map<String,IssueKpi> issues;
            
            private DateAsserter(Range<LocalDate> week, Map<String,IssueKpi> issues) {
                this.week = week;
                this.issues = issues;
            }

            public DateAsserter endsAt(String end) {
                assertThat(week.getMaximum(),is(LocalDate.parse(end)));
                return this;
            }

            private DateAsserter haveIssues(String... issuesToTest) {
                for (String issue : issuesToTest) {
                    if(!issues.containsKey(issue))
                        Assert.fail(String.format("Issue %s not found on collection", issue));
                }
                return this;
            }
            
            private SingleIssueAsserter with(String issue) {
                return new SingleIssueAsserter(issues.get(issue));
            }
            
            private class SingleIssueAsserter {
                
                private IssueKpi issue;
                private String currentStatus;
                
                private SingleIssueAsserter(IssueKpi issue) {
                    this.issue = issue;
                }
                
                private SingleIssueAsserter on(String status) {
                    this.currentStatus = status;
                    return this;
                }
                
                private SingleIssueAsserter hasEffort(long effort) {
                    Assert.assertThat(issue.getEffortUntilDate(currentStatus, DateAsserter.this.week.getMaximum().atStartOfDay(ZONE_ID)), is(effort));
                    return this;
                }
                
                private SingleIssueAsserter hasEffort(double effortInHours) {
                    return hasEffort(DateTimeUtils.hoursToSeconds(effortInHours));
                }
                
                private SingleIssueAsserter with(String issue) {
                    return DateAsserter.this.with(issue);
                }
                
                private DateAsserter atWeekStartingAtSunday(String startOfWeek) {
                    return IssuesAsserter.this.atWeekStartingAtSunday(startOfWeek);
                }
                
            }
            
        }
            
    }
}
