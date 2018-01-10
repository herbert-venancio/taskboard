package objective.taskboard.followup.data;

import java.util.Date;

public class ProgressDataPoint {
    public Date date;
    public double progress;
    
    public ProgressDataPoint(){}
    
    public ProgressDataPoint(Date date, double progress) {
        this.date = date;
        this.progress = progress;
    }
    
    @Override
    public String toString() {
        return date + " / " + progress;
    }
}