package objective.taskboard.followup.kpi.enviroment;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;

import objective.taskboard.followup.kpi.properties.IssueTypeChildrenStatusHierarchy;
import objective.taskboard.followup.kpi.properties.IssueTypeChildrenStatusHierarchy.Hierarchy;
import objective.taskboard.followup.kpi.properties.KPIProperties;

public class KpiPropertiesMocker implements KpiEnvironment.KpiPropertiesMockBuilder<KPIProperties> {

    private KpiEnvironment environment;
    private Map<String, HierarchyBuilder> featureHierarchyBuilder = new LinkedHashMap<>();
    private Map<String, HierarchyBuilder> demandHierarchyBuilder = new LinkedHashMap<>();
    private boolean shouldCollectProgressingStatuses = true;
    @SuppressWarnings("rawtypes")
	private Map<Class<?>, KpiEnvironment.KpiPropertiesMockBuilder> mockBuilderMap = new HashMap<>();
    private Map<Class<?>, Object> instanceMap = new HashMap<>();

    public KpiPropertiesMocker(KpiEnvironment environment) {
        this.environment = environment;
        put(this);
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

    public KpiPropertiesMocker withNoProgressingStatusesConfigured() {
        this.shouldCollectProgressingStatuses = false;
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T getKpiProperties(Class<T> propertiesClass) {
        if(!instanceMap.containsKey(propertiesClass)) {
            KpiEnvironment.KpiPropertiesMockBuilder<T> builder = mockBuilderMap.computeIfAbsent(propertiesClass, EmptyKpiPropertiesBuilder::new);
            instanceMap.put(propertiesClass, builder.build(environment));
        }
        return (T) instanceMap.get(propertiesClass);
    }

    public void put(KpiEnvironment.KpiPropertiesMockBuilder<?> builder) {
        mockBuilderMap.put(builder.propertiesClass(), builder);
    }

    @Override
    public Class<KPIProperties> propertiesClass() {
        return KPIProperties.class;
    }

    @Override
    public KPIProperties build(KpiEnvironment environment) {
        KPIProperties kpiProperties = new KPIProperties();
        kpiProperties.setDemandHierarchy(getHierarchy(demandHierarchyBuilder));
        kpiProperties.setFeaturesHierarchy(getHierarchy(featureHierarchyBuilder));
        kpiProperties.setProgressingStatuses(getProgressingStatuses());
        return kpiProperties;
    }

    public KpiEnvironment eoKP() {
        return environment;
    }

    public KpiEnvironment eoKpi() {
        return environment;
    }

    private List<String> getProgressingStatuses() {
        if (!shouldCollectProgressingStatuses) {
            return Collections.emptyList();
        }
        return environment.statuses().getProgressingStatuses();
    }

    private IssueTypeChildrenStatusHierarchy getHierarchy(Map<String, HierarchyBuilder> hierarchyBuilder) {
        IssueTypeChildrenStatusHierarchy hierarchy = new IssueTypeChildrenStatusHierarchy();
        List<Hierarchy> hierachies = hierarchyBuilder.values().stream()
                .map(HierarchyBuilder::buildHierachy)
                .collect(Collectors.toList());
        hierarchy.setHierarchies(hierachies);
        return hierarchy;
    }

    private static class EmptyKpiPropertiesBuilder<T> implements KpiEnvironment.KpiPropertiesMockBuilder<T> {

        private final Class<T> propertiesClass;

        public EmptyKpiPropertiesBuilder(Class<T> propertiesClass) {
            this.propertiesClass = propertiesClass;
        }

        @Override
        public Class<T> propertiesClass() {
            return propertiesClass;
        }

        @Override
        public T build(KpiEnvironment environment) {
            return BeanUtils.instantiate(propertiesClass);
        }
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

        private List<Long> getChildrenTypeId() {
            return KpiPropertiesMocker.this.environment.collectTypeIds(childrenTypes);
        }
    }

}
