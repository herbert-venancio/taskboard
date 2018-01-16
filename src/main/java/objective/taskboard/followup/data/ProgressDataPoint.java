package objective.taskboard.followup.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import objective.taskboard.utils.NumberUtils;

import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProgressDataPoint {
    public final LocalDate date;
    public final double progress;
    @JsonSerialize(using = NumberUtils.FormattedNumberSerializer.class)
    public final Double sumEffortDone;
    @JsonSerialize(using = NumberUtils.FormattedNumberSerializer.class)
    public final Double sumEffortBacklog;

    public ProgressDataPoint(LocalDate date, double progress) {
        this(date, progress, null, null);
    }

    public ProgressDataPoint(LocalDate date, double progress, Double sumEffortDone, Double sumEffortBacklog) {
        this.date = date;
        this.progress = progress;
        this.sumEffortDone = sumEffortDone;
        this.sumEffortBacklog = sumEffortBacklog;
    }
    
    @Override
    public String toString() {
        return date + " / " + progress;
    }
}