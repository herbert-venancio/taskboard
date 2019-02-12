package objective.taskboard.followup.kpi.enviroment;

import static objective.taskboard.followup.kpi.KpiLevel.DEMAND;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;

import objective.taskboard.data.Issue;
import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.IssueTypeKpi;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.enviroment.DSLKpi.BehaviorFactory;
import objective.taskboard.followup.kpi.properties.KPIProperties;
import objective.taskboard.followup.kpi.transformer.IssueKpiDataItemAdapter;
import objective.taskboard.jira.properties.JiraProperties;
import objective.taskboard.testUtils.FixedClock;
import objective.taskboard.utils.Clock;
import objective.taskboard.utils.DateTimeUtils;

public class KpiEnvironment {

    private IssueTypeRepository typeRepository = new IssueTypeRepository();
    private StatusRepository statusRepository = new StatusRepository();
    private KpiPropertiesMocker kpiPropertiesMocker = new KpiPropertiesMocker(this);
    private JiraPropertiesMocker jiraPropertiesMocker = new JiraPropertiesMocker(this);
    private TransitionsBuilder transitionsBuilder = new TransitionsBuilder(this);

    private Map<String,IssueKpiMocker> issues = new LinkedHashMap<>();
    private DSLKpi kpiContext;
    private FixedClock clock = new FixedClock();
    private ZoneId timezone = ZoneId.systemDefault();

    private MockedServices services = new MockedServices(this);

    public KpiEnvironment() {}

    public KpiEnvironment(DSLKpi kpiContext) {
        this.kpiContext = kpiContext;
    }

    public Clock getClock() {
        return clock;
    }

    public KpiEnvironment withTimezone(ZoneId timezone) {
        this.timezone = timezone;
        return this;
    }

    public ZoneId getTimezone() {
        return timezone;
    }

    public DSLKpi then() {
        return kpiContext;
    }

    public DSLKpi eoE() {
        return kpiContext;
    }

    public BehaviorFactory when() {
        return kpiContext.when();
    }

    public KpiPropertiesMocker withKpiProperties() {
        return kpiPropertiesMocker;
    }

    public KpiEnvironment withKpiProperties(KpiPropertiesMockBuilder<?> builder) {
        kpiPropertiesMocker.put(builder);
        return this;
    }

    public JiraPropertiesMocker withJiraProperties() {
        return jiraPropertiesMocker;
    }

    public KpiEnvironment todayIs(String date) {
        ZonedDateTime zonedDate = DateTimeUtils.parseDateTime(date);
        clock.setNow(zonedDate.toInstant());
        return this;
    }

    public KpiEnvironment withDemandType(String name) {
        typeRepository.addDemand(name);
        return this;
    }

    public IssueTypeRepository types() {
        return typeRepository;
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

    public MockedServices services() {
        return services;
    }

    public KPIProperties getKPIProperties() {
        return getKPIProperties(KPIProperties.class);
    }

    public <T> T getKPIProperties(Class<T> propertiesClass) {
        return kpiPropertiesMocker.getKpiProperties(propertiesClass);
    }

    public JiraProperties getJiraProperties() {
        return jiraPropertiesMocker.getJiraProperties();
    }

    public TransitionsBuilder statusTransition() {
        return transitionsBuilder;
    }

    public IssueKpiMocker givenIssue(String pKey) {
        IssueKpiMocker issueMocker = new IssueKpiMocker(this, pKey);
        issues.putIfAbsent(pKey, issueMocker);
        return issues.get(pKey);
    }

    public IssueKpiMocker givenSubtask(String pkey) {
        return givenIssue(pkey).isSubtask();
    }

    public IssueKpiMocker givenFeature(String pkey) {
        return givenIssue(pkey).isFeature();
    }

    public IssueKpiMocker givenDemand(String pkey) {
        return givenIssue(pkey).isDemand();
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

    public StatusRepository statuses() {
        return statusRepository;
    }

    public KpiEnvironment mockAllIssues() {
        issues.values().forEach(i -> i.mockAllJiraIssue());
        return this;
    }

    public List<Issue> collectIssuesMocked() {
        return getAllIssueMockers().stream().map( i -> i.mockedIssue()).collect(Collectors.toList());
    }

    public List<IssueKpiMocker> getAllIssueMockers(){
        return issues.values().stream()
                .map(i -> i.allMockers())
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public List<IssueKpiDataItemAdapter> getAllIssuesAdapters() {
        return getAllIssueMockers().stream().map(IssueKpiMocker::buildAsAdapter).collect(Collectors.toList());
    }

    public List<Long> collectTypeIds(List<String> childrenTypes) {
        return typeRepository.types.values().stream()
                .filter(type -> childrenTypes.contains(type.name))
                .map(type -> type.id)
                .collect(Collectors.toList());
    }

    public class IssueTypeRepository {
        private long id = 1l;
        private Map<String,IssueTypeDTO> types = new LinkedHashMap<>();

        public IssueTypeRepository addDemand(String name) {
            IssueTypeDTO dto = new IssueTypeDTO(id++, DEMAND,name);
            storeType(name, dto);
            return this;
        }

        public IssueTypeRepository addUnmapped(String name) {
            IssueTypeDTO dto = new IssueTypeDTO(id++, KpiLevel.UNMAPPED,name);
            storeType(name, dto);
            return this;
        }

        private void addFeature(String name) {
            IssueTypeDTO dto = new IssueTypeDTO(id++, KpiLevel.FEATURES,name);
            storeType(name, dto);
        }

        private void addSubtask(String name) {
            IssueTypeDTO dto = new IssueTypeDTO(id++, KpiLevel.SUBTASKS,name);
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

        public IssueTypeRepository addFeatures(String...features) {
            for (String featureType : features) {
                addFeature(featureType);
            }
            return this;
        }

        public IssueTypeRepository addSubtasks(String... subtasks) {
            for (String subtaskType : subtasks) {
                addSubtask(subtaskType);
            }
            return this;
        }

        public KpiEnvironment eoT() {
            return KpiEnvironment.this;
        }

        public List<IssueTypeDTO> configuredForLevel(KpiLevel level){
            return types.values().stream().filter(t -> level == t.level()).collect(Collectors.toList());
        }

        public void foreach(Consumer<? super IssueTypeDTO> action) {
            this.types.values().forEach(action);
        }
    }

    public class StatusRepository {

        private long id = 1l;
        private Map<String,StatusDto> statuses = new LinkedHashMap<>();

        private StatusDto create(String name) {
            StatusDto status = new StatusDto(id++,KpiEnvironment.this,name);
            statuses.put(name,status);
            return status;
        }

        public List<String> getProgressingStatuses() {
            return statuses.values().stream()
                    .filter(s -> s.isProgressingStatus())
                    .map(s -> s.name())
                    .collect(Collectors.toList());
        }

        Collection<StatusDto> getStatuses() {
            return statuses.values();
        }

        public StatusRepository withProgressingStatuses(String... statuses) {
            createBulk(true,statuses);
            return this;
        }

        public StatusRepository withNotProgressingStatuses(String... statuses) {
            createBulk(false,statuses);
            return this;
        }

        private void createBulk(boolean isProgressing,String... statuses) {
            Stream.of(statuses).forEach(s ->
                create(s)
                .setProgressingStatus(isProgressing));
        }

        public KpiEnvironment eoS() {
            return KpiEnvironment.this;
        }

    }

    public static class IssueTypeDTO {
        private long id;
        private String name;
        private KpiLevel level = KpiLevel.UNMAPPED;

        public IssueTypeDTO(long id, KpiLevel level, String name) {
            this.id = id;
            this.name = name;
            this.level = level;
        }

        public KpiLevel level() {
            return level;
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

        public boolean isSubtask() {
            return level == KpiLevel.SUBTASKS;
        }

    }
    public static class StatusDto {

        private String name;
        private boolean isProgressingStatus;
        private KpiEnvironment environment;
        private long id;

        private StatusDto(long id, KpiEnvironment environment, String name) {
            this.id = id;
            this.environment = environment;
            this.name = name;
        }

        private void setProgressingStatus(boolean isProgressing) {
            this.isProgressingStatus = isProgressing;
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

        public long id() {
            return id;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    public interface KpiPropertiesMockBuilder<T> {

        Class<T> propertiesClass();

        T build(KpiEnvironment environment);
    }
}
