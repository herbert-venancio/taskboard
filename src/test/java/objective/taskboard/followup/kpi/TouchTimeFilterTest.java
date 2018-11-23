package objective.taskboard.followup.kpi;

import static objective.taskboard.utils.DateTimeUtils.parseDateTime;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.kpi.enviroment.KPIEnvironmentBuilder;
import objective.taskboard.testUtils.FixedClock;
import objective.taskboard.utils.DateTimeUtils;

public class TouchTimeFilterTest {
    
    private FixedClock clock = new FixedClock();
    private static final String START_RANGE = "2018-01-10";
    private static final String END_RANGE = "2018-01-20";
    private static final ZoneId timezone = ZoneId.systemDefault();
    
    private TouchTimeFilter subject;
    
    @Before
    public void setup() {
        clock.setNow(DateTimeUtils.parseDateTime("2018-02-25").toInstant());
        
        ProjectFilterConfiguration project = configureProject();
        
        ProjectRangeByConfiguration defaultRange = new ProjectRangeByConfiguration(project);
        subject = new TouchTimeFilter(clock,timezone,defaultRange);
    }

    private ProjectFilterConfiguration configureProject() {
        ProjectFilterConfiguration project = Mockito.mock(ProjectFilterConfiguration.class);
        LocalDate startDate = parseDateTime(START_RANGE).toLocalDate();
        LocalDate endDate = parseDateTime(END_RANGE).toLocalDate();
        when(project.getStartDate()).thenReturn(Optional.of(startDate));
        when(project.getDeliveryDate()).thenReturn(Optional.of(endDate));
        return project;
    }
    
    @Test
    public void filterHappyDay() {
        KPIEnvironmentBuilder builder = getSimpleEnvironment();
        
        builder.mockingSubtask("I-1", "Development")
                .addTransition("To Do","2018-01-11")
                .addTransition("Doing","2018-01-12")
                .addTransition("To Review","2018-01-13")
                .addTransition("Reviewing","2018-01-14")
                .addTransition("Done","2018-01-15");
        
        IssueKpi issueKpi = builder.buildCurrentIssueAsKpi();
                
        assertTrue(subject.test(issueKpi));
    }
    
    @Test
    public void issueBeforeRange() {
        KPIEnvironmentBuilder builder = getSimpleEnvironment();
        
        builder.mockingSubtask("I-1", "Development")
                .addTransition("To Do","2018-01-02")
                .addTransition("Doing","2018-01-03")
                .addTransition("To Review","2018-01-05")
                .addTransition("Reviewing","2018-01-07")
                .addTransition("Done","2018-01-09");
        
        IssueKpi issueKpi = builder.buildCurrentIssueAsKpi();
        
        assertFalse(subject.test(issueKpi));
    }
    
    @Test
    public void issueRangeEndingAtStartOfTimelineRange() {
        KPIEnvironmentBuilder builder = getSimpleEnvironment();
        
        builder.mockingSubtask("I-1", "Development")
                .addTransition("To Do","2018-01-02")
                .addTransition("Doing","2018-01-03")
                .addTransition("To Review","2018-01-05")
                .addTransition("Reviewing","2018-01-07")
                .addTransition("Done","2018-01-10");
        
        IssueKpi issueKpi = builder.buildCurrentIssueAsKpi();
        
        assertTrue(subject.test(issueKpi));
    }
    
    @Test
    public void issueStartingAtEndOfRange() {
        KPIEnvironmentBuilder builder = getSimpleEnvironment();
        
        builder.mockingSubtask("I-1", "Development")
                .addTransition("To Do","2018-01-19")
                .addTransition("Doing","2018-01-20")
                .addTransition("To Review","2018-01-25")
                .addTransition("Reviewing","2018-01-27")
                .addTransition("Done","2018-01-31");
        
        IssueKpi issueKpi = builder.buildCurrentIssueAsKpi();
        
        assertTrue(subject.test(issueKpi));
    }
    
    @Test
    public void issueAfterRange() {
        KPIEnvironmentBuilder builder = getSimpleEnvironment();
        
        builder.mockingSubtask("I-1", "Development")
                .addTransition("To Do","2018-01-20")
                .addTransition("Doing","2018-01-23")
                .addTransition("To Review","2018-01-25")
                .addTransition("Reviewing","2018-01-27")
                .addTransition("Done","2018-01-31");
        
        IssueKpi issueKpi = builder.buildCurrentIssueAsKpi();
        
        assertFalse(subject.test(issueKpi));
    }
    
    @Test
    public void issueWithSameRange() {
        KPIEnvironmentBuilder builder = getSimpleEnvironment();
        
        builder.mockingSubtask("I-1", "Development")
                .addTransition("To Do","2018-01-09")
                .addTransition("Doing","2018-01-10")
                .addTransition("To Review","2018-01-11")
                .addTransition("Reviewing","2018-01-13")
                .addTransition("Done","2018-01-20");
        
        IssueKpi issueKpi = builder.buildCurrentIssueAsKpi();
        
        assertTrue(subject.test(issueKpi));
    }
    
    @Test
    public void openIssue() {
        KPIEnvironmentBuilder builder = getSimpleEnvironment();
        
        builder.mockingSubtask("I-1", "Development")
                .addTransition("To Do","2018-01-15")
                .addTransition("Doing")
                .addTransition("To Review")
                .addTransition("Reviewing")
                .addTransition("Done");
        
        IssueKpi issueKpi = builder.buildCurrentIssueAsKpi();
        
        assertFalse(subject.test(issueKpi));
    }
    
    @Test
    public void workingIssue_startingAfterInsideRange() {
        KPIEnvironmentBuilder builder = getSimpleEnvironment();
        
        builder.mockingSubtask("I-1", "Development")
                .addTransition("To Do","2018-01-10")
                .addTransition("Doing","2018-01-15")
                .addTransition("To Review")
                .addTransition("Reviewing")
                .addTransition("Done");
        
        IssueKpi issueKpi = builder.buildCurrentIssueAsKpi();
        
        assertTrue(subject.test(issueKpi));
    }
    
    @Test
    public void workingIssue_startingBeforeRange() {
        KPIEnvironmentBuilder builder = getSimpleEnvironment();
        builder.mockingSubtask("I-1", "Development")
                .addTransition("To Do","2018-01-08")
                .addTransition("Doing","2018-01-09")
                .addTransition("To Review")
                .addTransition("Reviewing")
                .addTransition("Done");
        
        IssueKpi issueKpi = builder.buildCurrentIssueAsKpi();
        
        assertTrue(subject.test(issueKpi));
    }
    
    private KPIEnvironmentBuilder getSimpleEnvironment() {
        KPIEnvironmentBuilder builder = new KPIEnvironmentBuilder();
        builder.addSubtaskType(1l, "Development")
                .addSubtaskType(2l, "Alpha Test")
                .addFeatureType(3l, "Feature");
        
        builder.addStatus(1l, "To Do", false)
                .addStatus(2l, "Doing", true)
                .addStatus(3l, "To Review", false)
                .addStatus(4l, "Reviewing", true)
                .addStatus(5l,"Done",false);
        
        return builder;
    }

}
