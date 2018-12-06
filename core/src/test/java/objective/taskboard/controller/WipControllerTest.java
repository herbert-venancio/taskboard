package objective.taskboard.controller;

import static java.util.Arrays.asList;
import static objective.taskboard.testUtils.AssertUtils.collectionToString;
import static org.apache.commons.lang3.StringUtils.join;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.Test;

import objective.taskboard.controller.WipController.LaneDto;
import objective.taskboard.controller.WipController.StepDto;
import objective.taskboard.controller.WipController.StepWipDto;
import objective.taskboard.controller.WipController.WipConfigurationDto;
import objective.taskboard.controller.WipController.WipConfigurationsDto;
import objective.taskboard.data.Team;
import objective.taskboard.domain.Lane;
import objective.taskboard.domain.Stage;
import objective.taskboard.domain.Step;
import objective.taskboard.domain.WipConfiguration;
import objective.taskboard.repository.LaneCachedRepository;
import objective.taskboard.repository.TeamCachedRepository;
import objective.taskboard.repository.WipConfigurationRepository;
import objective.taskboard.testUtils.JpaRepositoryMock;

public class WipControllerTest {
    
    private final WipConfigurationRepository configRepo = new WipConfigurationRepositoryMock();
    private final LaneCachedRepository laneRepo = mock(LaneCachedRepository.class);
    private final TeamCachedRepository teamRepo = mock(TeamCachedRepository.class);
    private final WipController subject = new WipController(configRepo, laneRepo, teamRepo);

    private final Lane feature = lane(1, "Feature", 0);
    private final Lane stories = lane(2, "Stories", 1);
    
    private final Step featureTodo  = step(11, "Todo",  0, feature);
    private final Step featureDoing = step(12, "Doing", 1, feature);
    private final Step featureUat   = step(13, "UAT",   2, feature);
    private final Step storyTodo    = step(21, "Todo",  0, stories);
    private final Step storyDev     = step(22, "Dev",   1, stories);
    
    @Test
    public void getWipConfigurationsTest() {
        when(teamRepo.getCache()).thenReturn(asList(
                team("Brundle"), 
                team("Compace"), 
                team("Devcare"), 
                team("Zookeep")));
        
        configRepo.save(asList(
                new WipConfiguration("Brundle", storyTodo,   20),
                new WipConfiguration("Brundle", storyDev,    10),
                new WipConfiguration("Brundle", featureTodo,  8),
                new WipConfiguration("Brundle", featureUat,   2),
                new WipConfiguration("Brundle", featureDoing, 4),
                
                new WipConfiguration("Compace", featureTodo,  7),
                new WipConfiguration("Compace", featureDoing, 3),
                new WipConfiguration("Compace", featureUat,   5),
                new WipConfiguration("Compace", storyDev,    15),
                
                new WipConfiguration("Zookeep", featureDoing, 2),
                new WipConfiguration("Zookeep", featureUat,   4)));
        
        WipConfigurationsDto configurations = subject.getWipConfigurations();
        
        assertLanes(configurations.lanes, 
                "Feature (steps: Todo <11>, Doing <12>, UAT <13>)",
                "Stories (steps: Todo <21>, Dev <22>)");
        
        assertWipsByTeam(configurations.wipsByTeam, 
                "[Brundle] step 11: 8, step 12: 4, step 13: 2, step 21: 20, step 22: 10",
                "[Compace] step 11: 7, step 12: 3, step 13: 5, step 22: 15",
                "[Devcare] <empty>",
                "[Zookeep] step 12: 2, step 13: 4");
    }
    
    @Test
    public void setWipConfigurations() {
        when(laneRepo.getAllSteps()).thenReturn(asList(featureTodo, featureDoing, featureUat));
        
        configRepo.save(asList(
                new WipConfiguration("Brundle", featureTodo, 99),
                new WipConfiguration("Devcare", featureUat,   4)));

        subject.setWipConfigurations(asList(
                new WipConfigurationDto("Brundle", 11L, 8),
                new WipConfigurationDto("Brundle", 12L, 4),
                new WipConfigurationDto("Brundle", 13L, 2),
                new WipConfigurationDto("Compace", 11L, 7),
                new WipConfigurationDto("Compace", 12L, 3)));

        assertConfigurations(configRepo.findAll(), 
                "Brundle | step: 11 | wip: 8",
                "Brundle | step: 12 | wip: 4",
                "Brundle | step: 13 | wip: 2",
                "Compace | step: 11 | wip: 7",
                "Compace | step: 12 | wip: 3");
    }

    private static void assertWipsByTeam(Map<String, List<StepWipDto>> actual, String... expected) {
        Function<StepWipDto, String> wipToString = w -> "step " + w.stepId + ": " + w.wip;
        Function<String, String> teamToString = t -> "[" + t + "] " + collectionToString(actual.get(t), wipToString, ", ");
        assertEquals(join(expected, "\n"), collectionToString(actual.keySet(), teamToString, "\n"));
    }
    
    private static void assertLanes(List<LaneDto> actual, String... expected) {
        Function<StepDto, String> stepToString = s -> s.name + " <" + s.id + ">";
        Function<LaneDto, String> laneToString = l -> l.name + " (steps: " + collectionToString(l.steps, stepToString, ", ") + ")";
        assertEquals(join(expected, "\n"), collectionToString(actual, laneToString, "\n"));
    }
    
    private static void assertConfigurations(List<WipConfiguration> actual, String... expected) {
        assertEquals(join(expected, "\n"), collectionToString(actual, 
                c -> c.getTeam() + " | step: " + (c.getStep() == null ? "<null>" : c.getStep().getId()) + " | wip: " + c.getWip(), "\n"));
    }

    private static Step step(int id, String name, int order, Lane lane) {
        Stage stage = new Stage();
        stage.setId((long) (id + 100));
        stage.setName(name);
        stage.setLane(lane);
        stage.setOrdem(order);
        stage.setSteps(new ArrayList<>());
        
        Step step = new Step();
        step.setId((long) id);
        step.setName(name);
        step.setStage(stage);
        step.setOrdem(1);
        
        stage.getSteps().add(step);
        lane.getStages().add(stage);
        
        return step;
    }
    
    private static Lane lane(int id, String name, int order) {
        Lane lane = new Lane();
        lane.setId((long) id);
        lane.setName(name);
        lane.setOrdem(order);
        lane.setStages(new ArrayList<>());
        
        return lane;
    }
    
    private static Team team(String name) {
        return new Team(name, "Joseph", "Joseph", Collections.emptyList());
    }

    private static class WipConfigurationRepositoryMock extends JpaRepositoryMock<WipConfiguration> implements WipConfigurationRepository {
        @Override
        public List<WipConfiguration> findByTeamIn(Collection<String> teamNames) {
            throw new UnsupportedOperationException();
        }
    }
}
