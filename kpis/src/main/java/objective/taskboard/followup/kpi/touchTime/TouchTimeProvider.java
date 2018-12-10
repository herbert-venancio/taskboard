package objective.taskboard.followup.kpi.touchTime;

import java.time.ZoneId;

import objective.taskboard.followup.kpi.KpiLevel;

public interface TouchTimeProvider <T>{

	T getDataSet(String projectKey, KpiLevel kpiLevel, ZoneId timezone);
	
}
