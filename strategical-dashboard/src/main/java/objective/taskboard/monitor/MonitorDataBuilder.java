package objective.taskboard.monitor;

import static java.util.Arrays.asList;
import static objective.taskboard.monitor.MonitorCalculator.CANT_CALCULATE_MESSAGE;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import objective.taskboard.monitor.StrategicalProjectDataSet.DataItem;
import objective.taskboard.monitor.StrategicalProjectDataSet.MonitorData;

public class MonitorDataBuilder {
    public String label;
    public String icon;
    public String status;
    public String statusDetails;
    public List<DataItem> items = new ArrayList<>();
    public DataItem expectedItem = new DataItem("(expected)");
    public DataItem warningItem = new DataItem("(warning)");
    public DataItem actualItem = new DataItem("(actual)");

    public MonitorDataBuilder() {

    }

    public MonitorDataBuilder withLabel(String label) {
        this.label = label;
        return this;
    }

    public MonitorDataBuilder withIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public MonitorDataBuilder withStatus(String status) {
        this.status = status;
        return this;
    }

    public MonitorDataBuilder withStatusDetails(String statusDetails) {
        this.statusDetails = statusDetails;
        return this;
    }

    public MonitorDataBuilder expectedDetails(String details) {
        expectedItem.details = details;
        return this;
    }

    public MonitorDataBuilder expectedValue(String value) {
        expectedItem.text = value;
        return this;
    }

    public MonitorDataBuilder warningDetails(String details) {
        warningItem.details = details;
        return this;
    }

    public MonitorDataBuilder warningValue(String value) {
        warningItem.text = value;
        return this;
    }

    public MonitorDataBuilder actualDetails(String details) {
        actualItem.details = details;
        return this;
    }

    public MonitorDataBuilder actualValue(String value) {
        actualItem.text = value;
        return this;
    }

    public MonitorData withError(String errorMessage) {
      List<DataItem> items = Stream.of(this.expectedItem, this.warningItem, this.actualItem)
              .peek(item -> {
                  item.text = CANT_CALCULATE_MESSAGE;
                  item.details = errorMessage;
              })
              .collect(Collectors.toList());

        return new MonitorData(this.label, this.icon, this.status, this.statusDetails, items);
    }

    public MonitorData build() {
        List<DataItem> items = asList(this.expectedItem, this.warningItem, this.actualItem);
        return new MonitorData(this.label, this.icon, this.status, this.statusDetails, items);
    }
}

