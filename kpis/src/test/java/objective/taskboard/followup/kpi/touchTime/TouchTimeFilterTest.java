package objective.taskboard.followup.kpi.touchTime;

import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.ProjectRangeByConfiguration;
import objective.taskboard.followup.kpi.enviroment.KpiEnvironment;
import objective.taskboard.testUtils.FixedClock;
import objective.taskboard.utils.DateTimeUtils;

public class TouchTimeFilterTest {
    
    private static final ZoneId timezone = ZoneId.systemDefault();
    
    @Test
    public void filterHappyDay() {
        IssueKpi kpi = 
        		environment()
		        	.givenIssue("I-1")
		        		.type("Development")
		        		.withTransitions()
		        			.status("To Do").date("2018-01-11")
		        			.status("Doing").date("2018-01-12")
		                    .status("To Review").date("2018-01-13")
		                    .status("Reviewing").date("2018-01-14")
		                    .status("Done").date("2018-01-15")
		                    .eoT()
		                .buildIssueKpi();
        filter()
        	.startingAt("2018-01-10").endingAt("2018-01-20")
        	.todayIs("2018-02-25")
        	.issueShouldBeAccepted(kpi);
        
    }
    
    @Test
    public void issueBeforeRange() {
        IssueKpi kpi = 
        		environment()
		        	.givenIssue("I-1")
		        		.type("Development")
		        		.isSubtask()
		        		.withTransitions()
		        			.status("To Do").date("2018-01-02")
		        			.status("Doing").date("2018-01-03")
		                    .status("To Review").date("2018-01-05")
		                    .status("Reviewing").date("2018-01-07")
		                    .status("Done").date("2018-01-09")
		                    .eoT()
		                .buildIssueKpi();
        
        filter()
	    	.startingAt("2018-01-10").endingAt("2018-01-20")
	    	.todayIs("2018-02-25")
	    	.issueShouldNotBeAccepted(kpi);
        
    }
    
    @Test
    public void issueRangeEndingAtStartOfTimelineRange() {
        IssueKpi kpi = 
        		environment()
		        	.givenIssue("I-1")
		        		.type("Development")
		        		.isSubtask()
		        		.withTransitions()
		        			.status("To Do").date("2018-01-02")
		        			.status("Doing").date("2018-01-03")
		                    .status("To Review").date("2018-01-05")
		                    .status("Reviewing").date("2018-01-07")
		                    .status("Done").date("2018-01-10")
		                    .eoT()
		                .buildIssueKpi();
        
        filter()
	    	.startingAt("2018-01-10").endingAt("2018-01-20")
	    	.todayIs("2018-02-25")
	    	.issueShouldBeAccepted(kpi);
    }
    
    @Test
    public void issueStartingAtEndOfRange() {
        IssueKpi kpi = 
        		environment()
		        	.givenIssue("I-1")
		        		.type("Development")
		        		.isSubtask()
		        		.withTransitions()
		        			.status("To Do").date("2018-01-19")
		        			.status("Doing").date("2018-01-20")
		                    .status("To Review").date("2018-01-25")
		                    .status("Reviewing").date("2018-01-27")
		                    .status("Done").date("2018-01-31")
		                    .eoT()
		                .buildIssueKpi();
        
        filter()
	    	.startingAt("2018-01-10").endingAt("2018-01-20")
	    	.todayIs("2018-02-25")
	    	.issueShouldBeAccepted(kpi);
    }
    
    @Test
    public void issueAfterRange() {
        IssueKpi kpi = 
        		environment()
		        	.givenIssue("I-1")
		        		.type("Development")
		        		.isSubtask()
		        		.withTransitions()
		        			.status("To Do").date("2018-01-20")
		        			.status("Doing").date("2018-01-23")
		                    .status("To Review").date("2018-01-25")
		                    .status("Reviewing").date("2018-01-27")
		                    .status("Done").date("2018-01-31")
		                    .eoT()
		                .buildIssueKpi();
        
        filter()
	    	.startingAt("2018-01-10").endingAt("2018-01-20")
	    	.todayIs("2018-02-25")
	    	.issueShouldNotBeAccepted(kpi);
    }
    
    @Test
    public void issueWithSameRange() {
    	IssueKpi kpi = 
        		environment()
		        	.givenIssue("I-1")
		        		.type("Development")
		        		.isSubtask()
		        		.withTransitions()
		        			.status("To Do").date("2018-01-09")
		        			.status("Doing").date("2018-01-10")
		                    .status("To Review").date("2018-01-11")
		                    .status("Reviewing").date("2018-01-13")
		                    .status("Done").date("2018-01-20")
		                    .eoT()
		                .buildIssueKpi();
        
        filter()
	    	.startingAt("2018-01-10").endingAt("2018-01-20")
	    	.todayIs("2018-02-25")
	    	.issueShouldBeAccepted(kpi);
    }
    
    @Test
    public void openIssue() {
    	IssueKpi kpi = 
        		environment()
		        	.givenIssue("I-1")
		        		.type("Development")
		        		.isSubtask()
		        		.withTransitions()
		        			.status("To Do").date("2018-01-15")
		        			.status("Doing").noDate()
		                    .status("To Review").noDate()
		                    .status("Reviewing").noDate()
		                    .status("Done").noDate()
		                    .eoT()
		                .buildIssueKpi();
        
        filter()
	    	.startingAt("2018-01-10").endingAt("2018-01-20")
	    	.todayIs("2018-02-25")
	    	.issueShouldNotBeAccepted(kpi);
    }
    
    @Test
    public void workingIssue_startingAfterInsideRange() {
        IssueKpi kpi = 
        		environment()
		        	.givenIssue("I-1")
		        		.type("Development")
		        		.isSubtask()
		        		.withTransitions()
		        			.status("To Do").date("2018-01-10")
		        			.status("Doing").date("2018-01-15")
		                    .status("To Review").noDate()
		                    .status("Reviewing").noDate()
		                    .status("Done").noDate()
		                    .eoT()
		                .buildIssueKpi();
        
        filter()
	    	.startingAt("2018-01-10").endingAt("2018-01-20")
	    	.todayIs("2018-02-25")
	    	.issueShouldBeAccepted(kpi);
    }
    
    @Test
    public void workingIssue_startingBeforeRange() {
        IssueKpi kpi = 
        		environment()
        		    .todayIs("2018-02-25")
		        	.givenIssue("I-1")
		        		.type("Development")
		        		.isSubtask()
		        		.withTransitions()
		        			.status("To Do").date("2018-01-08")
		        			.status("Doing").date("2018-01-09")
		                    .status("To Review").noDate()
		                    .status("Reviewing").noDate()
		                    .status("Done").noDate()
		                    .eoT()
		                .buildIssueKpi();
        
        filter()
	    	.startingAt("2018-01-10").endingAt("2018-01-20")
	    	.issueShouldBeAccepted(kpi);
    }
    
    private TouchTimeFilterAsserter filter() {
    	return new TouchTimeFilterAsserter();
    }
    
    private KpiEnvironment environment() {
    	KpiEnvironment environment = new KpiEnvironment();
    	environment
    		.withSubtaskType("Development")
    		.withSubtaskType("Alpha Test")
    		.withFeatureType("Feature")
    		.withStatus("To Do").isNotProgressing()
    		.withStatus("Doing").isProgressing()
    		.withStatus("To Review").isNotProgressing()
    		.withStatus("Reviewing").isProgressing()
    		.withStatus("Done").isNotProgressing();
    	return environment;
    }

    private class TouchTimeFilterAsserter {

		private String projectStartDate;
		private String projectEndDate;
		private FixedClock clock = new FixedClock();

		public TouchTimeFilterAsserter startingAt(String projectStartDate) {
			this.projectStartDate = projectStartDate;
			return this;
		}

		public void issueShouldNotBeAccepted(IssueKpi kpi) {
	        TouchTimeFilter filter = getFilter();
	        Assert.assertFalse(filter.test(kpi));
		}

		public void issueShouldBeAccepted(IssueKpi kpi) {
	        TouchTimeFilter filter = getFilter();
	        Assert.assertTrue(filter.test(kpi));
			
		}

		public TouchTimeFilterAsserter todayIs(String today) {
			clock.setNow(DateTimeUtils.parseDateTime(today).toInstant());
			return this;
		}

		public TouchTimeFilterAsserter endingAt(String projectEndDate) {
			this.projectEndDate = projectEndDate;
			return this;
		}
		
	    private ProjectFilterConfiguration configureProject() {
	        ProjectFilterConfiguration project = Mockito.mock(ProjectFilterConfiguration.class);
	        LocalDate startDate = LocalDate.parse(projectStartDate);
	        LocalDate endDate = LocalDate.parse(projectEndDate);
	        when(project.getStartDate()).thenReturn(Optional.of(startDate));
	        when(project.getDeliveryDate()).thenReturn(Optional.of(endDate));
	        return project;
	    }
	    
		private TouchTimeFilter getFilter() {
			ProjectFilterConfiguration project = configureProject();
	        
	        ProjectRangeByConfiguration defaultRange = new ProjectRangeByConfiguration(project);
	        TouchTimeFilter filter = new TouchTimeFilter(timezone,defaultRange);
			return filter;
		}
    	
    }

}
