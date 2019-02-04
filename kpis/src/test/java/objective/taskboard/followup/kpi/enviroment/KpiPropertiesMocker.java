package objective.taskboard.followup.kpi.enviroment;

import static java.util.Arrays.asList;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.properties.IssueTypeChildrenStatusHierarchy;
import objective.taskboard.followup.kpi.properties.IssueTypeChildrenStatusHierarchy.Hierarchy;
import objective.taskboard.followup.kpi.properties.KPIProperties;
import objective.taskboard.followup.kpi.properties.KpiCycleTimeProperties;
import objective.taskboard.followup.kpi.properties.KpiLeadTimeProperties;
import objective.taskboard.followup.kpi.properties.TouchTimeSubtaskConfiguration;

public class KpiPropertiesMocker {

    private KPIProperties kpiProperties;
    private KpiEnvironment parentEnvironment;
    private Map<String, HierarchyBuilder> featureHierarchyBuilder = new LinkedHashMap<>();
    private Map<String, HierarchyBuilder> demandHierarchyBuilder = new LinkedHashMap<>();
    private TouchTimeSubtaskConfigsBuilder touchTimeSubtaskConfigsBuilder = new TouchTimeSubtaskConfigsBuilder();
    private boolean shouldCollectProgressingStatuses = true;
    private Map<KpiLevel,Set<String>> cycleTimeConfigurations;
    private Map<KpiLevel, Set<String>> leadTimeConfigurations;

    public KpiPropertiesMocker(KpiEnvironment environment) {
        this.parentEnvironment = environment;
        initializeMaps();
    }

    public Map<KpiLevel, Set<String>> getCycleStatusMap() {
        return cycleTimeConfigurations;
    }

    public Map<KpiLevel, Set<String>> getLeadStatusMap() {
        return leadTimeConfigurations;
    }

    public HierarchyBuilder atFeatureHierarchy(String fatherStatus) {
        HierarchyBuilder builder = new HierarchyBuilder(fatherStatus);
        featureHierarchyBuilder.put(fatherStatus, builder);
        return builder;
    }

    public HierarchyBuilder atDemandHierarchy(String fatherStatus) {
        HierarchyBuilder builder = new HierarchyBuilder(fatherStatus);
        demandHierarchyBuilder.put(fatherStatus, builder);
        return builder;
    }

    public TouchTimeSubtaskConfigsBuilder withTouchTimeSubtaskConfig() {
        return touchTimeSubtaskConfigsBuilder;
    }

    public KpiPropertiesMocker withNoProgressingStatusesConfigured() {
        this.shouldCollectProgressingStatuses = false;
        return this;
    }

    public KpiPropertiesMocker withSubtaskCycleTimeProperties(String...statuses) {
        cycleTimeConfigurations.get(KpiLevel.SUBTASKS).addAll(asList(statuses));
        return this;
    }

    public KpiPropertiesMocker withSubtaskLeadTimeProperties(String ...statuses) {
        configureLeadStatusesForLevel(KpiLevel.SUBTASKS, statuses);
        return this;
    }

    public KpiPropertiesMocker withFeatureLeadTimeProperties(String ...statuses) {
        configureLeadStatusesForLevel(KpiLevel.FEATURES, statuses);
        return this;
    }

    public KpiPropertiesMocker withDemandLeadTimeProperties(String ...statuses) {
        configureLeadStatusesForLevel(KpiLevel.DEMAND, statuses);
        return this;
    }

    public KpiEnvironment eoKP() {
        return parentEnvironment;
    }

    KPIProperties getKpiProperties() {
        if (kpiProperties == null)
            kpiProperties = prepareProperties();

        return kpiProperties;
    }

    private void initializeMaps() {
        cycleTimeConfigurations = new EnumMap<>(KpiLevel.class);
        leadTimeConfigurations = new EnumMap<>(KpiLevel.class);
        for (KpiLevel level : KpiLevel.values()) {
            cycleTimeConfigurations.put(level, new HashSet<>());
            leadTimeConfigurations.put(level, new HashSet<>());
        }
    }

    private void configureLeadStatusesForLevel(KpiLevel level, String[] statuses) {
        leadTimeConfigurations.get(level).addAll(asList(statuses));
    }

    private KPIProperties prepareProperties() {
        KPIProperties kpiProperties = new KPIProperties();
        kpiProperties.setDemandHierarchy(getHierarchy(demandHierarchyBuilder));
        kpiProperties.setFeaturesHierarchy(getHierarchy(featureHierarchyBuilder));
        kpiProperties.setTouchTimeSubtaskConfigs(touchTimeSubtaskConfigsBuilder.build());
        kpiProperties.setProgressingStatuses(getProgressingStatuses());
        kpiProperties.setCycleTime(getCycleTimeProperties());
        kpiProperties.setLeadTime(getLeadTimeProperties());
        return kpiProperties;
    }

    private List<String> getProgressingStatuses() {
        if (!shouldCollectProgressingStatuses) {
            return Collections.emptyList();
        }
        return parentEnvironment.statuses().getProgressingStatuses();
    }

    private KpiCycleTimeProperties getCycleTimeProperties() {
        KpiCycleTimeProperties cycleProperties = new KpiCycleTimeProperties();
        cycleProperties.setDemands(getCycleConfigurationsFor(KpiLevel.DEMAND));
        cycleProperties.setFeatures(getCycleConfigurationsFor(KpiLevel.FEATURES));
        cycleProperties.setSubtasks(getCycleConfigurationsFor(KpiLevel.SUBTASKS));
        return cycleProperties;
    }

    private List<String> getCycleConfigurationsFor(KpiLevel level){
        return new LinkedList<>(cycleTimeConfigurations.get(level));
    }

    private KpiLeadTimeProperties getLeadTimeProperties() {
        KpiLeadTimeProperties leadProperties = new KpiLeadTimeProperties();
        leadProperties.setDemands(getLeadConfigurationsFor(KpiLevel.DEMAND));
        leadProperties.setFeatures(getLeadConfigurationsFor(KpiLevel.FEATURES));
        leadProperties.setSubtasks(getLeadConfigurationsFor(KpiLevel.SUBTASKS));
        return leadProperties;
    }

    private List<String> getLeadConfigurationsFor(KpiLevel level){
        return new LinkedList<>(leadTimeConfigurations.get(level));
    }

    private IssueTypeChildrenStatusHierarchy getHierarchy(Map<String, HierarchyBuilder> hierarchyBuilder) {
        IssueTypeChildrenStatusHierarchy hierarchy = new IssueTypeChildrenStatusHierarchy();
        List<Hierarchy> hierachies = hierarchyBuilder.values().stream()
                .map(builder -> builder.buildHierachy())
                .collect(Collectors.toList());
        hierarchy.setHierarchies(hierachies);
        return hierarchy;
    }

    public class HierarchyBuilder {

        private String fatherStatus;
        private List<String> childrenTypes = new LinkedList<>();
        private List<String> childrenStatuses = new LinkedList<>();

        public HierarchyBuilder(String fatherStatus) {
            this.fatherStatus = fatherStatus;
        }

        public Hierarchy buildHierachy() {
            Hierarchy hierarchy = new Hierarchy();
            hierarchy.setFatherStatus(fatherStatus);
            hierarchy.setChildrenStatus(childrenStatuses);
            hierarchy.setChildrenTypeId(getChildrenTypeId());
            return hierarchy;
        }

        private List<Long> getChildrenTypeId() {
            return KpiPropertiesMocker.this.parentEnvironment.collectTypeIds(childrenTypes);
        }

        public HierarchyBuilder withChildrenType(String type) {
            childrenTypes.add(type);
            return this;
        }

        public HierarchyBuilder withChildrenStatus(String status) {
            childrenStatuses.add(status);
            return this;
        }

        public KpiPropertiesMocker eoH() {
            return KpiPropertiesMocker.this;
        }
    }

    public class TouchTimeSubtaskConfigsBuilder {
        private List<ChartStackConfig> stacks = new LinkedList<>();

        public ChartStackConfig withChartStack(String stackName) {
            return new ChartStackConfig(stackName);
        }

        public KpiPropertiesMocker eoTTSC() {
            return KpiPropertiesMocker.this;
        }

        public List<TouchTimeSubtaskConfiguration> build() {
            return stacks.stream()
                    .map(s -> {
                        TouchTimeSubtaskConfiguration config = new TouchTimeSubtaskConfiguration();
                        config.setStackName(s.name);
                        config.setStatuses(s.statuses);
                        config.setTypeIds(s.typeIds);
                        return config;
                    })
                    .collect(Collectors.toList());
        }

        private void addStack(ChartStackConfig stackBuilder) {
            stacks.add(stackBuilder);
        }

        @Override
        public String toString() {
            return "TouchTimeSubtaskConfigsBuilder [stacks=" + stacks + "]";
        }

        public class ChartStackConfig {
            private String name;
            private List<Long> typeIds = new LinkedList<>();
            private List<String> statuses = new LinkedList<>();

            public ChartStackConfig(String stackName) {
                this.name = stackName;
            }

            public ChartStackConfig types(String ...types) {
                this.typeIds = KpiPropertiesMocker.this.parentEnvironment.collectTypeIds(Arrays.asList(types));
                return this;
            }

            public ChartStackConfig statuses(String ...statuses) {
                this.statuses = Arrays.asList(statuses);
                return this;
            }

            public TouchTimeSubtaskConfigsBuilder eoS() {
                TouchTimeSubtaskConfigsBuilder.this.addStack(this);
                return TouchTimeSubtaskConfigsBuilder.this;
            }

            @Override
            public String toString() {
                return "ChartStackConfig [name=" + name + ", typeIds=" + typeIds + ", statuses=" + statuses + "]";
            }
        }
    }

}
