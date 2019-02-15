package objective.taskboard.followup.kpi.properties;

import static java.util.Arrays.asList;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.enviroment.KpiEnvironment;

public class KpiLeadTimePropertiesMocker implements KpiEnvironment.KpiPropertiesMockBuilder<KpiLeadTimeProperties> {

    private Map<KpiLevel,Set<String>> leadTimeConfigurations;

    public KpiLeadTimePropertiesMocker() {
        leadTimeConfigurations = new EnumMap<>(KpiLevel.class);
        for (KpiLevel level : KpiLevel.values()) {
            leadTimeConfigurations.put(level, new HashSet<>());
        }
    }

    public static KpiLeadTimePropertiesMocker withSubtaskLeadTimeProperties(String... statuses) {
        return new KpiLeadTimePropertiesMocker()
                .addAll(KpiLevel.SUBTASKS, statuses);
    }

    public static KpiLeadTimePropertiesMocker withFeatureLeadTimeProperties(String... statuses) {
        return new KpiLeadTimePropertiesMocker()
                .addAll(KpiLevel.FEATURES, statuses);
    }

    public static KpiLeadTimePropertiesMocker withDemandLeadTimeProperties(String ...statuses) {
        return new KpiLeadTimePropertiesMocker()
                .addAll(KpiLevel.DEMAND, statuses);
    }

    @Override
    public Class<KpiLeadTimeProperties> propertiesClass() {
        return KpiLeadTimeProperties.class;
    }

    @Override
    public KpiLeadTimeProperties build(KpiEnvironment environment) {
        KpiLeadTimeProperties kpiProperties = new KpiLeadTimeProperties();
        LeadTimeProperties leadProperties = new LeadTimeProperties();
        leadProperties.setDemands(getLeadTimeConfigurationsFor(KpiLevel.DEMAND));
        leadProperties.setFeatures(getLeadTimeConfigurationsFor(KpiLevel.FEATURES));
        leadProperties.setSubtasks(getLeadTimeConfigurationsFor(KpiLevel.SUBTASKS));
        kpiProperties.setLeadTime(leadProperties);
        return kpiProperties;
    }

    private KpiLeadTimePropertiesMocker addAll(KpiLevel level, String...statuses) {
        leadTimeConfigurations.get(level).addAll(asList(statuses));
        return this;
    }

    private List<String> getLeadTimeConfigurationsFor(KpiLevel level){
        return new LinkedList<>(leadTimeConfigurations.get(level));
    }
}
