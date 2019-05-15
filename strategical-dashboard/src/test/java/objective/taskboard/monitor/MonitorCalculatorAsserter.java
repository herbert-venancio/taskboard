package objective.taskboard.monitor;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import objective.taskboard.monitor.StrategicalProjectDataSet.MonitorData;

public class MonitorCalculatorAsserter {
    private final MonitorData monitorData;

    public MonitorCalculatorAsserter(MonitorData monitorData) {
        this.monitorData = monitorData;
    }

    public MonitorCalculatorAsserter assertLabel(String label) {
        assertEquals(label, monitorData.label);
        return this;
    }

    public MonitorCalculatorAsserter assertIcon(String icon) {
        assertEquals(icon, monitorData.icon);
        return this;
    }

    public MonitorCalculatorAsserter assertActual(String actual) {
        assertEquals(actual, monitorData.items.get(2).text);
        return this;
    }

    public MonitorCalculatorAsserter assertExpected(String expected) {
        assertEquals(expected, monitorData.items.get(0).text);
        return this;
    }

    public MonitorCalculatorAsserter assertStatus(String status) {
        assertEquals(status, monitorData.status);
        return this;
    }

    public MonitorCalculatorAsserter assertWarning(String warning) {
        assertEquals(warning, monitorData.items.get(1).text);
        return this;
    }

    public MonitorCalculatorAsserter assertErrors(String... errors) {
        boolean hasErrors = monitorData.items.stream().anyMatch(item -> {
            return asList(errors).contains(item.details);
        });

        assertTrue(hasErrors);
        return this;
    }
}
