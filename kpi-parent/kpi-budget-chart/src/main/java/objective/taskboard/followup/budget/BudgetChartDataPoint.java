package objective.taskboard.followup.budget;
import java.time.LocalDate;

public class BudgetChartDataPoint {
    public final LocalDate date;
    public final double value;

    public BudgetChartDataPoint(LocalDate date, double value) {
        this.date = date;
        this.value = value;
    }

    @Override
    public String toString() {
        return date + " / " + value;
    }
}