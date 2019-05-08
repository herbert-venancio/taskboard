package objective.taskboard.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import objective.taskboard.data.IssuesConfiguration;
import objective.taskboard.data.LaneConfiguration;
import objective.taskboard.data.RuleConfiguration;
import objective.taskboard.data.StageConfiguration;
import objective.taskboard.data.StepConfiguration;
import objective.taskboard.domain.Filter;
import objective.taskboard.domain.Lane;
import objective.taskboard.domain.Rule;
import objective.taskboard.domain.Stage;
import objective.taskboard.domain.Step;

public class TaskboardConfigToLaneConfigurationTransformer {

    private TaskboardConfigToLaneConfigurationTransformer() { }

    public static List<LaneConfiguration> transform(List<Lane> lanes) {

        List<LaneConfiguration> lanesConfiguration = new ArrayList<>();

        ArrayList<Lane> sortedLanes = new ArrayList<>(lanes);
        sortLanes(sortedLanes);

        for (Lane lane : sortedLanes) {
            final LaneConfiguration laneConfiguration = getLaneConfiguration(lanesConfiguration, lane);

            for (Rule rule : lane.getRules())
                ruleConfiguration(laneConfiguration, rule);

            List<Stage> stages = lane.getStages();
            sortStages(stages);
            for (Stage stage : stages) {
                final StageConfiguration stageConfiguration = getStageConfiguration(laneConfiguration, stage);

                List<Step> steps = stage.getSteps();
                sortSteps(steps);
                for (Step step : steps) {
                    step.setColor(stage.getColor());
                    final StepConfiguration stepConfiguration = getStepConfiguration(stageConfiguration, step);

                    step.getFilters().stream().forEach(filter->issuesConfiguration(stepConfiguration, filter));
                }
            }
        }

        return lanesConfiguration;
    }

    private static void sortLanes(List<Lane> lanes) {
        final Comparator<Lane> comparator = new Comparator<Lane>() {

            @Override
            public int compare(Lane o1, Lane o2) {
                try {
                    Integer ordem = o2.getOrdem();
                    Integer ordem2 = o1.getOrdem();
                    return ordem2.compareTo(ordem);
                } catch (Exception e) {//NOSONAR
                    return 0;
                }

            }
        };
        Collections.sort(lanes, comparator);
    }

    private static void sortStages(List<Stage> stages) {
        final Comparator<Stage> comparator = new Comparator<Stage>() {

            @Override
            public int compare(Stage o1, Stage o2) {
                return o1.getOrdem().compareTo(o2.getOrdem());
            }
        };
        Collections.sort(stages, comparator);
    }

    private static void sortSteps(List<Step> steps) {
        final Comparator<Step> comparator = new Comparator<Step>() {

            @Override
            public int compare(Step o1, Step o2) {
                return o1.getOrdem().compareTo(o2.getOrdem());
            }
        };
        Collections.sort(steps, comparator);
    }

    private static LaneConfiguration getLaneConfiguration(List<LaneConfiguration> lanesConfiguration, Lane lane) {
        Optional<LaneConfiguration> laneConfOptional = lanesConfiguration.stream().filter(stg -> stg.getLevel().equals(lane.getName())).findFirst();

        LaneConfiguration laneConfiguration = null;

        if (laneConfOptional.isPresent()) {
            laneConfiguration = laneConfOptional.get();
        } else {
            laneConfiguration = LaneConfiguration.from(lane);
            lanesConfiguration.add(laneConfiguration);
        }

        return laneConfiguration;
    }

    private static RuleConfiguration ruleConfiguration(LaneConfiguration lane, Rule rule) {

        Optional<RuleConfiguration> ruleConfOptional = lane.getRules().stream().filter(r -> r.getChave().equals(rule.getChave())).findFirst();

        RuleConfiguration ruleConfiguration = null;

        if (ruleConfOptional.isPresent()) {
            ruleConfiguration = ruleConfOptional.get();
        } else {
            ruleConfiguration = RuleConfiguration.from(rule);
            lane.addRuleConfiguration(ruleConfiguration);
        }

        return ruleConfiguration;
    }

    private static StageConfiguration getStageConfiguration(LaneConfiguration lane, Stage stage) {

        Optional<StageConfiguration> stageConfOptional = lane.getStages().stream().filter(stg -> stg.getStage().equals(stage.getName())).findFirst();

        StageConfiguration stageConfiguration = null;

        if (stageConfOptional.isPresent()) {
            stageConfiguration = stageConfOptional.get();
        } else {
            stageConfiguration = StageConfiguration.from(stage);
            lane.addStageConfiguration(stageConfiguration);
        }

        return stageConfiguration;
    }

    private static StepConfiguration getStepConfiguration(StageConfiguration stageConfiguration, Step step) {
        Optional<StepConfiguration> stepConfOptional = stageConfiguration.getSteps().stream().filter(stp -> stp.getStep().equals(step.getName())).findFirst();

        StepConfiguration stepConfiguration = null;

        if (stepConfOptional.isPresent()) {
            stepConfiguration = stepConfOptional.get();
        } else {
            stepConfiguration = StepConfiguration.from(step);
            stageConfiguration.addStepConfiguration(stepConfiguration);
        }

        return stepConfiguration;
    }

    private static void issuesConfiguration(StepConfiguration stepConfiguration, Filter filter) {
        if (filter.getIssueTypeId() != 0L && filter.getStatusId() != 0L)
            stepConfiguration.addIssueConfiguration(IssuesConfiguration.fromFilter(filter));
    }

}
