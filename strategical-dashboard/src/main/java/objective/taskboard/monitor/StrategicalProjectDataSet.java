package objective.taskboard.monitor;

import static objective.taskboard.monitor.MonitorCalculator.CANT_CALCULATE_MESSAGE;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StrategicalProjectDataSet {
    public String projectKey;
    public String projectDisplayName;
    public List<MonitorData> monitors = new ArrayList<>();

    public static class MonitorData {
        public final String label;
        public final String icon;
        public String status;
        public String statusDetails;
        public List<DataItem> items = new ArrayList<DataItem>();

        public MonitorData(String label, String icon) {
            this.label = label;
            this.icon = icon;
        }

        public static MonitorData withError(MonitorData fromMonitorData, String errorMessage) {
            MonitorData monitorDataWithError = new MonitorData(fromMonitorData.label, fromMonitorData.icon);

            List<DataItem> dataItems = fromMonitorData.items.stream().map(item -> {
                item.text = CANT_CALCULATE_MESSAGE;
                item.details = errorMessage;
                return item;
            }).collect(Collectors.toList());

            monitorDataWithError.items = dataItems;

            return monitorDataWithError;
        }
    }

    public static class DataItem {
        public String title;
        public String text;
        public String details;

        public DataItem(String title, String details) {
            this.title = title;
            this.details = details;
        }
    }
}
