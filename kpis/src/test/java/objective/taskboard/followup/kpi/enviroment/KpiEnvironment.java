package objective.taskboard.followup.kpi.enviroment;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Assert;

import objective.taskboard.followup.kpi.IssueTypeKpi;
import objective.taskboard.followup.kpi.properties.KPIProperties;
import objective.taskboard.testUtils.FixedClock;
import objective.taskboard.utils.Clock;
import objective.taskboard.utils.DateTimeUtils;

public class KpiEnvironment {
    
    private IssueTypeRepository typeRepository = new IssueTypeRepository();
    private StatusRepository statusRepository = new StatusRepository();
    private KpiPropertiesMocker kpiPropertiesMocker = new KpiPropertiesMocker(this);
    private TransitionsBuilder transitionsBuilder = new TransitionsBuilder(this);
    
    private Map<String,IssueKpiMocker> issues = new LinkedHashMap<>();
    private DSLKpi kpiContext;
    private FixedClock clock = new FixedClock();
    private ZoneId timezone = ZoneId.systemDefault();
    
    public KpiEnvironment() {}
    
    public Clock getClock() {
        return clock;
    }
    
    public ZoneId getTimezone() {
        return timezone;
    }
    
    public KpiEnvironment(DSLKpi kpiContext) {
        this.kpiContext = kpiContext;
    }
    
    public DSLKpi then() {
        return kpiContext;
    }

    public KpiPropertiesMocker kpiProperties() {
        return kpiPropertiesMocker;
    }
    
    public KpiEnvironment todayIs(String date) {
        ZonedDateTime zonedDate = DateTimeUtils.parseDateTime(date);
        clock.setNow(zonedDate.toInstant());
        return this;
    }
    
    public KpiEnvironment withFeatureType(String name) {
        typeRepository.addFeature(name);
        return this;
    }
    
    public KpiEnvironment withSubtaskType(String name) {
        typeRepository.addSubtask(name);
        return this;
    }
    
    public StatusDto withStatus(String name) {
        StatusDto status = statusRepository.create(name);
        return status;
    }
    
    public KPIProperties getKPIProperties() {
        return kpiPropertiesMocker.getKpiProperties();
    }
    
    public TransitionsBuilder statusTransition() {
        return transitionsBuilder;
    }
    
    List<Long> collectTypeIds(List<String> childrenTypes) {
        return typeRepository.types.values().stream()
                    .filter(type -> childrenTypes.contains(type.name))
                    .map(type -> type.id)
                    .collect(Collectors.toList());
                    
    }
    
    public IssueKpiMocker givenIssue(String pKey) {
        issues.putIfAbsent(pKey, new IssueKpiMocker(this, transitionsBuilder,pKey));
        return issues.get(pKey);
    }
    
    
    public StatusDto getStatus(String name) {
        return statusRepository.statuses.get(name);
    }
    
    public IssueTypeDTO getType(String type) {
        return typeRepository.getExistent(type);
    }
    
    public Optional<IssueTypeDTO> getOptionalType(String type) {
        return typeRepository.getOptional(type);
    }
    
    private class IssueTypeRepository {
        private long id = 1l;
        private Map<String,IssueTypeDTO> types = new LinkedHashMap<>();
        
        private void addFeature(String name) {
            IssueTypeDTO dto = new IssueTypeDTO(id++, name);
            storeType(name, dto);
        }
        
        private void addSubtask(String name) {
            IssueTypeDTO dto = new IssueTypeDTO(id++, name);
            storeType(name, dto);
        }
        
        private void storeType(String name, IssueTypeDTO dto) {
            types.put(name,dto);
        }
        
        public IssueTypeDTO getExistent(String type) {
            if(isEmpty(type))
                Assert.fail("Type not configured");
            return types.get(type);
        }
        
        public Optional<IssueTypeDTO> getOptional(String type) {
            if(isEmpty(type) || !types.containsKey(type))
                return Optional.empty();
            return Optional.of(types.get(type));
        }
        
    }
    
    private class StatusRepository {
        
        private Map<String,StatusDto> statuses = new LinkedHashMap<>();
        
        private StatusDto create(String name) {
            StatusDto status = new StatusDto(KpiEnvironment.this,name);
            statuses.put(name,status);
            return status;
        }
    }
    
    public static class IssueTypeDTO {
        private long id;
        private String name;
        
        public IssueTypeDTO(long id, String name) {
            this.id = id;
            this.name = name;
        }

        public IssueTypeKpi buildIssueTypeKpi() {
            return new IssueTypeKpi(this.id,this.name);
        }

        public Long id() {
            return id;
        }
        
        public String name() {
            return name;
        }
    }
        
    public static class StatusDto {
        
        private String name;
        private boolean isProgressingStatus;
        private KpiEnvironment environment;

        private StatusDto(KpiEnvironment environment, String name) {
            this.environment = environment;
            this.name = name;
        }
        
        public KpiEnvironment isProgressing() {
            this.isProgressingStatus = true;
            return environment;
        }
        
        public KpiEnvironment isNotProgressing() {
            this.isProgressingStatus = false;
            return environment;
        }
        
        public String name() {
            return name;
        }
        
        public boolean isProgressingStatus() {
            return isProgressingStatus;
        }
        
        @Override
        public String toString() {
            return this.name;
        }
        
    }

}
