package objective.taskboard.monitor;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.budget.BudgetChartCalculator;
import objective.taskboard.followup.budget.BudgetChartData;
import objective.taskboard.followup.data.FollowupProgressCalculator;
import objective.taskboard.followup.data.ProgressData;
import objective.taskboard.monitor.StrategicalProjectDataSet.MonitorData;

class MonitorCalculatorDSL {

    private final MonitorCalculator subject;

    private final BudgetChartCalculator budgetChartCalculatorMock;
    private final FollowupProgressCalculator followupProgressCalculatorMock;

    private final ProjectFilterConfiguration project = mock(ProjectFilterConfiguration.class);
    private final BudgetChartData budgetChartData = new BudgetChartData();
    private final ProgressData progressData = new ProgressData();
    private Optional<Exception> exception = Optional.empty();

    private MonitorData monitorDataResult;

    private static ZoneId timezone = ZoneId.of("America/Sao_Paulo");

    public static MonitorCalculatorDSL forTimeline() {
        BudgetChartCalculator budgetChartCalculatorMock = mock(BudgetChartCalculator.class);
        TimelineMonitorCalculator timelineSubject = new TimelineMonitorCalculator(budgetChartCalculatorMock);

        return new MonitorCalculatorDSL(timelineSubject, budgetChartCalculatorMock);
    }

    public static MonitorCalculatorDSL forCost() {
        FollowupProgressCalculator followupProgressCalculatorMock = mock(FollowupProgressCalculator.class);
        CostMonitorCalculator costSubject = new CostMonitorCalculator(followupProgressCalculatorMock);

        return new MonitorCalculatorDSL(costSubject, followupProgressCalculatorMock);
    }

    public static MonitorCalculatorDSL forScope() {
        FollowupProgressCalculator followupProgressCalculatorMock = mock(FollowupProgressCalculator.class);
        ScopeMonitorCalculator scopeSubject = new ScopeMonitorCalculator(followupProgressCalculatorMock);

        return new MonitorCalculatorDSL(scopeSubject, followupProgressCalculatorMock);
    }

    private MonitorCalculatorDSL(MonitorCalculator subject, 
            BudgetChartCalculator budgetChartCalculatorMock) {
        this.subject = subject;
        this.budgetChartCalculatorMock = budgetChartCalculatorMock;
        this.followupProgressCalculatorMock = mock(FollowupProgressCalculator.class);
    }
    
    private MonitorCalculatorDSL(MonitorCalculator subject, 
            FollowupProgressCalculator followupProgressCalculatorMock) {
        this.subject = subject;
        this.budgetChartCalculatorMock = mock(BudgetChartCalculator.class);
        this.followupProgressCalculatorMock = followupProgressCalculatorMock;
    }

    public MonitorCalculatorDSL projectWithRisk(double risk) {
        when(project.getRiskPercentage()).thenReturn(BigDecimal.valueOf(risk));
        return this;
    }

    public MonitorCalculatorDSL projectWithStartDate(String date) {
        LocalDate localDate = LocalDate.parse(date);

        when(project.getStartDate()).thenReturn(Optional.of(localDate));
        return this;
    }

    public MonitorCalculatorDSL projectWithDeliveryDate(String date) {
        LocalDate localDate = LocalDate.parse(date);

        when(project.getDeliveryDate()).thenReturn(Optional.of(localDate));
        return this;
    }

    public MonitorCalculatorDSL budgetChartwithProjectionDate(String date) {
        budgetChartData.projectionDate = LocalDate.parse(date);
        return this;
    }

    public MonitorCalculatorDSL givenServiceThrowing(Exception exception) {
        this.exception = Optional.of(exception);
        return this;
    }

    public MonitorCalculatorDSL progressDataWithActualProjection(ProgressDataPointBuilder ...dataPoints) {
        progressData.actualProjection = stream(dataPoints)
                .map(ProgressDataPointBuilder::build)
                .collect(toList());
        return this;
    }

    public MonitorCalculatorDSL progressDataWithExpected(ProgressDataPointBuilder ...dataPoints) {
        progressData.expected = stream(dataPoints)
                .map(ProgressDataPointBuilder::build)
                .collect(toList());
        return this;
    }

    public MonitorCalculatorDSL whenCalculate() {
        if (!exception.isPresent()) {
            when(budgetChartCalculatorMock.calculate(timezone, project)).thenReturn(budgetChartData);
            when(followupProgressCalculatorMock.calculateWithExpectedProjection(timezone, project.getProjectKey(), project.getProjectionTimespan())).thenReturn(progressData);
        } else {
            when(budgetChartCalculatorMock.calculate(timezone, project)).thenThrow(exception.get());
            when(followupProgressCalculatorMock.calculateWithExpectedProjection(timezone, project.getProjectKey(), project.getProjectionTimespan())).thenThrow(exception.get());
        }

        monitorDataResult = subject.calculate(project, timezone);
        return this;
    }

    public MonitorCalculatorAsserter then() {
        return new MonitorCalculatorAsserter(monitorDataResult);
    }
}
