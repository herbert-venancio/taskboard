package objective.taskboard.followup.kpi.touchtime.helpers;

import java.util.List;

import objective.taskboard.followup.kpi.services.KpiDataSetAsserter;
import objective.taskboard.followup.kpi.touchtime.TouchTimeByIssueKpiDataPoint;

public class TTByIssueKpiDataSetAsserter extends KpiDataSetAsserter<TouchTimeByIssueKpiDataPoint, TTByIssueKpiDataPointBuilder> {
    public TTByIssueKpiDataSetAsserter(List<TouchTimeByIssueKpiDataPoint> dataSet) {
        super(dataSet);
    }

    @Override
    protected TTByIssueKpiDataPointAsserter getAsserter(TouchTimeByIssueKpiDataPoint expectedPoint) {
        return new TTByIssueKpiDataPointAsserter(expectedPoint);
    }
}