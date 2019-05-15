package objective.taskboard.domain.converter;

import static java.util.Arrays.asList;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.mock;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.BiMap;

import objective.taskboard.domain.Filter;
import objective.taskboard.domain.Lane;
import objective.taskboard.domain.Stage;
import objective.taskboard.domain.Step;
import objective.taskboard.filter.LaneService;
import objective.taskboard.repository.LaneRepository;
import objective.taskboard.testUtils.JsonBuilder;

public class StartDateStepServiceTestDSL {

    private final BiMap<Long, String> issueTypeMap;
    private final BiMap<Long, String> statusMap;

    private AtomicLong laneIdGenerator = new AtomicLong();
    private AtomicLong stageIdGenerator = new AtomicLong();
    private AtomicLong stepIdGenerator = new AtomicLong();

    private Map<String, Lane> lanes = new HashMap<>();
    private Map<Pair<Lane, String>, Stage> stages = new HashMap<>();
    private Map<Pair<Stage, String>, Step> steps = new HashMap<>();

    public final StartDateStepService startDateStepService;

    StartDateStepServiceTestDSL(BiMap<Long, String> issueTypeMap, BiMap<Long, String> statusMap) {
        this.issueTypeMap = issueTypeMap;
        this.statusMap = statusMap;

        LaneRepository laneRepository = mock(LaneRepository.class);
        willAnswer(invocation -> new ArrayList<>(lanes.values())).given(laneRepository).findAll();
        LaneService laneService = new LaneService(laneRepository);
        startDateStepService = new StartDateStepService(laneService);
    }

    void givenLane(String laneName, StageBuilder... stages) {
        new LaneBuilder(laneName, stages).build(this);
    }

    static StageBuilder withStage(String stageName, StepBuilder... steps) {
        return new StageBuilder(stageName, steps);
    }

    static StepBuilder withStep(String stepName, FilterBuilder... filters) {
        return new StepBuilder(stepName, filters);
    }

    static FilterBuilder withFilterFor() {
        return new FilterBuilder();
    }

    static class LaneBuilder {

        public final String name;
        public final List<StageBuilder> stages;

        public LaneBuilder(String name, StageBuilder[] stages) {
            this.name = name;
            this.stages = asList(stages);
        }

        public void build(StartDateStepServiceTestDSL dsl) {
            stages.forEach(stage ->
                    stage.steps.forEach(step ->
                            step.filters.forEach(filter ->
                                    dsl.createFilter(filter.issueType, filter.status, step.name, stage.name, name)
                            )
                    )
            );
        }

    }

    static class StageBuilder {

        public final String name;
        public final List<StepBuilder> steps;

        public StageBuilder(String name, StepBuilder[] steps) {
            this.name = name;
            this.steps = asList(steps);
        }

    }

    static class StepBuilder {

        public final String name;
        public final List<FilterBuilder> filters;

        public StepBuilder(String name, FilterBuilder[] filters) {
            this.name = name;
            this.filters = asList(filters);
        }

    }

    static class FilterBuilder {

        private String issueType;

        private String status;

        public FilterBuilder issueType(String issueType) {
            this.issueType = issueType;
            return this;
        }

        public FilterBuilder status(String status) {
            this.status = status;
            return this;
        }

    }

    private Filter createFilter(String issueType, String status, String stepName, String stageName, String laneName) {
        long issueTypeId = Optional.ofNullable(issueTypeMap.inverse().get(issueType))
                .orElseThrow(() -> new AssertionError("invalid issueType: " + issueType));
        long statusId = Optional.ofNullable(statusMap.inverse().get(status))
                .orElseThrow(() -> new AssertionError("invalid status: " + status));
        Lane lane = lanes.computeIfAbsent(laneName, name -> {
            Lane newLane = new Lane();

            long id = laneIdGenerator.incrementAndGet();
            newLane.setId(id);
            newLane.setOrdem(0);
            newLane.setRules(new ArrayList<>());

            newLane.setName(name);
            newLane.setStages(new ArrayList<>());
            return newLane;
        });
        Stage stage = stages.computeIfAbsent(Pair.of(lane, stageName), key -> {
            Stage newStage = new Stage();

            long id = stageIdGenerator.incrementAndGet();
            newStage.setId(id);
            newStage.setOrdem(0);
            newStage.setWeight(0.0);

            newStage.setName(stageName);
            newStage.setLane(lane);
            newStage.setSteps(new ArrayList<>());
            lane.getStages().add(newStage);
            return newStage;
        });
        Step step = steps.computeIfAbsent(Pair.of(stage, stepName), key -> {
            Step newStep = new Step();

            long id = stepIdGenerator.incrementAndGet();
            newStep.setId(id);
            newStep.setOrdem(0);
            newStep.setWeight(0.0);
            newStep.setShowHeader(false);

            newStep.setName(stepName);
            newStep.setStage(stage);
            newStep.setFilters(new ArrayList<>());
            stage.getSteps().add(newStep);
            return newStep;
        });
        Filter filter = new Filter();
        filter.setIssueTypeId(issueTypeId);
        filter.setStatusId(statusId);
        filter.setStep(step);
        step.getFilters().add(filter);
        return filter;
    }

    public JsonBuilder.JsonObjectPropertyBuilder withStatus(String status) {
        long statusId = statusMap.inverse().get(status);
        return JsonBuilder.property("status"
                , JsonBuilder.object(
                        JsonBuilder.property("id", statusId)
                        , JsonBuilder.property("name", status)
                )
        );
    }

    public JsonBuilder.JsonObjectPropertyBuilder withIssueType(String issueType) {
        long issueTypeId = issueTypeMap.inverse().get(issueType);
        return JsonBuilder.property("issuetype"
                , JsonBuilder.object(
                        JsonBuilder.property("id", issueTypeId)
                        , JsonBuilder.property("name", issueType)
                )
        );
    }

    public ChangelogItemBuilder movedFrom(String statusName) {
        return new ChangelogItemBuilder().from(statusName);
    }

    class ChangelogItemBuilder extends JsonBuilder.JsonObjectBuilder {

        private final JsonObjectBuilder item;

        public ChangelogItemBuilder() {
            item = JsonBuilder.object(JsonBuilder.property("field", "status"));
            add(JsonBuilder.property("items", JsonBuilder.array(item)));
        }

        public ChangelogItemBuilder from(String fromString) {
            Long fromId = statusMap.inverse().get(fromString);
            item.add(JsonBuilder.property("from", fromId));
            item.add(JsonBuilder.property("fromString", fromString));
            return this;
        }

        public ChangelogItemBuilder to(String toString) {
            Long toId = statusMap.inverse().get(toString);
            item.add(JsonBuilder.property("to", toId));
            item.add(JsonBuilder.property("toString", toString));
            return this;
        }

        public ChangelogItemBuilder on(String instant) {
            Long created = Instant.parse(instant).toEpochMilli();
            add(JsonBuilder.property("created", created));
            return this;
        }

    }

}
