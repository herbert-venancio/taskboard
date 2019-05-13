package objective.taskboard.monitor;

import java.time.ZoneId;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.monitor.StrategicalProjectDataSet.MonitorData;

interface MonitorCalculator {
    String CANT_CALCULATE_MESSAGE ="Can't Calculate";

    MonitorData calculate(ProjectFilterConfiguration project, ZoneId timezone);

}
