package objective.taskboard.followup.kpi.properties;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import objective.taskboard.followup.kpi.services.KpiEnvironment;

public class KpiTouchTimePropertiesMocker implements KpiEnvironment.KpiPropertiesMockBuilder<KpiTouchTimeProperties> {

    private boolean shouldCollectProgressingStatuses = true;
    private List<ChartStackConfig> stacks = new LinkedList<>();

    public static KpiTouchTimePropertiesMocker withTouchTimeConfig() {
        return new KpiTouchTimePropertiesMocker();
    }

    public ChartStackConfig withChartStack(String stackName) {
        return new ChartStackConfig(stackName);
    }

    public KpiTouchTimePropertiesMocker eoTTSC() {
        return this;
    }

    @Override
    public Class<KpiTouchTimeProperties> propertiesClass() {
        return KpiTouchTimeProperties.class;
    }

    @Override
    public KpiTouchTimeProperties build(KpiEnvironment environment) {
        KpiTouchTimeProperties properties = new KpiTouchTimeProperties();
        properties.setTouchTimeSubtaskConfigs(stacks.stream()
                .map(s -> {
                    TouchTimeSubtaskConfiguration config = new TouchTimeSubtaskConfiguration();
                    config.setStackName(s.name);
                    config.setStatuses(s.statuses);
                    config.setTypeIds(environment.collectTypeIds(s.typeIds));
                    return config;
                })
                .collect(Collectors.toList()));
        if(shouldCollectProgressingStatuses)
            properties.setProgressingStatuses(environment.statuses().getProgressingStatuses());
        else
            properties.setProgressingStatuses(Collections.emptyList());
        return properties;
    }

    public KpiTouchTimePropertiesMocker withNoProgressingStatusesConfigured() {
        this.shouldCollectProgressingStatuses = false;
        return this;
    }

    private void addStack(ChartStackConfig stackBuilder) {
        stacks.add(stackBuilder);
    }

    public class ChartStackConfig {
        private String name;
        private List<String> typeIds = new LinkedList<>();
        private List<String> statuses = new LinkedList<>();

        public ChartStackConfig(String stackName) {
            this.name = stackName;
        }

        public ChartStackConfig types(String ...types) {
            this.typeIds.addAll(Arrays.asList(types));
            return this;
        }

        public ChartStackConfig statuses(String ...statuses) {
            this.statuses.addAll(Arrays.asList(statuses));
            return this;
        }

        public KpiTouchTimePropertiesMocker eoS() {
            addStack(this);
            return KpiTouchTimePropertiesMocker.this;
        }

        @Override
        public String toString() {
            return "ChartStackConfig [name=" + name + ", typeIds=" + typeIds + ", statuses=" + statuses + "]";
        }
    }
}
