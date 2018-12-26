package objective.taskboard.followup.kpi.enviroment;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.mockito.Mockito;

import objective.taskboard.followup.kpi.properties.IssueTypeChildrenStatusHierarchy;
import objective.taskboard.followup.kpi.properties.IssueTypeChildrenStatusHierarchy.Hierarchy;
import objective.taskboard.followup.kpi.properties.KPIProperties;

public class KpiPropertiesMocker {

    private KPIProperties kpiProperties = Mockito.mock(KPIProperties.class);
    private KpiEnvironment fatherEnvironment;
    private Map<String,HierarchyBuilder> featureHierarchyBuilder = new LinkedHashMap<>();

    public KpiPropertiesMocker(KpiEnvironment environment) {
        this.fatherEnvironment = environment;
    }

    public HierarchyBuilder atFeatureHierarchy(String fatherStatus) {
        HierarchyBuilder builder = new HierarchyBuilder(fatherStatus);
        featureHierarchyBuilder.put(fatherStatus,builder);
        return builder;
    }

    public KpiPropertiesMocker withKpiProperties(KPIProperties kpiProperties) {
        this.kpiProperties = kpiProperties;
        return this;
    }

    public KPIProperties getKpiProperties() {
        buildHierarchies();
        mockProgressingStatuses(fatherEnvironment.getStatusRepository().getProgressingStatuses());
        return kpiProperties;
    }

    private void mockProgressingStatuses(List<String> progressingStatuses) {
        Mockito.when(kpiProperties.getProgressingStatuses()).thenReturn(progressingStatuses);
    }

    private void buildHierarchies() {
        IssueTypeChildrenStatusHierarchy hierarchy = new IssueTypeChildrenStatusHierarchy();
        List<Hierarchy> hierachies = featureHierarchyBuilder.values().stream().map(builder -> builder.buildHierachy()).collect(Collectors.toList());
        hierarchy.setHierarchies(hierachies);

        Mockito.when(kpiProperties.getFeaturesHierarchy()).thenReturn(hierarchy);
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

        public HierarchyBuilder putChildrenType(String type) {
            childrenTypes.add(type);
            return this;
        }

        public HierarchyBuilder putChildrenStatus(String status) {
            childrenStatuses.add(status);
            return this;
        }

        public KpiPropertiesMocker and() {
            return KpiPropertiesMocker.this;
        }

        public KpiEnvironment eoKp() {
            return fatherEnvironment;
        }

    }

}
