package objective.taskboard.followup.data;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

public class ProgressData {
    public List<ProgressDataPoint> actual             = new LinkedList<>();
    public List<ProgressDataPoint> expected           = new LinkedList<>();
    public List<ProgressDataPoint> actualProjection   = new LinkedList<>();
    public LocalDate startingDate;
    public LocalDate endingDate;
    public Integer projectionTimespan;
}