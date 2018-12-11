package objective.taskboard.followup.budget;

import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;
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
                .withStartAndEndDate("2018-10-21", "2018-10-22")
                .withScopeDonePoint("2018-10-21", 0.0, 42.0, 15.0)
                .build();
        BudgetChartData data = subject.calculate(systemDefault, project);
        List<BudgetChartDataPoint> scopeDone = data.scopeDone;

        assertResults(scopeDone, "2018-10-21 | 42.0");
    }

    @Test
    public void scopeTotal_mustHaveActualScopeTotalValues() {

       BudgetChartCalculator subject = new BudgetChartCalculatorBuilder()
               .withStartAndEndDate("2018-10-20", "2018-10-22")
               .withScopeDonePoint("2018-10-21", 0.0, 42.0, 15.0)
               .build();
        BudgetChartData data = subject.calculate(systemDefault, project);
        List<BudgetChartDataPoint> scopeTotal = data.scopeTotal;

        assertResults(scopeTotal, "2018-10-21 | 62.7");
    }

    @Test
    public void budget_mustHaveActualBudgetValues() {

       BudgetChartCalculator subject = new BudgetChartCalculatorBuilder()
               .withStartAndEndDate("2018-10-20", "2018-10-21")
               .withScopeDonePoint("2018-10-21", 0.0, 42.0, 15.0)
               .withBudgetValue("ChangeRequest1", "2018-10-21", 25, false)
               .withBudgetValue("Baseline", "2018-10-20", 500, true)
               .build();
        BudgetChartData data = subject.calculate(systemDefault, project);
        
        List<BudgetChartDataPoint> budget = data.budget;
        
        assertResults(budget, 
            "2018-10-20 | 500.0",
            "2018-10-21 | 525.0"
        );
    }

    @Test
    public void scopeDoneProjection_mustHaveActualProjectionValues() {
        
        BudgetChartCalculator subject = new BudgetChartCalculatorBuilder()
                .withStartAndEndDate("2018-10-21", "2018-10-23")
                .withScopeDonePoint("2018-10-21", 0.0, 42.0, 15.0)
                .withScopeDoneProjectionPoint("2018-10-21",0.4, 40.0, 60.0)
                .withScopeDoneProjectionPoint("2018-10-22",0.5, 50.0, 50.0)
                .withScopeDoneProjectionPoint("2018-10-23",0.6, 60.0, 40.0)
                .build();

        BudgetChartData data = subject.calculate(systemDefault, project);
        List<BudgetChartDataPoint> scopeDoneProjection = data.scopeDoneProjection;

        assertResults(scopeDoneProjection,
                "2018-10-21 | 40.0",
                "2018-10-22 | 50.0",
                "2018-10-23 | 60.0"
        );
    }

    @Test
    public void scopeTotalProjection_mustHaveActualTotalProjectionValues() {
        BudgetChartCalculator subject = new BudgetChartCalculatorBuilder()
                .withStartAndEndDate("2018-10-21", "2018-10-23")
                .withScopeDonePoint("2018-10-21", 0.0, 42.0, 15.0)
                .withScopeDoneProjectionPoint("2018-10-21", 0.4, 40.0, 60.0)
                .withScopeDoneProjectionPoint("2018-10-22", 0.5, 50.0, 70.0)
                .withScopeDoneProjectionPoint("2018-10-23", 0.6, 60.0, 80.0)
                .build();

        BudgetChartData data = subject.calculate(systemDefault, project);
        List<BudgetChartDataPoint> scopeTotalProjection = data.scopeTotalProjection;

        assertResults(scopeTotalProjection,
                "2018-10-21 | 110.0",
                "2018-10-22 | 132.0",
                "2018-10-23 | 154.0"
        );
    }

    @Test
    public void scopeDoneProjection_mustHaveFinalProjectionDate() {
        BudgetChartCalculator subject = new BudgetChartCalculatorBuilder()
                .withStartAndEndDate("2018-10-21", "2018-10-26")
                .withScopeDonePoint("2018-10-21", 0.0, 42.0, 15.0)
                .withScopeDoneProjectionPoint("2018-10-21", 0.5, 50.0, 50.0)
                .withScopeDoneProjectionPoint("2018-10-22", 0.6, 60.0, 40.0)
                .withScopeDoneProjectionPoint("2018-10-23", 0.7, 70.0, 30.0)
                .withScopeDoneProjectionPoint("2018-10-24", 0.8, 80.0, 20.0)
                .withScopeDoneProjectionPoint("2018-10-25", 0.9, 90.0, 10.0)
                .withScopeDoneProjectionPoint("2018-10-26", 1.0, 100.0, 0.0)
                .build();

        BudgetChartData data = subject.calculate(systemDefault, project);

        assertEquals(LocalDate.parse("2018-10-27"), data.projectionDate);
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
        private ChangeRequestService changeRequestService = mock(ChangeRequestService.class);
        private FollowupProgressCalculator calculator = mock(FollowupProgressCalculator.class);
        private List<ChangeRequest> changeRequests = new ArrayList<ChangeRequest>();
        private ProgressData value = new ProgressData();
        
        public BudgetChartCalculatorBuilder withStartAndEndDate(String startDate, String endDate) {
            value.startingDate = LocalDate.parse(startDate);
            value.endingDate = LocalDate.parse(endDate);
            return this;
        }
        
        public BudgetChartCalculatorBuilder withScopeDonePoint(String date, double progress, double effortDone, double effortBacklog) {
            value.actual.add(new ProgressDataPoint(LocalDate.parse(date), progress, effortDone, effortBacklog));
            return this;
        }
        
        public BudgetChartCalculatorBuilder withBudgetValue(String name, String date, int budgetIncrease, boolean isBaseline ) {
            changeRequests.add(new ChangeRequest(project, name, LocalDate.parse(date), budgetIncrease, isBaseline));
            return this;
        }
        
        public BudgetChartCalculatorBuilder withScopeDoneProjectionPoint(String date, double progress, double effortDone, double effortBacklog) {
            value.actualProjection.add(new ProgressDataPoint(LocalDate.parse(date), progress, effortDone, effortBacklog));
            return this;
        }

        public BudgetChartCalculator build() {
            when(calculator.calculate(systemDefault, "PROJECT", 2, true)).thenReturn(value);
            when(changeRequestService.listByProject(project)).thenReturn(changeRequests);
            when(project.getProjectKey()).thenReturn("PROJECT");
            when(project.getProjectionTimespan()).thenReturn(2);
            when(project.getRiskPercentage()).thenReturn(new BigDecimal(10.0));
            return new BudgetChartCalculator(calculator, changeRequestService);
        }

    }
}
