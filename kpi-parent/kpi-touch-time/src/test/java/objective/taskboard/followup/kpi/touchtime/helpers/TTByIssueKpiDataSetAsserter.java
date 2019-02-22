package objective.taskboard.followup.kpi.touchtime.helpers;

import java.util.LinkedList;
import java.util.List;

import objective.taskboard.followup.kpi.enviroment.KpiDataPointAsserter;
import objective.taskboard.followup.kpi.enviroment.KpiDataSetAsserter;
import objective.taskboard.followup.kpi.touchtime.TouchTimeByIssueKpiDataPoint;

public class TTByIssueKpiDataSetAsserter extends KpiDataSetAsserter<TouchTimeByIssueKpiDataPoint, TTByIssueKpiDataPointBuilder> {
    public TTByIssueKpiDataSetAsserter(List<TouchTimeByIssueKpiDataPoint> dataSet) {
        super(dataSet);
    }

    @Override
    protected List<KpiDataPointAsserter<TouchTimeByIssueKpiDataPoint>> buildAsserters(
            TTByIssueKpiDataPointBuilder[] expectedPointsBuilders) {
        List<KpiDataPointAsserter<TouchTimeByIssueKpiDataPoint>> asserters = new LinkedList<>();
        for (TTByIssueKpiDataPointBuilder ttByIssueKpiDataPointBuilder : expectedPointsBuilders) {
            TouchTimeByIssueKpiDataPoint expectedPoint = ttByIssueKpiDataPointBuilder.build();
            asserters.add(new TTByIssueKpiDataPointAsserter(expectedPoint));
        }
        return asserters;
    }
}