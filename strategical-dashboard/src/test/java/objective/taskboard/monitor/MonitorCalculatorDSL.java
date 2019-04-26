package objective.taskboard.monitor;

import static java.util.Arrays.asList;
import static objective.taskboard.monitor.MonitorCalculator.CANT_CALCULATE_MESSAGE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.budget.BudgetChartData;
import objective.taskboard.followup.data.ProgressData;
import objective.taskboard.followup.data.ProgressDataPoint;
import objective.taskboard.monitor.StrategicalProjectDataSet.MonitorData;

class MonitorCalculatorDSL {

    private final MonitorCalculator subject;

    private final MonitorDataService monitorDataServiceMock;

    private final ProjectFilterConfiguration project = mock(ProjectFilterConfiguration.class);
    private final BudgetChartData budgetChartData = new BudgetChartData();
    private final ProgressData progressData = new ProgressData();
    private Optional<Exception> exception = Optional.empty();

    private MonitorData monitorDataResult;

    private static ZoneId timezone = ZoneId.of("America/Sao_Paulo");

    public static MonitorCalculatorDSL asTimeline() {
        MonitorDataService monitorDataServiceMock = mock(MonitorDataService.class);
        TimelineMonitorCalculator timelineSubject = new TimelineMonitorCalculator(monitorDataServiceMock);

        return new MonitorCalculatorDSL(timelineSubject, monitorDataServiceMock);
    }

    public static MonitorCalculatorDSL asCost() {
        MonitorDataService monitorDataServiceMock = mock(MonitorDataService.class);
        CostMonitorCalculator costSubject = new CostMonitorCalculator(monitorDataServiceMock);

        return new MonitorCalculatorDSL(costSubject, monitorDataServiceMock);
    }

    public static MonitorCalculatorDSL asScope() {
        MonitorDataService monitorDataServiceMock = mock(MonitorDataService.class);
        ScopeMonitorCalculator costSubject = new ScopeMonitorCalculator(monitorDataServiceMock);

        return new MonitorCalculatorDSL(costSubject, monitorDataServiceMock);
    }

    private MonitorCalculatorDSL(MonitorCalculator subject, MonitorDataService monitorDataServiceMock) {
        this.subject = subject;
        this.monitorDataServiceMock = monitorDataServiceMock;
    }

    public MonitorCalculatorDSL projectWithRisk(double risk) {
        when(project.getRiskPercentage()).thenReturn(new BigDecimal(risk));
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

    public MonitorCalculatorDSL progressDataWithActualProjection(ProgressDataPoint ...dataPoints) {
        progressData.actualProjection = asList(dataPoints);
        return this;
    }

    public MonitorCalculatorDSL progressDataWithExpected(ProgressDataPoint ...dataPoints) {
        progressData.expected = asList(dataPoints);
        return this;
    }

    public MonitorCalculatorDSL whenCalculate() {
        if (!exception.isPresent()) {
            when(monitorDataServiceMock.getBudgetChartData(project, timezone)).thenReturn(budgetChartData);
            when(monitorDataServiceMock.getProgressData(project, timezone)).thenReturn(progressData);
        } else {
            when(monitorDataServiceMock.getBudgetChartData(project, timezone)).thenThrow(exception.get());
            when(monitorDataServiceMock.getProgressData(project, timezone)).thenThrow(exception.get());
        }

        monitorDataResult = subject.calculate(project, timezone);
        return this;
    }

    public MonitorCalculatorDSL verifyException(MonitorCalculatorDSL dslWithType, Exception exception, String... errors) {
        dslWithType
            .givenServiceThrowing(exception)

        .whenCalculate()

        .then()
            .assertActual(CANT_CALCULATE_MESSAGE)
            .assertExpected(CANT_CALCULATE_MESSAGE)
            .assertStatus(null)
            .assertWarning(CANT_CALCULATE_MESSAGE)
            .assertErrors(errors);
        
        return this;
    }

    public MonitorCalculatorAsserter then() {
        return new MonitorCalculatorAsserter(monitorDataResult);
    }

    public static void assertMonitorError(MonitorCalculatorDSL typedDsl, Exception exception, String... errors) {
        typedDsl
            .givenServiceThrowing(exception)

        .whenCalculate()

        .then()
            .assertActual(CANT_CALCULATE_MESSAGE)
            .assertExpected(CANT_CALCULATE_MESSAGE)
            .assertStatus(null)
            .assertWarning(CANT_CALCULATE_MESSAGE)
            .assertErrors(errors);
        
    }
}
