package objective.taskboard.followup.kpi.enviroment;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.mockito.Mockito;

import objective.taskboard.followup.kpi.properties.IssueTypeChildrenStatusHierarchy;
import objective.taskboard.followup.kpi.properties.IssueTypeChildrenStatusHierarchy.Hierarchy;
import objective.taskboard.followup.kpi.properties.KPIProperties;
import objective.taskboard.followup.kpi.properties.TouchTimeSubtaskConfiguration;

public class KpiPropertiesMocker {

    private KPIProperties kpiProperties = Mockito.mock(KPIProperties.class);
    private KpiEnvironment fatherEnvironment;
    private Map<String, HierarchyBuilder> featureHierarchyBuilder = new LinkedHashMap<>();
    private Map<String, HierarchyBuilder> demandHierarchyBuilder = new LinkedHashMap<>();
    private TouchTimeSubtaskConfigsBuilder touchTimeSubtaskConfigsBuilder = new TouchTimeSubtaskConfigsBuilder();
    private List<String> progressingStatuses = new LinkedList<>();
    private boolean shouldCollectProgressingStatuses = true;

    public KpiPropertiesMocker(KpiEnvironment environment) {
        this.fatherEnvironment = environment;
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

    public KPIProperties getKpiProperties() {
        buildHierarchies();
        buildTouchTimeSubtaskConfigs();
        if (shouldCollectProgressingStatuses)
            progressingStatuses = fatherEnvironment.statuses().getProgressingStatuses();
        mockProgressingStatuses(progressingStatuses);
        return kpiProperties;
    }

    public KpiEnvironment eoKp() {
        return fatherEnvironment;
    }

    private void mockProgressingStatuses(List<String> progressingStatuses) {
        Mockito.when(kpiProperties.getProgressingStatuses()).thenReturn(progressingStatuses);
    }

    private void buildHierarchies() {
        buildDemands();
        buildFeatures();
    }

    private void buildDemands() {
        IssueTypeChildrenStatusHierarchy hierarchy = getHierarchy(demandHierarchyBuilder);
        Mockito.when(kpiProperties.getDemandHierarchy()).thenReturn(hierarchy);
    }

    private void buildFeatures() {
        IssueTypeChildrenStatusHierarchy hierarchy = getHierarchy(featureHierarchyBuilder);
        Mockito.when(kpiProperties.getFeaturesHierarchy()).thenReturn(hierarchy);
    }

    private IssueTypeChildrenStatusHierarchy getHierarchy(Map<String, HierarchyBuilder> hierarchyBuilder) {
        IssueTypeChildrenStatusHierarchy hierarchy = new IssueTypeChildrenStatusHierarchy();
        List<Hierarchy> hierachies = hierarchyBuilder.values().stream()
                .map(builder -> builder.buildHierachy())
                .collect(Collectors.toList());
        hierarchy.setHierarchies(hierachies);
        return hierarchy;
    }

    private void buildTouchTimeSubtaskConfigs() {
        List<TouchTimeSubtaskConfiguration> configs = touchTimeSubtaskConfigsBuilder.build();
        Mockito.when(kpiProperties.getTouchTimeSubtaskConfigs()).thenReturn(configs);
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
            return KpiPropertiesMocker.this.fatherEnvironment.collectTypeIds(childrenTypes);
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
                this.typeIds = KpiPropertiesMocker.this.fatherEnvironment.collectTypeIds(Arrays.asList(types));
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
