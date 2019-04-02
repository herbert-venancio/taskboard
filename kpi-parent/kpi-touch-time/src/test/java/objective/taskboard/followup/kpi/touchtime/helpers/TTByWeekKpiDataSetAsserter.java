package objective.taskboard.followup.kpi.touchtime.helpers;

import java.util.List;

import objective.taskboard.followup.kpi.services.KpiDataSetAsserter;
import objective.taskboard.followup.kpi.touchtime.TouchTimeByWeekKpiDataPoint;

public class TTByWeekKpiDataSetAsserter extends KpiDataSetAsserter<TouchTimeByWeekKpiDataPoint, TTByWeekKpiDataPointBuilder> {
    public TTByWeekKpiDataSetAsserter(List<TouchTimeByWeekKpiDataPoint> dataSet) {
        super(dataSet);
    }

    @Override
    protected TTByWeekKpiDataPointAsserter getAsserter(TouchTimeByWeekKpiDataPoint expectedPoint) {
        return new TTByWeekKpiDataPointAsserter(expectedPoint);
    }
}