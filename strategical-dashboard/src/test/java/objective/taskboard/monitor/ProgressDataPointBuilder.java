package objective.taskboard.monitor;

import java.time.LocalDate;

import objective.taskboard.followup.data.ProgressDataPoint;

public class ProgressDataPointBuilder {
    private LocalDate date;
    private double progress;
    private double sumEffortDone;
    private double sumEffortBacklog;

    public static ProgressDataPointBuilder progressDataPoint() {
        return new ProgressDataPointBuilder();
    }

    public ProgressDataPointBuilder date(String date) {
        this.date = LocalDate.parse(date);
        return this;
    }

    public ProgressDataPointBuilder progress(double progress) {
        this.progress = progress;
        return this;
    }

    public ProgressDataPointBuilder sumEffortDone(double sumEffortDone) {
        this.sumEffortDone = sumEffortDone;
        return this;
    }

    public ProgressDataPointBuilder sumEffortBacklog(double sumEffortBacklog) {
        this.sumEffortBacklog = sumEffortBacklog;
        return this;
    }

    public ProgressDataPoint build() {
        return new ProgressDataPoint(date, progress, sumEffortDone, sumEffortBacklog);
    }
}
