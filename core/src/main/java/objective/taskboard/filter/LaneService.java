package objective.taskboard.filter;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import objective.taskboard.config.CacheConfiguration;
import objective.taskboard.data.IssuesConfiguration;
import objective.taskboard.data.LaneConfiguration;
import objective.taskboard.data.StepConfiguration;
import objective.taskboard.database.TaskboardConfigToLaneConfigurationTransformer;
import objective.taskboard.domain.Lane;
import objective.taskboard.repository.LaneRepository;

@Service
public class LaneService {

    @Autowired
    private LaneRepository laneRepository;

    @Autowired
    public LaneService(LaneRepository laneRepository) {
        this.laneRepository = laneRepository;
    }

    @Cacheable(value = CacheConfiguration.CONFIGURATION, key = "'lane'")
    public List<LaneConfiguration> getLanes() {
        List<Lane> lanes = laneRepository.findAll();
        return TaskboardConfigToLaneConfigurationTransformer.transform(lanes);
    }

    @Cacheable(value = CacheConfiguration.CONFIGURATION, key = "'filter'")
    public List<IssuesConfiguration> getFilters() {
        return filterStream()
                .collect(toList());
    }

    private Stream<StepConfiguration> stepStream() {
        return getLanes().stream()
                .flatMap(lane -> lane.getStages().stream())
                .flatMap(stage -> stage.getSteps().stream());
    }

    private Stream<IssuesConfiguration> filterStream() {
        return stepStream()
                .flatMap(step -> step.getIssuesConfiguration().stream());
    }
}
