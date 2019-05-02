package objective.taskboard.controller;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.data.Team;
import objective.taskboard.domain.Lane;
import objective.taskboard.domain.Stage;
import objective.taskboard.domain.Step;
import objective.taskboard.domain.WipConfiguration;
import objective.taskboard.repository.StepRepository;
import objective.taskboard.repository.TeamCachedRepository;
import objective.taskboard.repository.WipConfigurationRepository;

@RestController
@RequestMapping("/api/wips")
public class WipController {

    private final WipConfigurationRepository wipConfigRepo;
    private final StepRepository stepRepository;
    private final TeamCachedRepository teamRepo;
    
    private final Comparator<WipConfiguration> wipConfigComparatorByOrder = comparing((WipConfiguration c) -> c.getStep().getStage().getLane().getOrdem())
            .thenComparing(c -> c.getStep().getStage().getOrdem())
            .thenComparing(c -> c.getStep().getOrdem());
    
    @Autowired
    public WipController(WipConfigurationRepository wipConfigRepo, StepRepository stepRepository, TeamCachedRepository teamRepo) {
        this.wipConfigRepo = wipConfigRepo;
        this.stepRepository = stepRepository;
        this.teamRepo = teamRepo;
    }

    @RequestMapping
    public WipConfigurationsDto getWipConfigurations() {
        List<Team> allTeams = teamRepo.getCache();
        List<WipConfiguration> configurations = wipConfigRepo.findAll();
        Set<Step> configuredSteps = configurations.stream().map(WipConfiguration::getStep).collect(toSet());
        Set<Lane> configuredLanes = configuredSteps.stream().map(s -> s.getStage().getLane()).collect(toSet());

        Map<String, List<StepWipDto>> wipsByTeam = configurations.stream()
                .sorted(wipConfigComparatorByOrder)
                .collect(groupingBy(
                        WipConfiguration::getTeam, 
                        TreeMap::new,
                        mapping(c -> new StepWipDto(c.getStep().getId(), c.getWip()), toList())));

        allTeams.stream().forEach(t -> wipsByTeam.putIfAbsent(t.getName(), emptyList()));
        
        List<LaneDto> lanesDto = configuredLanes.stream()
                .sorted(comparing(Lane::getOrdem))
                .map(lane -> {
                    List<StepDto> stepsDto = lane.getStages().stream()
                            .sorted(comparing(Stage::getOrdem))
                            .flatMap(stage -> stage.getSteps().stream())
                            .filter(configuredSteps::contains)
                            .sorted(comparing(Step::getOrdem))
                            .map(step -> new StepDto(step.getId(), step.getName()))
                            .collect(toList());

                    return new LaneDto(lane.getName(), stepsDto);
                })
                .collect(toList());

        return new WipConfigurationsDto(wipsByTeam, lanesDto);
    }

    @RequestMapping(method = RequestMethod.PUT)
    public void setWipConfigurations(@RequestBody List<WipConfigurationDto> configsDto) {
        List<WipConfiguration> existingConfigs = wipConfigRepo.findAll();
        Set<WipConfiguration> configsToDelete = new HashSet<>(existingConfigs);
        
        for (WipConfigurationDto configDto : configsDto) {
            Optional<WipConfiguration> existingConfig = existingConfigs.stream()
                    .filter(c -> c.getTeam().equals(configDto.team) && c.getStep().getId().equals(configDto.stepId))
                    .findFirst();
            
            if (existingConfig.isPresent()) {
                configsToDelete.remove(existingConfig.get());

                existingConfig.get().setWip(configDto.wip);
                wipConfigRepo.save(existingConfig.get());
            } else {
                Step step = stepRepository.getOne(configDto.stepId);
                if (step == null)
                    throw new IllegalArgumentException("Step with id <" + configDto.stepId + "> not found");

                wipConfigRepo.save(new WipConfiguration(configDto.team, step, configDto.wip));
            }
        }

        if (!configsToDelete.isEmpty())
            wipConfigRepo.delete(configsToDelete);
    }

    public static class WipConfigurationsDto {
        public final Map<String, List<StepWipDto>> wipsByTeam;
        public final List<LaneDto> lanes;

        public WipConfigurationsDto(Map<String, List<StepWipDto>> wipsByTeam, List<LaneDto> lanes) {
            this.wipsByTeam = wipsByTeam;
            this.lanes = lanes;
        }
    }
    
    public static class StepWipDto {
        public final Long stepId;
        public final Integer wip;

        public StepWipDto(Long stepId, Integer wip) {
            this.stepId = stepId;
            this.wip = wip;
        }
    }
    
    public static class LaneDto {
        public final String name;
        public final List<StepDto> steps;

        public LaneDto(String name, List<StepDto> steps) {
            this.name = name;
            this.steps = steps;
        }
    }

    public static class StepDto {
        public final Long id;
        public final String name;

        public StepDto(Long id, String name) {
            this.id = id;
            this.name = name;
        }
    }
    
    public static class WipConfigurationDto {
        public String team;
        public Long stepId;
        public Integer wip;
        
        public WipConfigurationDto() {
        }
        
        public WipConfigurationDto(String team, Long stepId, Integer wip) {
            this.team = team;
            this.stepId = stepId;
            this.wip = wip;
        }
    }
}
