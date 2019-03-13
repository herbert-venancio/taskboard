package objective.taskboard.followup.kpi.touchtime;

import java.util.List;

interface TouchTimeKpiStrategy<T> {
    List<T> getDataSet();
}
