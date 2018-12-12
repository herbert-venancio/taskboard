package objective.taskboard.followup.kpi.enviroment;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Assert;

import objective.taskboard.data.Worklog;
import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.IssueTypeKpi;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.StatusTransition;
import objective.taskboard.followup.kpi.SubtaskWorklogDistributor;
import objective.taskboard.followup.kpi.enviroment.KpiEnvironment.IssueTypeDTO;
import objective.taskboard.utils.DateTimeUtils;

public class IssueKpiMocker {
    
    final KpiEnvironment fatherEnvironment;
    private TransitionsBuilder transitionBuilder;
    private WorklogsBuilder worklogsBuilder = new WorklogsBuilder();
    private String pKey;
    private Optional<IssueTypeDTO> type;
    private KpiLevel level;
    
    private List<IssueKpiMocker> children = new LinkedList<>();
    private IssueKpiMocker parent;

    IssueKpiMocker(KpiEnvironment fatherEnvironment, TransitionsBuilder transitionBuilder,String pKey) {
        this.fatherEnvironment = fatherEnvironment;
        this.transitionBuilder = transitionBuilder;
        this.transitionBuilder.setIssueKpi(this);
        this.pKey = pKey;
    }
    
    public KpiEnvironment eoI() {
        return fatherEnvironment;
    }
    
    public IssueKpiMocker subtask(String subtaskKey) {
        IssueKpiMocker child = new IssueKpiMocker(fatherEnvironment,transitionBuilder,subtaskKey);
        child.setParent(this);
        child.isSubtask();
        children.add(child);
        return child;
    }
    
    private IssueKpiMocker setParent(IssueKpiMocker parent) {
        this.parent = parent;
        return this;
    }
    
    public IssueKpiMocker endOfSubtask() {
        if(parent == null)
            Assert.fail("Parent issue not configured");
        return this.parent;
    }

    public IssueKpiMocker type(String type) {
        this.type = fatherEnvironment.getOptionalType(type);
        return this;
    }
    
    public IssueKpiMocker emptyType() {
        this.type = Optional.empty();
        return this;
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

    public TransitionsBuilder withTransitions() {
        return transitionBuilder;
    }
    
    public IssueKpiMocker withTransitions(TransitionsBuilder transitions) {
        this.transitionBuilder = transitions;
        return this;
    }
    
    public WorklogsBuilder worklogs() {
        return worklogsBuilder;
    }
    
    public IssueKpi buildAsIssueKpi() {
        Optional<StatusTransition> firstChain = this.transitionBuilder.getFirtStatusTransition();
        Optional<IssueTypeKpi> kpiType = getIssueType();
        
        IssueKpi kpi = new IssueKpi(pKey, kpiType, level,firstChain,fatherEnvironment.getClock());
        
        this.children.stream()
            .map(c -> c.buildAsIssueKpi())
            .forEach( c-> kpi.addChild(c));
        
        this.worklogsBuilder.setup(kpi);
        
        return kpi;
    }
    
    public IssueKpi buildIssueKpi() {
        Optional<StatusTransition> firstChain = this.transitionBuilder.getFirtStatusTransition();
        Optional<IssueTypeKpi> kpiType = getIssueType();
        
        IssueKpi kpi = new IssueKpi(pKey, kpiType, level,firstChain,fatherEnvironment.getClock());
        
        this.children.stream()
            .map(c -> c.buildAsIssueKpi())
            .forEach( c-> kpi.addChild(c));
        
        this.worklogsBuilder.setup(kpi);
        
        return kpi;
    }
    
    private Optional<IssueTypeKpi> getIssueType() {
        if(type == null)
            Assert.fail("Configure a type or explicitly cal emptyType()");
        if(!type.isPresent())
            return Optional.empty();
        
        IssueTypeDTO dto = type.get();
        return Optional.of( new IssueTypeKpi(dto.id(), dto.name()));
    }

    public class WorklogsBuilder {
        
        private List<WorklogDto> worklogs = new LinkedList<>();
        
        public WorklogDto at(String date) {
            WorklogDto dto = new WorklogDto(date);
            worklogs.add(dto);
            return dto;
        }
        
        public void setup(IssueKpi kpi) {
            List<Worklog> realWorklogs = worklogs.stream().map(w -> w.build()).collect(Collectors.toList());
            if(realWorklogs.isEmpty())
                return;
            
            SubtaskWorklogDistributor distributor = new SubtaskWorklogDistributor();
            distributor.distributeWorklogs(kpi, realWorklogs);
            
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
            
            public IssueKpiMocker eoW() {
                return IssueKpiMocker.this;
            }
        }
    }
    
}
