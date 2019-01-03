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
    private Map<String, HierarchyBuilder> featureHierarchyBuilder = new LinkedHashMap<>();
    private Map<String, HierarchyBuilder> demandHierarchyBuilder = new LinkedHashMap<>();

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

    public KPIProperties getKpiProperties() {
        buildHierarchies();
        mockProgressingStatuses(fatherEnvironment.statuses().getProgressingStatuses());
        return kpiProperties;
    }

    public KpiEnvironment eoKpi() {
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

        public KpiPropertiesMocker eoH() {
            return KpiPropertiesMocker.this;
        }

        public KpiEnvironment eoKp() {
            return fatherEnvironment;
        }

    }

}
