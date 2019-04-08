package objective.taskboard.followup.kpi.properties;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import objective.taskboard.followup.kpi.services.KpiEnvironment;
import objective.taskboard.followup.kpi.services.KpiEnvironment.IssueTypeDTO;

public class KpiBugByEnvironmentMocker  implements KpiEnvironment.KpiPropertiesMockBuilder<KpiBugByEnvironmentProperties> {

    private List<String> types = new LinkedList<>();
    
    public static KpiBugByEnvironmentMocker withBugTypes(String...types) {
        return new KpiBugByEnvironmentMocker().addBugTypes(types);
    }
    
    public static KpiBugByEnvironmentMocker noBugTypes() {
        return new KpiBugByEnvironmentMocker();
    }
    
    private KpiBugByEnvironmentMocker addBugTypes(String[] types) {
        this.types = Arrays.asList(types);
        return this;
    }

    @Override
    public Class<KpiBugByEnvironmentProperties> propertiesClass() {
        return KpiBugByEnvironmentProperties.class;
    }

    @Override
    public KpiBugByEnvironmentProperties build(KpiEnvironment environment) {
        List<Long> typesIds = getTypeIds(environment);
        
        KpiBugByEnvironmentProperties properties = new KpiBugByEnvironmentProperties();
        properties.setBugTypes(typesIds);
        return properties;
    }

    private List<Long> getTypeIds(KpiEnvironment environment) {
        return types.stream()
            .map(typeName -> environment.types().getExistent(typeName))
            .map(IssueTypeDTO::id)
            .collect(Collectors.toList());
    }



}
