package objective.taskboard.followup.kpi.bugbyenvironment.filters;

import java.time.LocalDate;
import java.time.ZoneId;

import org.apache.commons.lang3.Range;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.filters.KpiWeekRange;
import objective.taskboard.followup.kpi.services.DSLKpi;
import objective.taskboard.followup.kpi.services.IssueKpiMocker;
import objective.taskboard.utils.RangeUtils;

public class FilterNotClosedIssuesOnWeekTest {
    
    private static ZoneId ZONE_ID = ZoneId.systemDefault();
    
    @Test
    public void issueClosedBeforeWeek_thenShouldNotBeSelected() {
        IssueKpi issueKpi =  
                givenIssue("I-1")
                        .project("PROJ")
                        .type("Alpha Bug")
                        .withTransitions()
                            .status("Open").date("2019-03-23")
                            .status("To Do").date("2019-03-24")
                            .status("Doing").date("2019-03-25")
                            .status("Done").date("2019-03-26")
                        .eoT()
                     .buildIssueKpi();
        filter()
            .from("2019-03-31").to("2019-04-05")
            .issue(issueKpi).shouldNotBeSelected();
    }
    
    @Test
    public void issueClosedDuringWeek_thenShouldBeSelected() {
        IssueKpi issueKpi =  
                givenIssue("I-1")
                        .project("PROJ")
                        .type("Alpha Bug")
                        .withTransitions()
                            .status("Open").date("2019-03-25")
                            .status("To Do").date("2019-03-27")
                            .status("Doing").date("2019-03-31")
                            .status("Done").date("2019-04-03")
                        .eoT()
                     .buildIssueKpi();
        filter()
            .from("2019-03-31").to("2019-04-05")
            .issue(issueKpi).shouldBeSelected();
    }
    
    @Test
    public void issueClosedAfterWeek_thenShouldBeSelected() {
        IssueKpi issueKpi =  
                givenIssue("I-1")
                        .project("PROJ")
                        .type("Alpha Bug")
                        .withTransitions()
                            .status("Open").date("2019-03-25")
                            .status("To Do").date("2019-03-27")
                            .status("Doing").date("2019-03-31")
                            .status("Done").date("2019-04-12")
                        .eoT()
                     .buildIssueKpi();
        filter()
            .from("2019-03-31").to("2019-04-05")
            .issue(issueKpi).shouldBeSelected();
    }
    
    @Test
    public void issueOpenAfterWeek_thenShouldNotBeSelected() {
        IssueKpi issueKpi =  
                givenIssue("I-1")
                        .project("PROJ")
                        .type("Alpha Bug")
                        .withTransitions()
                            .status("Open").date("2019-04-10")
                            .status("To Do").noDate()
                            .status("Doing").noDate()
                            .status("Done").noDate()
                        .eoT()
                     .buildIssueKpi();
        filter()
            .from("2019-03-31").to("2019-04-05")
            .issue(issueKpi).shouldNotBeSelected();
    }
    
    @Test
    public void issueOpenDuringWeek_thenShouldBeSelected() {
        IssueKpi issueKpi =  
                givenIssue("I-1")
                        .project("PROJ")
                        .type("Alpha Bug")
                        .withTransitions()
                            .status("Open").date("2019-04-02")
                            .status("To Do").noDate()
                            .status("Doing").noDate()
                            .status("Done").noDate()
                        .eoT()
                     .buildIssueKpi();
        filter()
            .from("2019-03-31").to("2019-04-05")
            .issue(issueKpi).shouldBeSelected();
    }
    
    @Test
    public void issueOpenBeforeWeek_thenShouldBeSelected() {
        IssueKpi issueKpi =  
                givenIssue("I-1")
                        .project("PROJ")
                        .type("Alpha Bug")
                        .withTransitions()
                            .status("Open").date("2019-03-23")
                            .status("To Do").noDate()
                            .status("Doing").noDate()
                            .status("Done").noDate()
                        .eoT()
                     .buildIssueKpi();
        filter()
            .from("2019-03-31").to("2019-04-05")
            .issue(issueKpi).shouldBeSelected();
    }
    
    @Test
    public void issueClosedBeforeWeek_withWorklogDuringWeek_thenShouldBeSelected() {
        IssueKpi issueKpi =  
                givenIssue("I-1")
                        .project("PROJ")
                        .type("Alpha Bug")
                        .withTransitions()
                            .status("Open").date("2019-03-23")
                            .status("To Do").date("2019-03-24")
                            .status("Doing").date("2019-03-25")
                            .status("Done").date("2019-03-26")
                        .eoT()
                        .worklogs()
                            .at("2019-31-03").timeSpentInHours(1.0)
                        .eoW()
                     .buildIssueKpi();
        filter()
            .from("2019-03-31").to("2019-04-05")
            .issue(issueKpi).shouldBeSelected();
    }
    
    @Test
    public void issueOpenAfterWeek_withWorklogDuringWeek_thenShouldBeSelected() {
        IssueKpi issueKpi =  
                givenIssue("I-1")
                        .project("PROJ")
                        .type("Alpha Bug")
                        .withTransitions()
                            .status("Open").date("2019-04-10")
                            .status("To Do").noDate()
                            .status("Doing").noDate()
                            .status("Done").noDate()
                        .eoT()
                        .worklogs()
                            .at("2019-04-02").timeSpentInHours(1.0)
                        .eoW()
                     .buildIssueKpi();
        filter()
            .from("2019-03-31").to("2019-04-05")
            .issue(issueKpi).shouldBeSelected();
    }

    @Test
    public void issueOpenBeforeWeek_inProgress_thenShouldBeSelected() {
        IssueKpi issueKpi =  
                givenIssue("I-1")
                        .project("PROJ")
                        .type("Alpha Bug")
                        .withTransitions()
                            .status("Open").date("2019-03-23")
                            .status("To Do").noDate()
                            .status("Doing").date("2019-04-02")
                            .status("Done").noDate()
                        .eoT()
                     .buildIssueKpi();
        filter()
            .from("2019-03-31").to("2019-04-05")
            .issue(issueKpi).shouldBeSelected();
    }
    
    @Test
    public void issueOpenAfterWeek_inProgress_withWorklogDuringWeek_thenShouldBeSelected() {
        IssueKpi issueKpi =  
                givenIssue("I-1")
                        .project("PROJ")
                        .type("Alpha Bug")
                        .withTransitions()
                            .status("Open").date("2019-03-23")
                            .status("To Do").noDate()
                            .status("Doing").date("2019-04-02")
                            .status("Done").noDate()
                        .eoT()
                        .worklogs()
                            .at("2019-04-02").timeSpentInHours(1.0)
                        .eoW()
                     .buildIssueKpi();
        filter()
            .from("2019-03-31").to("2019-04-05")
            .issue(issueKpi).shouldBeSelected();
    }
    
    @Test
    public void issueOpenOnWeek_inProgressAfterWeek_thenIssueShouldBeSelected() {
        IssueKpi issueKpi =  
                givenIssue("I-1")
                        .project("PROJ")
                        .type("Alpha Bug")
                        .withTransitions()
                            .status("Open").date("2019-03-23")
                            .status("To Do").date("2019-04-06")
                            .status("Doing").date("2019-04-08")
                            .status("Done").date("2019-04-12")
                        .eoT()
                     .buildIssueKpi();
        filter()
            .from("2019-03-31").to("2019-04-05")
            .issue(issueKpi).shouldBeSelected();
    }
    
    private FilterNotClosed filter() {
        return new FilterNotClosed();
    }
    
    private class FilterNotClosed {
        
        private String startOfWeek;
        private String endOfWeek;
        private IssueKpi issueKpi;
        
        public FilterNotClosed from(String startOfWeek) {
            this.startOfWeek = startOfWeek;
            return this;
        }
        
        public FilterNotClosed to(String endOfWeek) {
            this.endOfWeek = endOfWeek;
            return this;
        }

        private FilterNotClosed issue(IssueKpi issueKpi) {
            this.issueKpi = issueKpi;
            return this;
        }
        
        private FilterNotClosed assertIssue(boolean valueExpected) {
            Assertions.assertThat(getFilter().test(issueKpi)).isEqualTo(valueExpected);
            return this;
        }
        
        private FilterNotClosedIssuesOnWeek getFilter() {
            Range<LocalDate> dateRange = RangeUtils.between(LocalDate.parse(startOfWeek), LocalDate.parse(endOfWeek)); 
            KpiWeekRange weekRange = new KpiWeekRange(dateRange, ZONE_ID); 
            return new FilterNotClosedIssuesOnWeek(weekRange);
        }
        

        private FilterNotClosed shouldNotBeSelected() {
            return assertIssue(false);
        }

        private FilterNotClosed shouldBeSelected() {
            return assertIssue(true);
        }
    }
    
    private IssueKpiMocker givenIssue(String issueKey) {
        return new DSLKpi()
                .environment()
                    .withTimezone(ZONE_ID)
                    .types()
                        .addDemand("Demand")
                        .addFeatures("Bug","Feature")
                        .addSubtasks("Alpha Bug","Development")
                    .eoT()
                    .statuses()
                        .withNotProgressingStatuses("Open","To Do","To Review", "Done","Cancelled")
                        .withProgressingStatuses("Doing","Review")
                    .eoS()
                    .withKpiProperties()
                        .environmentField("clientEnvironment")
                    .eoKP()
                    .givenSubtask(issueKey);
    }

}
