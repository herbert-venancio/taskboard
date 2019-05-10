package objective.taskboard.followup.kpi.bugbyenvironment.filters;

import static objective.taskboard.followup.kpi.properties.KpiBugByEnvironmentMocker.noBugTypes;
import static objective.taskboard.followup.kpi.properties.KpiBugByEnvironmentMocker.withBugTypes;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.properties.KpiBugByEnvironmentProperties;
import objective.taskboard.followup.kpi.services.DSLKpi;

public class FilterOnlyBugsTest {

    @Test
    public void filterIssues_configuredType_happyDay() {
        DSLKpi context = 
                context().environment()
                    .givenSubtask("I-1")
                        .project("PROJ")
                        .type("Alpha Bug")
                        .withPreconfiguredTransition("Subtasks")
                     .eoI()
                    .givenSubtask("I-2")
                        .project("PROJ")
                        .type("Development")
                        .withPreconfiguredTransition("Subtasks")
                    .eoI()
                .eoE();
        
        filter()
            .givenContext(context)
            .withConfiguredTypes("Alpha Bug")
            .issue("I-1").shouldBeAccepted()
            .issue("I-2").shouldNotBeAccepted();
    }

    @Test
    public void filterIssues_withoutConfiguration() {
        DSLKpi context = 
                context().environment()
                    .givenSubtask("I-1")
                        .project("PROJ")
                        .type("Alpha Bug")
                        .withPreconfiguredTransition("Subtasks")
                     .eoI()
                    .givenSubtask("I-2")
                        .project("PROJ")
                        .type("Development")
                        .withPreconfiguredTransition("Subtasks")
                    .eoI()
                .eoE();
        
        filter()
            .givenContext(context)
            .withoutConfiguredTypes()
            .issue("I-1").shouldNotBeAccepted()
            .issue("I-2").shouldNotBeAccepted();
    }
    
    @Test
    public void filterIssues_withoutType() {
        DSLKpi context = 
                context().environment()
                    .givenSubtask("I-1")
                        .project("PROJ")
                        .emptyType()
                        .withPreconfiguredTransition("Subtasks")
                     .eoI()
                 .eoE();
        
        filter()
            .givenContext(context)
            .withConfiguredTypes("Alpha Bug")
            .issue("I-1").shouldNotBeAccepted();
    }

    private FilterBugs filter() {
        return new FilterBugs();
    }
    
    private class FilterBugs {
        
        private DSLKpi context;
        private IssueKpi issue;
        
        private FilterBugs givenContext(DSLKpi context) {
            this.context = context;
            return this;
        }
        
        public FilterBugs withConfiguredTypes(String... types) {
            this.context.environment().withKpiProperties(withBugTypes(types));
            return this;
        }
        
        public FilterBugs withoutConfiguredTypes() {
            this.context.environment().withKpiProperties(noBugTypes());
            return this;
        }

        private FilterBugs issue(String issueKey) {
            this.issue = context.getIssueKpi(issueKey);
            return this;
        }
        
        private FilterBugs assertIssue(IssueKpi issueKpi, boolean valueExpected) {
            Assertions.assertThat(getFilter().test(issueKpi)).isEqualTo(valueExpected);
            return this;
        }
        
        private FilterBugs shouldNotBeAccepted() {
            return assertIssue(issue,false);
        }
        
        private FilterBugs shouldBeAccepted() {
            return assertIssue(issue,true);
        }
        
        private FilterOnlyBugs getFilter() {
            KpiBugByEnvironmentProperties properties = context.environment().getKPIProperties(KpiBugByEnvironmentProperties.class);
            return new FilterOnlyBugs(properties.getBugTypes());
        }
    }
       
    private DSLKpi context() {
        return new DSLKpi()
                .environment()
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
                    .preConfigureTransitions("Features")
                        .status("Open").date("2019-01-01")
                        .status("To Do").date("2019-01-03")
                        .status("Doing").date("2019-01-05")
                        .status("Done").date("2019-01-07")
                    .eoSt()
                    .preConfigureTransitions("Subtasks")
                        .status("Open").date("2019-01-01")
                        .status("To Do").date("2019-01-02")
                        .status("Doing").date("2019-01-03")
                        .status("To Review").date("2019-01-04")
                        .status("Review").date("2019-01-05")
                        .status("Done").date("2019-01-04")
                    .eoSt()
                .eoE();
    }

}
