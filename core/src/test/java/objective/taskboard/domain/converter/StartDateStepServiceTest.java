package objective.taskboard.domain.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import objective.taskboard.domain.Filter;
import objective.taskboard.domain.Lane;
import objective.taskboard.domain.Stage;
import objective.taskboard.domain.Step;
import objective.taskboard.jira.client.JiraIssueDto;
import objective.taskboard.repository.FilterCachedRepository;

public class StartDateStepServiceTest {

    private AtomicLong laneIdGenerator;
    private AtomicLong stageIdGenerator;
    private AtomicLong stepIdGenerator;

    private Map<String, Lane> lanes = new HashMap<>();
    private Map<Pair<Lane, String>, Stage> stages = new HashMap<>();
    private Map<Pair<Stage, String>, Step> steps = new HashMap<>();
    private List<Filter> filters = new ArrayList<>();

    private StartDateStepService startDateStepService;

    @Before
    public void resetIdGenerators() {
        laneIdGenerator = new AtomicLong();
        stageIdGenerator = new AtomicLong();
        stepIdGenerator = new AtomicLong();
    }

    @Before
    public void setupMocks() {
        FilterCachedRepository filterRepository = mock(FilterCachedRepository.class);
        willAnswer(invocation -> filters).given(filterRepository).getCache();
        startDateStepService = new StartDateStepService(filterRepository);
    }

    @Test
    public void givenIssueMovedToPlanningStepThenBackToBallparkStep_whenCalculateStartDate_thenDateIsLastTimeItEnteredBallparkStep() throws IOException {
        // issueTypeId, statusId, step, stage, lane
        createFilter(7, 1    , "Ballpark", "Ballpark", "Demand");
        createFilter(7, 10151, "Ballpark", "Ballpark", "Demand");
        createFilter(7, 10252, "Ballpark", "Ballpark", "Demand");
        createFilter(7, 10253, "Ballpark", "Ballpark", "Demand");
        createFilter(7, 10152, "Planning", "Planning", "Demand");

        JiraIssueDto issue = new ObjectMapper().readValue(
                "{" +
                    "\"changelog\":{" +
                        "\"histories\":[" +
                            "{\"created\":1536003106000,\"items\":[{\"field\":\"status\",\"from\":\"1\",\"to\":\"10151\",\"fromString\":\"Aberto\",\"toString\":\"OG\"}]}," +
                            "{\"created\":1536003125000,\"items\":[{\"field\":\"status\",\"from\":\"10151\",\"to\":\"10152\",\"fromString\":\"OG\",\"toString\":\"Planejamento\"}]}," +
                            "{\"created\":1547036284000,\"items\":[{\"field\":\"status\",\"from\":\"10152\",\"to\":\"10151\",\"fromString\":\"Planejamento\",\"toString\":\"OG\"}]}" +
                        "]" +
                    "}," +
                    "\"key\":\"TEST-1000\"," +
                    "\"id\":10000," +
                    "\"fields\":{" +
                        "\"status\":{\"id\":10151,\"name\":\"OG\"}," +
                        "\"issuetype\":{\"id\":7,\"name\":\"Demanda\"}," +
                        "\"updated\":1548099986000," +
                        "\"created\":1535637700000" +
                    "}" +
                "}"
                , JiraIssueDto.class);

        long value = startDateStepService.get(issue);

        assertThat(Instant.ofEpochMilli(value)).isEqualTo(Instant.ofEpochMilli(1547036284000L));
    }

    private Filter createFilter(long issueTypeId, long statusId, String stepName, String stageName, String laneName) {
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
        filters.add(filter);
        return filter;
    }
}
