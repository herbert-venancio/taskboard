package objective.taskboard.followup.kpi.touchtime;

import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

import objective.taskboard.followup.kpi.IssueKpi;

public abstract class TouchTimeByIssueKpiStrategy implements TouchTimeKpiStrategy<TouchTimeByIssueKpiDataPoint> {

    protected ZoneId timezone;
    protected List<IssueKpi> issues;
    protected List<String> progressingStatuses;

    protected TouchTimeByIssueKpiStrategy(ZoneId timezone, List<IssueKpi> issues, List<String> progressingStatuses) {
        this.timezone = timezone;
        this.issues = issues;
        this.progressingStatuses = progressingStatuses;
    }

    @Override
    public List<TouchTimeByIssueKpiDataPoint> getDataSet() {
        if (this.issues.isEmpty())
            return Collections.emptyList();
        return getDataPoints();
    }

    protected abstract List<TouchTimeByIssueKpiDataPoint> getDataPoints();

}