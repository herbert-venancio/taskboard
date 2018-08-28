package objective.taskboard.followup.kpi;

import java.util.List;

public abstract class ChartDataSet<R>{

    public final List<R> rows;
    
    public ChartDataSet(List<R> rows) {
        this.rows = rows;
    }


}
