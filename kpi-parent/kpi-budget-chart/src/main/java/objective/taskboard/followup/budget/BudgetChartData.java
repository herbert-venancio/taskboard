package objective.taskboard.followup.budget;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

public class BudgetChartData {
    
    public List<BudgetChartDataPoint> budget = new LinkedList<>();
    public List<BudgetChartDataPoint> scopeDone = new LinkedList<>();
    public List<BudgetChartDataPoint> scopeDoneProjection = new LinkedList<>();
    public List<BudgetChartDataPoint> scopeTotal = new LinkedList<>();
    public List<BudgetChartDataPoint> scopeTotalProjection = new LinkedList<>();
    public LocalDate startingDate;
    public LocalDate endingDate;
    public LocalDate projectionDate;

}