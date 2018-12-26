package objective.taskboard.followup.budget;

import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import org.junit.Test;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.data.FollowupProgressCalculator;
import objective.taskboard.followup.data.ProgressData;
import objective.taskboard.followup.data.ProgressDataPoint;
import objective.taskboard.project.config.changeRequest.ChangeRequest;
import objective.taskboard.project.config.changeRequest.ChangeRequestService;

public class BudgetChartCalculatorTest {
    
    private ProjectFilterConfiguration project = mock( ProjectFilterConfiguration.class);
    private ZoneId systemDefault = ZoneId.systemDefault();

    @Test
    public void scopeDone_mustHaveActualScopeDoneValues() {
        
        BudgetChartCalculator subject = new BudgetChartCalculatorBuilder()
                .withStartDate("2018-10-21")
                .withEndDate("2018-10-22")
                .point().date("2018-10-21").progress(0).effortDone(42).effortBacklog(15).addPointToDone()
                .build();
        BudgetChartData data = subject.calculate(systemDefault, project);

        assertResults(data.scopeDone, "2018-10-21 | 42.0");
    }

    @Test
    public void scopeTotal_mustHaveActualScopeTotalValues() {

       BudgetChartCalculator subject = new BudgetChartCalculatorBuilder()
               .withStartDate("2018-10-20")
               .withEndDate("2018-10-22")
               .point().date("2018-10-21").progress(0).effortDone(42).effortBacklog(15).addPointToDone()
               .build();
        BudgetChartData data = subject.calculate(systemDefault, project);

        assertResults(data.scopeTotal, "2018-10-21 | 62.7");
    }

    @Test
    public void budget_mustHaveActualBudgetValues() {

       BudgetChartCalculator subject = new BudgetChartCalculatorBuilder()
               .withStartDate("2018-10-20")
               .withEndDate("2018-10-21")
               .point().date("2018-10-21").progress(0).effortDone(42).effortBacklog(15).addPointToDone()
               .budgetValue().name("ChangeRequest1").budgetDate("2018-10-21").budgetIncrease(25).isBaseline(false).addBudgetValue()
               .budgetValue().name("Baseline").budgetDate("2018-10-20").budgetIncrease(500).isBaseline(true).addBudgetValue()
               .build();
        BudgetChartData data = subject.calculate(systemDefault, project);

        assertResults(data.budget, 
            "2018-10-20 | 500.0",
            "2018-10-21 | 525.0"
        );
    }

    @Test
    public void scopeDonewithScopeDonePointsProjection_mustHaveActualProjectionValues() {
        
        BudgetChartCalculator subject = new BudgetChartCalculatorBuilder()
                .withStartDate("2018-10-21")
                .withEndDate("2018-10-23")
                .point().date("2018-10-21").progress(0).effortDone(42).effortBacklog(15).addPointToDone()
                .point().date("2018-10-21").progress(0.4).effortDone(40.0).effortBacklog(60.0).addPointToProjection()
                .point().date("2018-10-22").progress(0.5).effortDone(50.0).effortBacklog(50.0).addPointToProjection()
                .point().date("2018-10-23").progress(0.6).effortDone(60.0).effortBacklog(40.0).addPointToProjection()
                .build();

        BudgetChartData data = subject.calculate(systemDefault, project);

        assertResults(data.scopeDoneProjection,
                "2018-10-21 | 40.0",
                "2018-10-22 | 50.0",
                "2018-10-23 | 60.0"
        );
    }

    @Test
    public void scopeTotalProjection_mustHaveActualTotalProjectionValues() {
        BudgetChartCalculator subject = new BudgetChartCalculatorBuilder()
                .withStartDate("2018-10-21")
                .withEndDate("2018-10-23")
                .point().date("2018-10-21").progress(0.0).effortDone(42.0).effortBacklog(15.0).addPointToDone()
                .point().date("2018-10-21").progress(0.4).effortDone(40.0).effortBacklog(60.0).addPointToProjection()
                .point().date("2018-10-22").progress(0.5).effortDone(50.0).effortBacklog(70.0).addPointToProjection()
                .point().date("2018-10-23").progress(0.6).effortDone(60.0).effortBacklog(80.0).addPointToProjection()
                .build();

        BudgetChartData data = subject.calculate(systemDefault, project);

        assertResults(data.scopeTotalProjection,
                "2018-10-21 | 110.0",
                "2018-10-22 | 132.0",
                "2018-10-23 | 154.0"
        );
    }

    @Test
    public void scopeDoneProjection_mustHaveFinalProjectionDate() {
        BudgetChartCalculator subject = new BudgetChartCalculatorBuilder()
                .withStartDate("2018-10-21")
                .withEndDate("2018-10-26")
                .point().date("2018-10-21").progress(0.0).effortDone(42.0).effortBacklog(15.0).addPointToDone()
                .point().date("2018-10-21").progress(0.5).effortDone(50.0).effortBacklog(50.0).addPointToProjection()
                .point().date("2018-10-22").progress(0.6).effortDone(60.0).effortBacklog(40.0).addPointToProjection()
                .point().date("2018-10-23").progress(0.7).effortDone(70.0).effortBacklog(30.0).addPointToProjection()
                .point().date("2018-10-24").progress(0.8).effortDone(80.0).effortBacklog(20.0).addPointToProjection()
                .point().date("2018-10-25").progress(0.9).effortDone(90.0).effortBacklog(10.0).addPointToProjection()
                .point().date("2018-10-26").progress(1.0).effortDone(100.0).effortBacklog(0.0).addPointToProjection()
                .build();

        BudgetChartData data = subject.calculate(systemDefault, project);

        assertEquals(LocalDate.parse("2018-10-27"), data.projectionDate);
    }
    
    @Test
    public void scopeDoneProjectionStalled_mustHaveNoFinalProjectionDate() {
        BudgetChartCalculator subject = new BudgetChartCalculatorBuilder()
                .withStartDate("2018-10-21")
                .withEndDate("2018-10-23")
                .point().date("2018-10-21").progress(0.0).effortDone(42.0).effortBacklog(15.0).addPointToDone()
                .point().date("2018-10-21").progress(0.5).effortDone(50.0).effortBacklog(50.0).addPointToProjection()
                .point().date("2018-10-22").progress(0.5).effortDone(50.0).effortBacklog(50.0).addPointToProjection()
                .point().date("2018-10-23").progress(0.5).effortDone(50.0).effortBacklog(50.0).addPointToProjection()
                .build();

        BudgetChartData data = subject.calculate(systemDefault, project);

        assertNull( data.projectionDate);
    }

    private String budgetChartDataPointToString(BudgetChartDataPoint p) {
        return String.format(Locale.US, "%s | %.1f", 
                p.date,
                p.value);
    }

    private void assertResults(List<BudgetChartDataPoint> points, String... expectedBudgetChartDataPoints) {
        String actual = points.stream()
                .map(p -> budgetChartDataPointToString(p))
                .collect(joining("\n"));

        String expected = Stream.of(expectedBudgetChartDataPoints)
                .map(i -> i.replaceAll("\\s+", " "))
                .collect(joining("\n"));

        assertEquals(expected, actual);
    }

    private class BudgetChartCalculatorBuilder {
        private class ProgressDataPointDraft {
            private LocalDate date;
            private double progress;
            private double effortDone;
            private double effortBacklog;
        }

        private class ChangeRequestDraft {
            private String name;
            private LocalDate date;
            private int budgetIncrease;
            private boolean isBaseline;
        }

        private ChangeRequestService changeRequestService = mock(ChangeRequestService.class);
        private FollowupProgressCalculator calculator = mock(FollowupProgressCalculator.class);
        private List<ChangeRequest> changeRequests = new ArrayList<ChangeRequest>();
        private ProgressData value = new ProgressData();
        private ProgressDataPointDraft progressDataPointDraft;
        private ChangeRequestDraft changeRequestDraft;

        public BudgetChartCalculatorBuilder point() {
            progressDataPointDraft = new ProgressDataPointDraft();
            return this;
        }

        public BudgetChartCalculatorBuilder date(String date) {
            progressDataPointDraft.date = LocalDate.parse(date);
            return this;
        }
        
        public BudgetChartCalculatorBuilder progress (double progress) {
            progressDataPointDraft.progress = progress;
            return this;
        }
        
        public BudgetChartCalculatorBuilder effortDone(double effortDone) {
            progressDataPointDraft.effortDone = effortDone;
            return this;
        }
        
        public BudgetChartCalculatorBuilder effortBacklog(double effortBacklog) {
            progressDataPointDraft.effortBacklog = effortBacklog;
            return this;
        }
        
        public BudgetChartCalculatorBuilder budgetValue() {
            changeRequestDraft = new ChangeRequestDraft();
            return this;
        }
        
        public BudgetChartCalculatorBuilder name(String name) {
            changeRequestDraft.name = name;
            return this;
        }
        
        public BudgetChartCalculatorBuilder budgetDate(String date) {
            changeRequestDraft.date = LocalDate.parse(date);
            return this;
        }
        
        public BudgetChartCalculatorBuilder budgetIncrease(int budgetIncrease) {
            changeRequestDraft.budgetIncrease = budgetIncrease;
            return this;
        }
        
        public BudgetChartCalculatorBuilder isBaseline(boolean isBaseline) {
            changeRequestDraft.isBaseline = isBaseline;
            return this;
        }
        
        public BudgetChartCalculatorBuilder addPointToProjection() {
            value.actualProjection.add(new ProgressDataPoint(
                    progressDataPointDraft.date,
                    progressDataPointDraft.progress,
                    progressDataPointDraft.effortDone,
                    progressDataPointDraft.effortBacklog));
            return this;
        }

        public BudgetChartCalculatorBuilder addPointToDone() {
            value.actual.add(new ProgressDataPoint(
                    progressDataPointDraft.date,
                    progressDataPointDraft.progress,
                    progressDataPointDraft.effortDone,
                    progressDataPointDraft.effortBacklog));
            return this;
        }
        
        public BudgetChartCalculatorBuilder addBudgetValue() {
            changeRequests.add(new ChangeRequest(
                    project, 
                    changeRequestDraft.name, 
                    changeRequestDraft.date, 
                    changeRequestDraft.budgetIncrease, 
                    changeRequestDraft.isBaseline));
           return this;
        }
        
        public BudgetChartCalculatorBuilder withStartDate(String startDate) {
            value.startingDate = LocalDate.parse(startDate);
            return this;
        }

        public BudgetChartCalculatorBuilder withEndDate(String endDate) {
            value.endingDate = LocalDate.parse(endDate);
            return this;
        }

        public BudgetChartCalculator build() {
            when(calculator.calculateWithCompleteProjection(systemDefault, "PROJECT", 2)).thenReturn(value);
            when(changeRequestService.listByProject(project)).thenReturn(changeRequests);
            when(project.getProjectKey()).thenReturn("PROJECT");
            when(project.getProjectionTimespan()).thenReturn(2);
            when(project.getRiskPercentage()).thenReturn(new BigDecimal(10.0));
            return new BudgetChartCalculator(calculator, changeRequestService);
        }

    }
}
