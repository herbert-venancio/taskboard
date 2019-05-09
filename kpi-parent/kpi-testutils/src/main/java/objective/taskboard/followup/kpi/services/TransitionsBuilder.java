package objective.taskboard.followup.kpi.services;

import static objective.taskboard.utils.DateTimeUtils.parseDateTime;
import static objective.taskboard.utils.DateTimeUtils.parseStringToDate;

import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.assertj.core.api.Assertions;

import objective.taskboard.data.Worklog;
import objective.taskboard.followup.kpi.DatedStatusTransition;
import objective.taskboard.followup.kpi.StatusTransition;
import objective.taskboard.followup.kpi.ZonedWorklog;
import objective.taskboard.followup.kpi.services.KpiEnvironment.StatusDto;

public class TransitionsBuilder {

    private TransitionDto firstTransition;
    private IssueKpiMocker kpiMocker;
    private final KpiEnvironment parentEnvironment;
    private Optional<TransitionDto> lastTransitionWithDate = Optional.empty();

    public TransitionsBuilder(KpiEnvironment fatherEnvironment) {
        this.parentEnvironment = fatherEnvironment;
    }

    public TransitionsBuilder(IssueKpiMocker kpiMocker) {
        this.kpiMocker = kpiMocker;
        this.parentEnvironment = kpiMocker.parentEnvironment;
    }

    public TransitionDto status(String step) {
        TransitionDto transition = new TransitionDto(step);
        configureNext(transition);
        return transition;
    }

    private void put(ZonedWorklog worklog, String step) {
        TransitionDto transition = firstTransition;
        
        while (transition != null) {
            if (step.equals(transition.status.name()))
                break;
            transition = transition.next();
        }
        if (transition == null)
            throw new AssertionError(step + " not found");

        transition.addWorklog(worklog);
    }

    private void configureNext(TransitionDto transition) {
        if (firstTransition == null) {
            firstTransition = transition;
            return;
        }
        firstTransition.setNext(transition);
    }

    public IssueKpiMocker eoT() {
        if (kpiMocker == null)
            throw new IllegalArgumentException("No IssueKpi configured");
        return kpiMocker;
    }

    public KpiEnvironment eoSt() {
        return parentEnvironment;
    }
    public Optional<StatusTransition> getFirstStatusTransition() {
        if (firstTransition == null)
            throw new IllegalArgumentException("Issue with no transition configured");

        return firstTransition.build();
    }

    public TransitionWorklog withWorklogs() {
        return new TransitionWorklog();
    }

    private void lastTransited(TransitionDto transitionDto) {
        this.lastTransitionWithDate = Optional.of(transitionDto);
    }

    public StatusDto currentStatus() {
        return lastTransitionWithDate.map( l -> l.status).orElse(firstTransition.status);
    }

    public Map<String, ZonedDateTime> getReversedTransitions() {
        LinkedList<TransitionDto> reversedOrder = new LinkedList<>();
        TransitionDto currentIndex = firstTransition;
        while(currentIndex.hasNext()) {
           reversedOrder.push(currentIndex);
           currentIndex = currentIndex.next();
        }
        reversedOrder.push(currentIndex);
        Map<String,ZonedDateTime> transitions = new LinkedHashMap<>();
        for (TransitionDto transition : reversedOrder) {
            transitions.put(transition.status.name(), transition.date.orElse(null));
        }

        return transitions;
    }

    public class TransitionDto implements Iterator<TransitionDto> {
        private StatusDto status;
        private Optional<ZonedDateTime> date = Optional.empty();
        private Optional<TransitionDto> next = Optional.empty();
        private List<ZonedWorklog> worklogs = new LinkedList<>();

        private TransitionDto(String name) {
            this.status = parentEnvironment.getStatus(name);
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

        public TransitionsBuilder addWorklog(ZonedWorklog worklog) {
            this.worklogs.add(worklog);
            return TransitionsBuilder.this;
        }

        public TransitionsBuilder date(String date) {
            this.date = Optional.of(parseDateTime(date));
            lastTransited(this);
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

        @Override
        public boolean hasNext() {
            return next.isPresent();
        }

        @Override
        public TransitionDto next() {
            return next.orElseThrow(NoSuchElementException::new);
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
            Assertions.assertThat(date).as("Date can't be null").isNotNull();
            Assertions.assertThat(status).as("Status can't be null").isNotNull();
            ZonedWorklog zonedWorklog = new ZonedWorklog(new Worklog("a.developer", parseStringToDate(date), timeSpentInSeconds), parentEnvironment.getTimezone());
            TransitionsBuilder.this.put(zonedWorklog,status);
        }
    }
}
