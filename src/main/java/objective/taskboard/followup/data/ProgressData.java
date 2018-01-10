package objective.taskboard.followup.data;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class ProgressData {
    public List<ProgressDataPoint> actual             = new LinkedList<>();
    public List<ProgressDataPoint> expected           = new LinkedList<>();
    public List<ProgressDataPoint> actualProjection   = new LinkedList<>();
    public Date startingDate;
    public Date endingDate;
}