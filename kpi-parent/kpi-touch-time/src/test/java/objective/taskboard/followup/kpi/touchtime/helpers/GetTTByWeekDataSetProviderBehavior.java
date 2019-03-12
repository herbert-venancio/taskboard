package objective.taskboard.followup.kpi.touchtime.helpers;

import java.time.ZoneId;
import java.util.List;

import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.touchtime.TouchTimeByWeekKpiDataPoint;

class GetTTByWeekDataSetProviderBehavior extends GetTTDataSetProviderBehavior<TTByWeekKpiDataSetAsserter> {

    GetTTByWeekDataSetProviderBehavior(String methodName, String projectKey, KpiLevel kpiLevel, ZoneId timezone) {
        super(methodName, projectKey, kpiLevel, timezone);
    }

    @Override
    @SuppressWarnings("unchecked")
    void createAsserter(List<?> dataset) {
        this.asserter = new TTByWeekKpiDataSetAsserter((List<TouchTimeByWeekKpiDataPoint>) dataset);
    }
}