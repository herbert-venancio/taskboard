package objective.taskboard.followup.kpi.touchtime;

import java.time.ZoneId;

import objective.taskboard.followup.kpi.KpiLevel;

public interface TouchTimeKpiDataProvider <T>{

	T getDataSet(String projectKey, KpiLevel kpiLevel, ZoneId timezone);
	
}
