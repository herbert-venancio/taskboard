package objective.taskboard.monitor;

import java.util.ArrayList;
import java.util.List;

public class StrategicalProjectDataSet {
    public String projectKey;
    public String projectDisplayName;
    public List<MonitorData> monitors = new ArrayList<>();

    public enum Status {

        ALERT("alert"),
        DANGER("danger"),
        NORMAL("normal");

        public final String status;

        Status(String status) {
            this.status = status;
        }

        public String status() {
            return status;
        }
    }

    public static class MonitorData {
        public final String label;
        public final String icon;
        public String status;
        public String statusDetails;
        public List<DataItem> items = new ArrayList<>();

        public MonitorData(String label, String icon) {
            this.label = label;
            this.icon = icon;
        }

        public MonitorData(String label, String icon, String status, String statusDetails, List<DataItem> items) {
            this.label = label;
            this.icon = icon;
            this.status = status;
            this.statusDetails = statusDetails;
            this.items = items;
        }

        public static MonitorDataBuilder builder() {
            return new MonitorDataBuilder();
        }
    }

    public static class DataItem {
        public String title;
        public String text;
        public String details;

        public DataItem(String title) {
            this.title = title;
        }

        public DataItem(String title, String details) {
            this.title = title;
            this.details = details;
        }
    }

}
