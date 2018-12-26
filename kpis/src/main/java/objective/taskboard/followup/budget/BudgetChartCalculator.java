package objective.taskboard.followup.budget;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.data.FollowupProgressCalculator;
import objective.taskboard.followup.data.ProgressData;
import objective.taskboard.project.config.changeRequest.ChangeRequest;
import objective.taskboard.project.config.changeRequest.ChangeRequestService;

@Component
public class BudgetChartCalculator {

    private FollowupProgressCalculator calculator;
    private ChangeRequestService changeRequestService;

    @Autowired
    public BudgetChartCalculator(FollowupProgressCalculator calculator, ChangeRequestService changeRequestService) {
        this.calculator = calculator;
        this.changeRequestService = changeRequestService;
    }

    public BudgetChartData calculate(ZoneId systemDefault, ProjectFilterConfiguration project) {
        ProgressData progressData = calculator.calculateWithCompleteProjection(
                systemDefault, project.getProjectKey(), project.getProjectionTimespan());

        BudgetChartData budgetChartData = new BudgetChartData(); 

        addBudget(project, progressData, budgetChartData);

        addScopeDone(progressData, budgetChartData);

        addScopeTotal(project, progressData, budgetChartData);

        addScopeDoneProjection(progressData, budgetChartData);

        addScopeTotalProjection(project, progressData, budgetChartData);

        calculateProjectionDate(budgetChartData);

        budgetChartData.startingDate = progressData.startingDate;
        budgetChartData.endingDate = progressData.endingDate;

        return budgetChartData;
    }

    private void calculateProjectionDate(BudgetChartData budgetChartData) {
        if (isProjectionDateAvailable(budgetChartData) ) {
            LocalDate lastScopeDoneDay = budgetChartData.scopeDone.get(budgetChartData.scopeDone.size() - 1).date;
            budgetChartData.projectionDate = lastScopeDoneDay.plusDays(budgetChartData.scopeDoneProjection.size());
        }
    }

    private boolean isProjectionDateAvailable(BudgetChartData budgetChartData) {
        if ( budgetChartData.scopeDoneProjection.size() <= 1) 
            return false;

        BudgetChartDataPoint bcdFirst =
            budgetChartData.scopeDoneProjection.get(0);
        BudgetChartDataPoint bcdLast =
            budgetChartData.scopeDoneProjection.get(budgetChartData.scopeDoneProjection.size() - 1);
        return bcdFirst.value < bcdLast.value;
    }

    private void addScopeTotalProjection(
            ProjectFilterConfiguration project, 
            ProgressData progressData,
            BudgetChartData budgetChartData) 
    {
        budgetChartData.scopeTotalProjection = progressData.actualProjection.stream()
            .map( point -> {
                double scopeTotalProjection = 
                        (point.sumEffortBacklog + point.sumEffortDone) * (1 + ((project.getRiskPercentage().doubleValue()) / 100));
                return new BudgetChartDataPoint(point.date, scopeTotalProjection);
            }).collect(Collectors.toList());
    }

    private void addBudget(
            ProjectFilterConfiguration project, 
            ProgressData progressData,
            BudgetChartData budgetChartData) 
    {
        List<ChangeRequest> changeRequests = changeRequestService.listByProject(project);
        Collections.reverse(changeRequests);
        List<BudgetChartDataPoint> budget = new ArrayList<>();

        int crIndex = 0;
        int budgetSum = 0;
        LocalDate currentDate = progressData.startingDate;
        LocalDate endDate = progressData.endingDate;

        while (!currentDate.isAfter(endDate)) {
            if (isNextChangeRequestAppliable(changeRequests, crIndex, currentDate)) {
                budgetSum += changeRequests.get(crIndex).getBudgetIncrease();
                crIndex++;
            }

            budget.add(new BudgetChartDataPoint(currentDate, budgetSum));

            currentDate = currentDate.plusDays(1l);
        }

        budgetChartData.budget = budget;
    }

    private boolean isNextChangeRequestAppliable(List<ChangeRequest> changeRequests, int dateIndex, LocalDate currentDate) {
        return changeRequests.size() > dateIndex && changeRequests.get(dateIndex).getDate().equals(currentDate);
    }

    private void addScopeDoneProjection(ProgressData progressData, BudgetChartData budgetChartData) {
        budgetChartData.scopeDoneProjection = progressData.actualProjection.stream()
            .map( point -> {
                    return new BudgetChartDataPoint(point.date, point.sumEffortDone);
            }).collect(Collectors.toList());
    }

    private void addScopeTotal(
            ProjectFilterConfiguration project, 
            ProgressData progressData,
            BudgetChartData budgetChartData) 
    {
            
        budgetChartData.scopeTotal = progressData.actual.stream()
            .map( point -> {
                double scopeTotal = 
                        (point.sumEffortBacklog + point.sumEffortDone) * (1 + ((project.getRiskPercentage().doubleValue()) / 100));
                return new BudgetChartDataPoint(point.date, scopeTotal);
            }).collect(Collectors.toList());
    }

    private void addScopeDone(ProgressData progressData, BudgetChartData budgetChartData) {
        budgetChartData.scopeDone = progressData.actual.stream()
            .map( point -> {
                    return new BudgetChartDataPoint(point.date, point.sumEffortDone);
            }).collect(Collectors.toList());
    }

}
