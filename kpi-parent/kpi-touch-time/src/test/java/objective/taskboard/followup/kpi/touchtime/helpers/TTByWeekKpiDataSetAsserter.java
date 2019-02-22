package objective.taskboard.followup.kpi.touchtime.helpers;

import java.util.LinkedList;
import java.util.List;

import objective.taskboard.followup.kpi.enviroment.KpiDataPointAsserter;
import objective.taskboard.followup.kpi.enviroment.KpiDataSetAsserter;
import objective.taskboard.followup.kpi.touchtime.TouchTimeByWeekKpiDataPoint;

public class TTByWeekKpiDataSetAsserter extends KpiDataSetAsserter<TouchTimeByWeekKpiDataPoint, TTByWeekKpiDataPointBuilder> {
    public TTByWeekKpiDataSetAsserter(List<TouchTimeByWeekKpiDataPoint> dataSet) {
        super(dataSet);
    }

    @Override
    protected List<KpiDataPointAsserter<TouchTimeByWeekKpiDataPoint>> buildAsserters(
            TTByWeekKpiDataPointBuilder[] expectedPointsBuilders) {
        List<KpiDataPointAsserter<TouchTimeByWeekKpiDataPoint>> asserters = new LinkedList<>();
        for (TTByWeekKpiDataPointBuilder ttByWeekKpiDataPointBuilder : expectedPointsBuilders) {
            TouchTimeByWeekKpiDataPoint expectedPoint = ttByWeekKpiDataPointBuilder.build();
            asserters.add(new TTByWeekKpiDataPointAsserter(expectedPoint));
        }
        return asserters;
    }
}