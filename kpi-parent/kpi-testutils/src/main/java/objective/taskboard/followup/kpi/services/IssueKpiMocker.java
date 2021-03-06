package objective.taskboard.followup.kpi.services;

import static objective.taskboard.followup.kpi.KpiLevel.DEMAND;
import static objective.taskboard.followup.kpi.KpiLevel.FEATURES;
import static objective.taskboard.followup.kpi.KpiLevel.SUBTASKS;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.mockito.Mockito;

import objective.taskboard.data.Issue;
import objective.taskboard.data.Worklog;
import objective.taskboard.followup.kpi.ChildrenWorklogDistributor;
import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.IssueTypeKpi;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.StatusTransition;
import objective.taskboard.followup.kpi.SubtaskWorklogDistributor;
import objective.taskboard.followup.kpi.ZonedWorklog;
import objective.taskboard.followup.kpi.properties.IssueTypeChildrenStatusHierarchy;
import objective.taskboard.followup.kpi.properties.KPIProperties;
import objective.taskboard.followup.kpi.services.KpiEnvironment.IssueTypeDTO;
import objective.taskboard.followup.kpi.transformer.IssueKpiDataItemAdapter;
import objective.taskboard.utils.DateTimeUtils;

public class IssueKpiMocker {

    final KpiEnvironment parentEnvironment;
    private TransitionsBuilder transitionBuilder;
    private WorklogsBuilder worklogsBuilder = new WorklogsBuilder();
    private FieldsBuilder fieldsBuidler = new FieldsBuilder();
    private final String pKey;
    private Optional<IssueTypeDTO> type = Optional.empty();
    private boolean typeConfigured = false;
    private KpiLevel level = KpiLevel.UNMAPPED;

    private List<IssueKpiMocker> children = new LinkedList<>();
    private IssueKpiMocker parent;
    private String projectKey;
    private Issue mockedIssue;
    private boolean shouldDistributeWorklog = true;
    
    IssueKpiMocker(KpiEnvironment fatherEnvironment, String pKey) {
        this.parentEnvironment = fatherEnvironment;
        this.transitionBuilder = new TransitionsBuilder(this);
        this.pKey = pKey;
    }

    public KpiEnvironment eoI() {
        return parentEnvironment;
    }

    public IssueKpiMocker subtask(String subtaskKey) {
        return createChild(subtaskKey).isSubtask();
    }

    public IssueKpiMocker feature(String subtaskKey) {
        if(this.level != KpiLevel.DEMAND)
            Assert.fail("Features must be called only inside a Demand");

        return createChild(subtaskKey).isFeature();
    }

    private IssueKpiMocker createChild(String subtaskKey) {
        IssueKpiMocker child = new IssueKpiMocker(parentEnvironment,subtaskKey).setParent(this);
        child.project(projectKey);
        return child;
    }

    private IssueKpiMocker setParent(IssueKpiMocker parent) {
        this.parent = parent;
        parent.addChild(this);
        return this;
    }

    private IssueKpiMocker addChild(IssueKpiMocker child) {
        this.children.add(child);
        return this;
    }

    public IssueKpiMocker endOfSubtask() {
        if(parent == null)
            Assert.fail("Parent issue not configured");
        return this.parent;
    }

    public IssueKpiMocker endOfFeature() {
        return endOfSubtask();
    }

    public IssueKpiMocker type(String type) {
        this.type = parentEnvironment.getOptionalFromExistentType(type);
        this.typeConfigured = true;
        return this;
    }

    public IssueKpiMocker emptyType() {
        this.type = Optional.empty();
        this.typeConfigured = true;
        return this;
    }

    public String getIssueKey() {
        return pKey;
    }

    public Optional<IssueTypeKpi> getIssueTypeKpi() {
        return type.map(IssueTypeDTO::buildIssueTypeKpi);
    }

    public IssueKpiMocker isFeature() {
        this.level = KpiLevel.FEATURES;
        return this;
    }

    public IssueKpiMocker isSubtask() {
        this.level = KpiLevel.SUBTASKS;
        return this;
    }

    public IssueKpiMocker isDemand() {
        this.level = KpiLevel.DEMAND;
        return this;
    }

    public IssueKpiMocker unmappedLevel() {
        this.level = KpiLevel.UNMAPPED;
        return this;
    }

    public KpiLevel level() {
        return level;
    }

    public String project() {
        return projectKey;
    }

    public boolean noProjectConfigured() {
        return projectKey == null;
    }

    public IssueKpiMocker project(String projectKey) {
        this.projectKey = projectKey;
        return this;
    }

    public TransitionsBuilder withTransitions() {
        return transitionBuilder;
    }
    
    public IssueKpiMocker withPreconfiguredTransition(String name) {
        this.transitionBuilder = parentEnvironment.getPreconfiguredTransition(name);
        return this;
    }
    
    public WorklogsBuilder worklogs() {
        return worklogsBuilder;
    }

    public Map<String, ZonedDateTime> getReversedTransitions() {
        return transitionBuilder.getReversedTransitions();
    }

    public List<IssueKpiMocker> allMockers() {
        List<IssueKpiMocker> allMockers = new LinkedList<>();
        allMockers.add(this);
        children.forEach(c -> allMockers.addAll(c.allMockers()));
        return allMockers;
    }

    public IssueKpiDataItemAdapter buildAsAdapter() {
        return new FakeIssueKpiAdapter(getReversedTransitions(), getIssueKey(),getIssueTypeKpi(), level,getFields());
    }

    private Map<String,String> getFields() {
        return fieldsBuidler.fields;
    }

    public IssueKpi buildIssueKpi() {
        Optional<StatusTransition> firstChain = this.transitionBuilder.getFirstStatusTransition();
        Optional<IssueTypeKpi> kpiType = getIssueType();

        IssueKpi kpi = new IssueKpi(pKey, kpiType, level,firstChain,parentEnvironment.getClock());
        String environmentField = parentEnvironment.getKPIProperties().getEnvironmentField();
        kpi.setClientEnvironment(getOptionalValueFromField(environmentField));
        this.children.stream()
            .map(IssueKpiMocker::buildIssueKpi)
            .forEach(kpi::addChild);

        distributeWorklogs(kpi);

        return kpi;
    }

    private Optional<String> getOptionalValueFromField(String environmentField) {
        return Optional.ofNullable(getFields().get(environmentField));
    }

    public Issue mockedIssue() {
        if(this.mockedIssue == null)
            mockAllJiraIssue();
        return this.mockedIssue;
    }

    public List<IssueKpi> buildAllIssuesKpi() {
        Optional<StatusTransition> firstChain = this.transitionBuilder.getFirstStatusTransition();
        Optional<IssueTypeKpi> kpiType = getIssueType();
        LinkedList<IssueKpi> allIssues = new LinkedList<>();
        IssueKpi kpi = new IssueKpi(pKey, kpiType, level,firstChain,parentEnvironment.getClock());
        allIssues.add(kpi);

        for (IssueKpiMocker issueKpiMocker : children) {
            List<IssueKpi> childrenIssuesKpis = issueKpiMocker.buildAllIssuesKpi();
            childrenIssuesKpis.forEach(c -> kpi.addChild(c));
            allIssues.addAll(childrenIssuesKpis);
        }

        distributeWorklogs(kpi);
        return allIssues;
    }

    public List<Issue> mockAllJiraIssue() {
        List<Issue> allIssues = new LinkedList<>();
        allIssues.add(mock());
        for (IssueKpiMocker childIssueKpiMocker : children) {
            List<Issue> childrenIssuesKpis = childIssueKpiMocker.mockAllJiraIssue();
            allIssues.addAll(childrenIssuesKpis);
        }

        return allIssues;
    }

    public Issue mock() {
        Issue issue = Mockito.mock(Issue.class);
        when(issue.getProjectKey()).thenReturn(projectKey);
        when(issue.getStatus()).thenReturn(getStatusId());
        when(issue.getIssueKey()).thenReturn(pKey);
        when(issue.getIssueTypeName()).thenReturn(getIssueType().map(t -> t.getType()).orElse("Unmapped"));
        when(issue.isDemand()).thenReturn(DEMAND == this.level);
        when(issue.isFeature()).thenReturn(FEATURES == this.level);
        when(issue.isSubTask()).thenReturn(SUBTASKS == this.level);
        when(issue.getWorklogs()).thenReturn(worklogsBuilder.getWorklogs());
        when(issue.getExtraFields()).thenReturn(getFields());
        if(hasParentIssue())
            when(issue.getParent()).thenReturn(parent.pKey);
        this.mockedIssue = issue;
        return issue;
    }

    private boolean hasParentIssue() {
        return parent != null;
    }

    private long getStatusId() {
        return transitionBuilder.currentStatus().id();
    }

    public IssueKpiMocker preventWorklogDistribution() {
        this.shouldDistributeWorklog  = false;
        return this;
    }

    private void distributeWorklogs(IssueKpi kpi) {
        if(!shouldDistributeWorklog)
            return;
        if (level == KpiLevel.UNMAPPED) {
            return;
        }
        if (level == KpiLevel.SUBTASKS) {
            this.worklogsBuilder.setup(kpi);
            return;
        }
        KPIProperties kpiProperties = this.parentEnvironment.getKPIProperties();
        IssueTypeChildrenStatusHierarchy hierarchy = level == KpiLevel.FEATURES
                                                            ? kpiProperties.getFeaturesHierarchy()
                                                            : kpiProperties.getDemandHierarchy();
        ChildrenWorklogDistributor.distributeWorklogs(hierarchy, kpi);
    }

    private Optional<IssueTypeKpi> getIssueType() {
        if(!typeConfigured)
            throw new AssertionError("Configure a type or explicitly call emptyType(). Issue key:"+pKey);

        return type.map(dto -> new IssueTypeKpi(dto.id(),dto.name()));
    }

    @Override
    public String toString() {
        String typeName = this.type.map(t -> t.name()).orElse("NOT CONFIGURED");
        return String.format("[%s] %s", typeName, pKey);
    }
    
    public FieldsBuilder fields() {
        return fieldsBuidler;
    }
    
    public class FieldsBuilder {
        
        private Map<String,String> fields = new LinkedHashMap<>();
        
        public FieldMapperHelper field(String field) {
            return new FieldMapperHelper(field);
        }
        
        public FieldsBuilder put(String attribute, String value) {
            fields.put(attribute, value);
            return this;
        }
        
        public IssueKpiMocker eoF() {
            return IssueKpiMocker.this;
        }
        
        public class FieldMapperHelper {
            private String attribute;
            
            public FieldMapperHelper(String field) {
                this.attribute = field;
            }
            
            public FieldsBuilder value(String value) {
                return FieldsBuilder.this.put(attribute,value);
            }

        }

        
    }

    public class WorklogsBuilder {

        private List<WorklogDto> worklogs = new LinkedList<>();

        public WorklogDto at(String date) {
            WorklogDto dto = new WorklogDto(date);
            worklogs.add(dto);
            return dto;
        }

        public void setup(IssueKpi kpi) {
            List<Worklog> realWorklogs = getWorklogs();
            if(realWorklogs.isEmpty())
                return;

            List<ZonedWorklog> zonedWorklogs = realWorklogs.stream()
                    .map(w -> new ZonedWorklog(w, parentEnvironment.getTimezone()))
                    .collect(Collectors.toList());

            SubtaskWorklogDistributor distributor = new SubtaskWorklogDistributor();
            distributor.distributeWorklogs(kpi, zonedWorklogs);
        }

        private List<Worklog> getWorklogs() {
            return worklogs.stream()
                    .map(w -> w.build())
                    .collect(Collectors.toList());
        }

        public IssueKpiMocker eoW() {
            return IssueKpiMocker.this;
        }

        public class WorklogDto {
            private Date date;
            private int timeSpentInSeconds;

            private WorklogDto(String date) {
                this.date = DateTimeUtils.parseStringToDate(date);
            }

            public Worklog build() {
                return new Worklog("a.developer", date, timeSpentInSeconds);
            }

            public WorklogsBuilder timeSpentInSeconds(int seconds) {
                this.timeSpentInSeconds = seconds;
                return WorklogsBuilder.this;
            }

            public WorklogsBuilder timeSpentInHours(double hours) {
                this.timeSpentInSeconds = Math.toIntExact(DateTimeUtils.hoursToSeconds(hours));
                return WorklogsBuilder.this;
            }

            public IssueKpiMocker eoW() {
                return IssueKpiMocker.this;
            }
        }
    }

    
}
