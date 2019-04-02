package objective.taskboard.followup.kpi.properties;

import static java.util.Arrays.asList;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.services.KpiEnvironment;

public class KpiCycleTimePropertiesMocker implements KpiEnvironment.KpiPropertiesMockBuilder<KpiCycleTimeProperties> {

    private Map<KpiLevel,Set<String>> cycleTimeConfigurations;

    public KpiCycleTimePropertiesMocker() {
        cycleTimeConfigurations = new EnumMap<>(KpiLevel.class);
        for (KpiLevel level : KpiLevel.values()) {
            cycleTimeConfigurations.put(level, new HashSet<>());
        }
    }

    public static KpiCycleTimePropertiesMocker withSubtaskCycleTimeProperties(String...statuses) {
        return new KpiCycleTimePropertiesMocker()
                .addAll(KpiLevel.SUBTASKS, statuses);
    }

    @Override
    public Class<KpiCycleTimeProperties> propertiesClass() {
        return KpiCycleTimeProperties.class;
    }

    @Override
    public KpiCycleTimeProperties build(KpiEnvironment environment) {
        KpiCycleTimeProperties kpiProperties = new KpiCycleTimeProperties();
        CycleTimeProperties cycleProperties = new CycleTimeProperties();
        cycleProperties.setDemands(getCycleConfigurationsFor(KpiLevel.DEMAND));
        cycleProperties.setFeatures(getCycleConfigurationsFor(KpiLevel.FEATURES));
        cycleProperties.setSubtasks(getCycleConfigurationsFor(KpiLevel.SUBTASKS));
        kpiProperties.setCycleTime(cycleProperties);
        return kpiProperties;
    }

    private KpiCycleTimePropertiesMocker addAll(KpiLevel level, String...statuses) {
        cycleTimeConfigurations.get(level).addAll(asList(statuses));
        return this;
    }

    private List<String> getCycleConfigurationsFor(KpiLevel level){
        return new LinkedList<>(cycleTimeConfigurations.get(level));
    }
}
