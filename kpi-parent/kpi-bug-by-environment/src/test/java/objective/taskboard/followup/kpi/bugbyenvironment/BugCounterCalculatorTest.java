package objective.taskboard.followup.kpi.bugbyenvironment;

import static objective.taskboard.followup.kpi.properties.KpiBugByEnvironmentMocker.withBugTypes;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.Range;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.filters.KpiWeekRange;
import objective.taskboard.followup.kpi.properties.KpiBugByEnvironmentProperties;
import objective.taskboard.followup.kpi.services.DSLKpi;
import objective.taskboard.followup.kpi.services.DSLSimpleBehaviorWithAsserter;
import objective.taskboard.followup.kpi.services.KpiEnvironment;
import objective.taskboard.jira.MetadataService;
import objective.taskboard.utils.RangeUtils;

public class BugCounterCalculatorTest {

    @Test
    public void allBugs_sameWeek_allCategorized() {
        given()
            .issue("I-1")
                .type("Bug")
                .toAppearOnWeek("Week 1")
                .withClientEnviroment("Production")
            .eoI()
            .issue("I-2")
                .type("Bug")
                .toAppearOnWeek("Week 1")
                .withClientEnviroment("Production")
            .eoI()
            .issue("I-3")
                .type("Alpha Bug")
                .toAppearOnWeek("Week 1")
                .withClientEnviroment("Alpha")
            .eoI()
            .issue("I-4")
                .type("Alpha Bug")
                .toAppearOnWeek("Week 1")
                .withClientEnviroment("Alpha")
            .eoI()
                .issue("I-5")
                .type("Alpha Bug")
                .toAppearOnWeek("Week 1")
                .withClientEnviroment("Alpha")
            .eoI()
        .when(
                countBugs().forWeek("Week 1")
            )
        .then()
            .totalOfKeySet(4)
            .key("Alpha").hasTotalOfBugs(3)
            .key("Production").hasTotalOfBugs(2)
            .key("Bug").hasNoBugs()
            .key("Alpha Bug").hasNoBugs();
    }

    @Test
    public void allBugs_sameWeek_withoutCategory() {
        given()
            .issue("I-1")
                .type("Bug")
                .toAppearOnWeek("Week 1")
                .withClientEnviroment("Production")
            .eoI()
            .issue("I-2")
                .type("Alpha Bug")
                .toAppearOnWeek("Week 1")
            .eoI()
            .issue("I-3")
                .type("Alpha Bug")
                .toAppearOnWeek("Week 1")
            .eoI()
            .issue("I-4")
                .type("Bug")
                .toAppearOnWeek("Week 1")
            .eoI()
            .issue("I-5")
                .type("Bug")
                .toAppearOnWeek("Week 1")
            .eoI()
            .issue("I-6")
                .type("Bug")
                .toAppearOnWeek("Week 1")
            .eoI()
        .when(
            countBugs().forWeek("Week 1")
            )
        .then()
            .totalOfKeySet(3)
            .key("Production").hasTotalOfBugs(1)
            .key("Alpha Bug").hasTotalOfBugs(2)
            .key("Bug").hasTotalOfBugs(3);
    }
    
    @Test
    public void filteringBugs_sameWeek_allCategorized() {
        given()
            .issue("I-1")
                .type("Development")
                .toAppearOnWeek("Week 1")
            .eoI()
            .issue("I-2")
                .type("Alpha Bug")
                .toAppearOnWeek("Week 1")
                .withClientEnviroment("Alpha")
            .eoI()
            .issue("I-3")
                .type("Alpha Bug")
                .toAppearOnWeek("Week 1")
                .withClientEnviroment("Alpha")
            .eoI()
            .issue("I-4")
                .type("Bug")
                .withClientEnviroment("Production")
                .toAppearOnWeek("Week 1")
            .eoI()
            .issue("I-5")
                .type("Development")
                .toAppearOnWeek("Week 1")
            .eoI()
        .when(
                countBugs().forWeek("Week 1")
            )
        .then()
            .totalOfKeySet(4)
            .key("Alpha").hasTotalOfBugs(2)
            .key("Production").hasTotalOfBugs(1)
            .key("Bug").hasNoBugs()
            .key("Alpha Bug").hasNoBugs();
    }
    
    @Test
    public void filteringBugs_sameWeek_withoutCategory() {
        given()
            .issue("I-1")
                .type("Development")
                .toAppearOnWeek("Week 1")
            .eoI()
            .issue("I-2")
                .type("Development")
                .toAppearOnWeek("Week 1")
            .eoI()
            .issue("I-3")
                .type("Development")
                .toAppearOnWeek("Week 1")
            .eoI()
            .issue("I-4")
                .type("Alpha Bug")
                .toAppearOnWeek("Week 1")
            .eoI()
            .issue("I-5")
                .type("Alpha Bug")
                .toAppearOnWeek("Week 1")
            .eoI()
            .issue("I-6")
                .type("Bug")
                .withClientEnviroment("Production")
                .toAppearOnWeek("Week 1")
            .eoI()
            .issue("I-7")
                .type("Development")
                .toAppearOnWeek("Week 1")
            .eoI()
            .issue("I-8")
                .type("Alpha Test")
                .toAppearOnWeek("Week 1")
            .eoI()
        .when(
                countBugs().forWeek("Week 1")
            )
        .then()
            .totalOfKeySet(3)
            .key("Production").hasTotalOfBugs(1)
            .key("Bug").hasNoBugs()
            .key("Alpha Bug").hasTotalOfBugs(2);
    }
    
    @Test
    public void allBugs_differentWeeks_allCategorized() {
        given()
            .issue("I-1")
                .type("Bug")
                .withClientEnviroment("Production")
                .toAppearOnWeek("Week 1")
            .eoI()
            .issue("I-2")
                .type("Alpha Bug")
                .withClientEnviroment("Alpha")
                .toAppearOnWeek("Week 3")
            .eoI()
            .issue("I-3")
                .type("Alpha Bug")
                .withClientEnviroment("Alpha")
                .toAppearOnWeek("Week 3")
            .eoI()
            .issue("I-4")
                .type("Bug")
                .withClientEnviroment("Production")
                .toAppearOnWeek("Week 2")
            .eoI()
            .issue("I-5")
                .type("Alpha Bug")
                .withClientEnviroment("Alpha")
                .toAppearOnWeek("Week 2")
            .eoI()
            .issue("I-6")
                .type("Bug")
                .withClientEnviroment("Production")
                .toAppearOnWeek("Week 2")
            .eoI()
            .issue("I-7")
                .type("Alpha Bug")
                .withClientEnviroment("Alpha")
                .toAppearOnWeek("Week 2")
            .eoI()
            .issue("I-8")
                .type("Alpha Bug")
                .withClientEnviroment("Alpha")
                .toAppearOnWeek("Week 2")
            .eoI()
        .when(
                countBugs().forWeek("Week 2")
            )
        .then()
            .totalOfKeySet(4)
            .key("Production").hasTotalOfBugs(2)
            .key("Alpha").hasTotalOfBugs(3)
            .key("Alpha Bug").hasNoBugs()
            .key("Bug").hasNoBugs();
    }
    
    @Test
    public void allBugs_differentWeeks_withoutCategory() {
        given()
            .issue("I-1")
                .type("Bug")
                .withClientEnviroment("Production")
                .toAppearOnWeek("Week 1")
            .eoI()
            .issue("I-2")
                .type("Alpha Bug")
                .toAppearOnWeek("Week 3")
            .eoI()
            .issue("I-3")
                .type("Alpha Bug")
                .toAppearOnWeek("Week 3")
            .eoI()
            .issue("I-4")
                .type("Bug")
                .toAppearOnWeek("Week 2")
            .eoI()
            .issue("I-5")
                .type("Bug")
                .toAppearOnWeek("Week 2")
            .eoI()
            .issue("I-6")
                .type("Bug")
                .withClientEnviroment("Production")
                .toAppearOnWeek("Week 2")
            .eoI()
            .issue("I-7")
                .type("Alpha Bug")
                .toAppearOnWeek("Week 2")
            .eoI()
            .issue("I-8")
                .type("Alpha Bug")
                .toAppearOnWeek("Week 2")
            .eoI()
        .when(
                countBugs().forWeek("Week 2")
            )
        .then()
            .totalOfKeySet(3)
            .key("Production").hasTotalOfBugs(1)
            .key("Alpha Bug").hasTotalOfBugs(2)
            .key("Bug").hasTotalOfBugs(2);
    }
    
    @Test
    public void filteringBugs_differentWeeks_allCategorized() {
        given()
            .issue("I-1")
                .type("Development")
                .toAppearOnWeek("Week 1")
            .eoI()
            .issue("I-2")
                .type("Alpha Bug")
                .withClientEnviroment("Alpha")
                .toAppearOnWeek("Week 2")
            .eoI()
            .issue("I-3")
                .type("Alpha Bug")
                .withClientEnviroment("Alpha")
                .toAppearOnWeek("Week 2")
            .eoI()
            .issue("I-4")
                .type("Development")
                .toAppearOnWeek("Week 2")
            .eoI()
            .issue("I-5")
                .type("Alpha Bug")
                .withClientEnviroment("Alpha")
                .toAppearOnWeek("Week 3")
            .eoI()
            .issue("I-6")
                .type("Alpha Bug")
                .withClientEnviroment("Alpha")
                .toAppearOnWeek("Week 3")
            .eoI()
            .issue("I-7")
                .type("Development")
                .toAppearOnWeek("Week 3")
            .eoI()
            .issue("I-8")
                .type("Alpha Bug")
                .withClientEnviroment("Alpha")
                .toAppearOnWeek("Week 2")
            .eoI()
            .issue("I-9")
                .type("Bug")
                .withClientEnviroment("Production")
                .toAppearOnWeek("Week 2")
            .eoI()
            .issue("I-10")
                .type("Bug")
                .withClientEnviroment("Production")
                .toAppearOnWeek("Week 2")
            .eoI()
        .when(
                countBugs().forWeek("Week 2")
            )
        .then()
            .totalOfKeySet(4)
            .key("Production").hasTotalOfBugs(2)
            .key("Alpha").hasTotalOfBugs(3)
            .key("Bug").hasNoBugs()
            .key("Alpha Bug").hasNoBugs();
    }
    
    @Test
    public void filteringBugs_differenteWeeks_withoutCategory() {
        given()
            .issue("I-1")
                .type("Development")
                .toAppearOnWeek("Week 1")
            .eoI()
            .issue("I-2")
                .type("Alpha Bug")
                .toAppearOnWeek("Week 2")
            .eoI()
            .issue("I-3")
                .type("Bug")
                .withClientEnviroment("Production")
                .toAppearOnWeek("Week 3")
            .eoI()
            .issue("I-4")
                .type("Bug")
                .toAppearOnWeek("Week 3")
            .eoI()
            .issue("I-5")
                .type("Alpha Bug")
                .toAppearOnWeek("Week 2")
            .eoI()
            .issue("I-6")
                .type("Alpha Bug")
                .toAppearOnWeek("Week 3")
            .eoI()
            .issue("I-7")
                .type("Bug")
                .withClientEnviroment("Production")
                .toAppearOnWeek("Week 2")
            .eoI()
            .issue("I-8")
                .type("Bug")
                .toAppearOnWeek("Week 2")
            .eoI()
            .issue("I-9")
                .type("Bug")
                .toAppearOnWeek("Week 2")
            .eoI()
            .issue("I-10")
                .type("Bug")
                .toAppearOnWeek("Week 2")
            .eoI()
        .when(
                countBugs().forWeek("Week 2")
            )
        .then()
            .totalOfKeySet(3)
            .key("Production").hasTotalOfBugs(1)
            .key("Alpha Bug").hasTotalOfBugs(2)
            .key("Bug").hasTotalOfBugs(3);
    }
    
    @Test
    public void allBugs_sameWeek_noCategoryConfigured() {
        given()
            .emptyEnvronmentFieldProperty()
            .issue("I-1")
                .type("Bug")
                .toAppearOnWeek("Week 1")
            .eoI()
            .issue("I-2")
                .type("Bug")
                .withClientEnviroment("Production")
                .toAppearOnWeek("Week 1")
            .eoI()
            .issue("I-3")
                .type("Alpha Bug")
                .withClientEnviroment("Alpha")
                .toAppearOnWeek("Week 1")
            .eoI()
            .issue("I-4")
                .type("Alpha Bug")
                .toAppearOnWeek("Week 1")
            .eoI()
        .when(
                countBugs().forWeek("Week 1")
            )
        .then()
            .totalOfKeySet(2)
            .key("Alpha Bug").hasTotalOfBugs(2)
            .key("Bug").hasTotalOfBugs(2);
    }
    
    @Test
    public void allBugs_allClosed_withoutCategory() {
        given()
            .issue("I-1")
                .type("Bug")
                .toAppearOnWeek("Week 1")
            .eoI()
            .issue("I-2")
                .type("Bug")
                .withClientEnviroment("Production")
                .toAppearOnWeek("Week 1")
            .eoI()
            .issue("I-3")
                .type("Alpha Bug")
                .withClientEnviroment("Alpha")
                .toAppearOnWeek("Week 1")
            .eoI()
            .issue("I-4")
                .type("Alpha Bug")
                .toAppearOnWeek("Week 1")
            .eoI()
        .when(
                countBugs().forWeek("Week 2")
            )
        .then()
            .totalOfKeySet(4)
            .key("Production").hasNoBugs()
            .key("Alpha").hasNoBugs()
            .key("Alpha Bug").hasNoBugs()
            .key("Bug").hasNoBugs();
    }
    
    @Test
    public void filteringBugs_allClosed_worklogInPreviousWeek_allCategorized() {
        given()
            .issue("I-1")
                .type("Bug")
                .withClientEnviroment("Production")
                .toAppearOnWeek("Week 3")
            .eoI()
            .issue("I-2")
                .type("Development")
                .toAppearOnWeek("Week 3")
            .eoI()
            .issue("I-3")
                .type("Alpha Bug")
                .withClientEnviroment("Alpha")
                .toAppearOnWeek("Week 3")
            .eoI()
            .issue("I-4")
                .type("Alpha Bug")
                .worklogAt("2019-04-11")
                .toAppearOnWeek("Week 3")
            .eoI()
        .when(
                countBugs().forWeek("Week 2")
            )
        .then()
            .totalOfKeySet(4)
            .key("Production").hasNoBugs()
            .key("Alpha").hasNoBugs()
            .key("Alpha Bug").hasTotalOfBugs(1)
            .key("Bug").hasNoBugs();
    }
    
    private InnerContext given(){
        return new InnerContext();
    }
    
    private class InnerContext {
        DSLKpi context;
        
        private InnerContext() {
            this.context = buildStartConfiguration();
        }
        
        private InnerContext emptyEnvronmentFieldProperty() {
            context.environment().withKpiProperties().environmentField("");
            return this;
        }
        public CalculateBugCounter when(CalculateBugCounter calculateBugs) {
            return (CalculateBugCounter) context.when().appliesBehavior(calculateBugs);
            
        }
        private IssueConfiguration issue(String issueKey) {
            return new IssueConfiguration(issueKey);
        }
        
        private DSLKpi buildStartConfiguration() {
            return new DSLKpi()
                    .environment()
                        .types()
                            .addSubtasks("Bug","Alpha Bug","Development","Alpha Test")
                        .eoT()
                        .statuses()
                            .withNotProgressingStatuses("Open","To Do","Done")
                            .withProgressingStatuses("Doing")
                        .eoS()
                        .withKpiProperties(withBugTypes("Bug","Alpha Bug"))
                        .withKpiProperties()
                            .environmentField("clientEnvironment")
                        .eoKP()
                        .preConfigureTransitions("Week 1")
                            .status("Open").date("2019-03-31")
                            .status("To Do").date("2019-04-02")
                            .status("Doing").date("2019-04-04")
                            .status("Done").date("2019-04-05")
                        .eoSt()
                        .preConfigureTransitions("Week 2")
                            .status("Open").date("2019-04-10")
                            .status("To Do").date("2019-04-11")
                            .status("Doing").date("2019-04-11")
                            .status("Done").date("2019-04-12")
                        .eoSt()
                        .preConfigureTransitions("Week 3")
                            .status("Open").date("2019-04-16")
                            .status("To Do").date("2019-04-18")
                            .status("Doing").date("2019-04-19")
                            .status("Done").date("2019-04-20")
                        .eoSt()
                    .eoE();        
        }

        
        private class IssueConfiguration {

            private String type;
            private String week;
            private String clientEnvironment;
            private String issueKey;
            private Optional<String> dateOfWorklog = Optional.empty(); 
            
            private IssueConfiguration(String issueKey) {
                this.issueKey = issueKey;
            }
            
            private IssueConfiguration worklogAt(String date) {
                this.dateOfWorklog = Optional.of(date);
                return this;
            }

            private IssueConfiguration type(String type) {
                this.type = type;
                return this;
            }
            
            private IssueConfiguration toAppearOnWeek(String week) {
                this.week = week;
                return this;
            }
            
            private IssueConfiguration withClientEnviroment(String clientEnvironment) {
                this.clientEnvironment = clientEnvironment;
                return this;
            }
            
            private InnerContext eoI() {
                configureIssue();
                return InnerContext.this;
            }

            private void configureIssue() {
                context
                    .environment()
                        .givenSubtask(issueKey)
                            .type(type)
                            .project("PROJ")
                            .withPreconfiguredTransition(week)
                            .fields()
                                .field("clientEnvironment").value(clientEnvironment)
                            .eoF();
                
                dateOfWorklog.ifPresent(this::prepareWorklog);
            }
            
            private void prepareWorklog(String date) {
                context
                    .environment()
                        .givenSubtask(issueKey)
                        .worklogs()
                            .at(date).timeSpentInHours(1.0)
                        .eoW();
                        
            }
            
        }
    }
    
    private CalculateBugCounter countBugs() {
        return new CalculateBugCounter();
    }
    
    
    private class CalculateBugCounter implements DSLSimpleBehaviorWithAsserter<BugCounterAsserter>{

        private BugCounterAsserter asserter;
        
        private Map<String,Range<LocalDate>> weeks = new LinkedHashMap<>();
        private String weekSelected;
        
        private CalculateBugCounter() {
            weeks.put("Week 1", getRange("2019-03-31","2019-04-06"));
            weeks.put("Week 2", getRange("2019-04-07","2019-04-13"));
            weeks.put("Week 3", getRange("2019-04-14","2019-04-20"));
        }
        
        private CalculateBugCounter forWeek(String week) {
            this.weekSelected = week;
            return this;
        }
        
        private Range<LocalDate> getRange(String start, String end) {
            return RangeUtils.between(LocalDate.parse(start), LocalDate.parse(end));
        }

        @Override
        public void behave(KpiEnvironment environment) {
            KpiBugByEnvironmentProperties properties = environment.getKPIProperties(KpiBugByEnvironmentProperties.class);
            MetadataService metadataService = environment.services().metadata().getService();

            List<IssueKpi> issues = environment.services().issueKpi().getAllIssues();
            KpiWeekRange weekRange = getWeekRange(environment.getTimezone());
            
            BugCounterCalculator subject = new BugCounterCalculator(metadataService,properties, issues);
            this.asserter = new BugCounterAsserter(subject.getBugsCategorizedOnWeek(weekRange));
        }

        private KpiWeekRange getWeekRange(ZoneId timezone) {
            Range<LocalDate> range = weeks.get(weekSelected);
            return new KpiWeekRange(range, timezone);
        }

        @Override
        public BugCounterAsserter then() {
            return this.asserter;
        }
        
    }
    
    private class BugCounterAsserter {

        private Map<String, Long> subject;

        private BugCounterAsserter(Map<String, Long> bugsCategorizedOnWeek) {
            this.subject = bugsCategorizedOnWeek;
        }

        public ValueAsserter key(String key) {
            return new ValueAsserter(Optional.ofNullable(subject.get(key)));
        }

        private BugCounterAsserter totalOfKeySet(int size) {
            Assertions.assertThat(subject.keySet()).hasSize(size);
            return this;
        }

        private class ValueAsserter {

            private Optional<Long> count;
            
            private ValueAsserter(Optional<Long> maybeValue) {
                this.count = maybeValue;
            }

            private BugCounterAsserter hasNoBugs() {
                Assertions.assertThat(count).hasValue(0l);
                return BugCounterAsserter.this;
            }

            private BugCounterAsserter hasTotalOfBugs(long total) {
                Assertions.assertThat(count).hasValue(total);
                return BugCounterAsserter.this;
            }
        }
       
    }

}