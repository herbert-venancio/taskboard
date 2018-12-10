package objective.taskboard.followup.budget;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.data.FollowupProgressCalculator;
import objective.taskboard.followup.data.ProgressData;
import objective.taskboard.followup.data.ProgressDataPoint;
import objective.taskboard.project.config.changeRequest.ChangeRequest;
import objective.taskboard.project.config.changeRequest.ChangeRequestService;

public class BudgetChartCalculatorTest {
    
    private ProjectFilterConfiguration project = mock( ProjectFilterConfiguration.class);
    private ChangeRequestService changeRequestService = mock(ChangeRequestService.class);
    private FollowupProgressCalculator calculator = mock(FollowupProgressCalculator.class);
    private ZoneId systemDefault = ZoneId.systemDefault();
    private LocalDate firstDate = LocalDate.now(); 
    private LocalDate secondDate = firstDate.plusDays(1);
    private ProgressData value = new ProgressData();

    @Before
    public void initialize() {

        value.startingDate = firstDate;
        value.endingDate = secondDate;
        value.actual = Arrays.asList(new ProgressDataPoint(firstDate, 0.0,42.0,15.0));

        List<ChangeRequest> changeRequests = Arrays.asList(
                new ChangeRequest(project,"ChangeRequest1",secondDate, 25, false),
                new ChangeRequest(project,"Baseline",firstDate, 500, true)
        );

        when(calculator.calculate(systemDefault, "PROJECT", 2, true)).thenReturn(value);
        when(changeRequestService.listByProject(project)).thenReturn(changeRequests);
        when(project.getProjectKey()).thenReturn("PROJECT");
        when(project.getProjectionTimespan()).thenReturn(2);
        when(project.getRiskPercentage()).thenReturn(new BigDecimal(10.0));
    }
    
    @Test
    public void scopeDone_mustHaveActualScopeDoneValues() {
        
        BudgetChartCalculator subject = new BudgetChartCalculator(calculator, changeRequestService);
        BudgetChartData data = subject.calculate(systemDefault, project);
        List<BudgetChartDataPoint> scopeDone = data.scopeDone;
        
        assertEquals(firstDate, scopeDone.get(0).date);
        assertEquals(42, scopeDone.get(0).value, .01);
    }

    @Test
    public void scopeTotal_mustHaveActualScopeTotalValues() {

        BudgetChartCalculator subject = new BudgetChartCalculator(calculator, changeRequestService);
        BudgetChartData data = subject.calculate(systemDefault, project);
        List<BudgetChartDataPoint> scopeTotal = data.scopeTotal;

        assertEquals(firstDate, scopeTotal.get(0).date);
        assertEquals(62.7, scopeTotal.get(0).value, .01);
    }

    @Test
    public void budget_mustHaveActualBudgetValues() {

        BudgetChartCalculator subject = new BudgetChartCalculator(calculator, changeRequestService);
        BudgetChartData data = subject.calculate(systemDefault, project);
        
        List<BudgetChartDataPoint> budget = data.budget;
        
        assertEquals(firstDate, budget.get(0).date);
        assertEquals(500, budget.get(0).value, .01);
        assertEquals(secondDate, budget.get(1).date);
        assertEquals(525, budget.get(1).value, .01);
    }

    @Test
    public void scopeDoneProjection_mustHaveActualProjectionValues() {
        
        value.actualProjection = Arrays.asList(
                new ProgressDataPoint(firstDate, 0.4, 40.0, 60.0),
                new ProgressDataPoint(firstDate.plusDays(1l), 0.5, 50.0, 50.0),
                new ProgressDataPoint(firstDate.plusDays(2l), 0.6, 60.0, 40.0)
        );

        BudgetChartCalculator subject = new BudgetChartCalculator(calculator, changeRequestService);
        BudgetChartData data = subject.calculate(systemDefault, project);
        List<BudgetChartDataPoint> scopeDoneProjection = data.scopeDoneProjection;

        assertEquals(firstDate, scopeDoneProjection.get(0).date);
        assertEquals(40, scopeDoneProjection.get(0).value, .01);
        assertEquals(firstDate.plusDays(1l), scopeDoneProjection.get(1).date);
        assertEquals(50, scopeDoneProjection.get(1).value, .01);

    }

    @Test
    public void scopeTotalProjection_mustHaveActualTotalProjectionValues() {
        
        value.actualProjection = Arrays.asList(
                new ProgressDataPoint(firstDate, 0.4, 40.0, 60.0),
                new ProgressDataPoint(firstDate.plusDays(1l), 0.5, 50.0, 70.0),
                new ProgressDataPoint(firstDate.plusDays(2l), 0.6, 60.0, 80.0)
        );

        BudgetChartCalculator subject = new BudgetChartCalculator(calculator, changeRequestService);
        BudgetChartData data = subject.calculate(systemDefault, project);
        List<BudgetChartDataPoint> scopeTotalProjection = data.scopeTotalProjection;

        assertEquals(firstDate, scopeTotalProjection.get(0).date);
        assertEquals(110, scopeTotalProjection.get(0).value, .01);
        assertEquals(firstDate.plusDays(1l), scopeTotalProjection.get(1).date);
        assertEquals(132, scopeTotalProjection.get(1).value, .01);

    }

    @Test
    public void scopeDoneProjection_mustHaveFinalProjectionDate() {
        value.actualProjection = Arrays.asList(
                new ProgressDataPoint(firstDate.plusDays(1l), 0.5, 50.0, 50.0),
                new ProgressDataPoint(firstDate.plusDays(2l), 0.6, 60.0, 40.0),
                new ProgressDataPoint(firstDate.plusDays(3l), 0.7, 70.0, 30.0),
                new ProgressDataPoint(firstDate.plusDays(4l), 0.8, 80.0, 20.0),
                new ProgressDataPoint(firstDate.plusDays(5l), 0.9, 90.0, 10.0),
                new ProgressDataPoint(firstDate.plusDays(6l), 1.0, 100.0, 0.0)
        );

        BudgetChartCalculator subject = new BudgetChartCalculator(calculator, changeRequestService);
        BudgetChartData data = subject.calculate(systemDefault, project);

        assertEquals(firstDate.plusDays(6l), data.projectionDate);
    }
}
