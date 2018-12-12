package objective.taskboard.followup.kpi.enviroment;

import static objective.taskboard.utils.DateTimeUtils.parseDateTime;
import static objective.taskboard.utils.DateTimeUtils.parseStringToDate;
import static org.junit.Assert.fail;

import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import objective.taskboard.data.Worklog;
import objective.taskboard.followup.kpi.DatedStatusTransition;
import objective.taskboard.followup.kpi.StatusTransition;
import objective.taskboard.followup.kpi.enviroment.KpiEnvironment.StatusDto;

public class TransitionsBuilder {

    private TransitionDto firstTransition;
    private IssueKpiMocker kpiMocker;
    private final KpiEnvironment fatherEnvironment;

    public TransitionsBuilder(KpiEnvironment fatherEnvironment) {
        this.fatherEnvironment = fatherEnvironment;
    }

    public TransitionsBuilder(IssueKpiMocker kpiMocker) {
        this.kpiMocker = kpiMocker;
        this.fatherEnvironment = kpiMocker.fatherEnvironment;
    }

    public TransitionDto status(String step) {
        TransitionDto transition = new TransitionDto(step);
        configureNext(transition);
        return transition;
    }

    private void put(Worklog worklog, String step) {
        TransitionDto transition = firstTransition;
        while (transition != null) {
            if (step.equals(transition.status.name()))
                break;
            transition = transition.next.get();
        }
        if (transition == null)
            fail(step + " not found");

        transition.addWorklog(worklog);
    }

    private void configureNext(TransitionDto transition) {
        if (firstTransition == null) {
            firstTransition = transition;
            return;
        }
        firstTransition.setNext(transition);
    }

    public void setIssueKpi(IssueKpiMocker issueKpiMocker) {
        this.kpiMocker = issueKpiMocker;
    }
    
    public IssueKpiMocker eoT() {
        if (kpiMocker == null)
            throw new IllegalArgumentException("No IssueKpi configured");
        return kpiMocker;
    }

    public KpiEnvironment eoSt() {
        return fatherEnvironment;
    }
    public Optional<StatusTransition> getFirtStatusTransition() {
        if (firstTransition == null)
            throw new IllegalArgumentException("Issue with no transition configured");

        return firstTransition.build();
    }

    public TransitionWorklog withWorklogs() {
        return new TransitionWorklog();
    }

    public class TransitionDto {
        private StatusDto status;
        private Optional<ZonedDateTime> date = Optional.empty();
        private Optional<TransitionDto> next = Optional.empty();
        private List<Worklog> worklogs = new LinkedList<>();

        private TransitionDto(String name) {
            this.status = fatherEnvironment.getStatus(name);
        }

        private void setNext(TransitionDto last) {
            if (!this.next.isPresent()) {
                this.next = Optional.of(last);
                return;
            }

            this.next.get().setNext(last);
        }

        public Optional<StatusTransition> build() {
            if (!next.isPresent())
                return buildStatus(Optional.empty());
            Optional<StatusTransition> nextChain = next.get().build();
            return buildStatus(nextChain);
        }

        private Optional<StatusTransition> buildStatus(Optional<StatusTransition> nextChain) {
            String transitionName = this.status.name();
            boolean isProgressingStatus = this.status.isProgressingStatus();
            StatusTransition chain = date.isPresent()
                    ? new DatedStatusTransition(transitionName, date.get(), isProgressingStatus, nextChain)
                    : new StatusTransition(transitionName, isProgressingStatus, nextChain);

            this.worklogs.stream().forEach(w -> chain.putWorklog(w));

            return Optional.of(chain);
        }

        public TransitionsBuilder addWorklog(Worklog worklog) {
            this.worklogs.add(worklog);
            return TransitionsBuilder.this;
        }

        public TransitionsBuilder date(String date) {
            this.date = Optional.of(parseDateTime(date));
            return TransitionsBuilder.this;
        }

        public TransitionsBuilder noDate() {
            return TransitionsBuilder.this;
        }

        public IssueKpiMocker eoT() {
            return kpiMocker;
        }

        @Override
        public String toString() {
            return String.format("%s [%s]", this.status, this.date.map(d -> d.toString()).orElse("Not Dated"));
        }

    }

    public class TransitionWorklog {
        private int timeSpentInSeconds;
        private String date;
        private String status;

        public TransitionWorklog timeSpent(int time) {
            this.timeSpentInSeconds = time;
            return this;
        }

        public TransitionWorklog on(String status) {
            this.status = status;
            
            return this;
        }

        public TransitionWorklog withDate(String date) {
            this.date = date;
            return this;
        }
        
        public TransitionsBuilder eoW() {
            registerWorklog();
            return TransitionsBuilder.this;
        }
        
        public TransitionWorklog and() {
            registerWorklog();
            return new TransitionWorklog();
        }
        
        private void registerWorklog() {
            TransitionsBuilder.this.put(new Worklog("a.developer", parseStringToDate(date), timeSpentInSeconds),status);
        }

    }

}
