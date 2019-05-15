package objective.taskboard.monitor;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import objective.taskboard.config.CacheConfiguration;
import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.ProjectDatesNotConfiguredException;
import objective.taskboard.followup.budget.BudgetChartCalculator;
import objective.taskboard.followup.budget.BudgetChartData;
import objective.taskboard.followup.cluster.ClusterNotConfiguredException;
import objective.taskboard.monitor.StrategicalProjectDataSet.MonitorData;
import objective.taskboard.monitor.StrategicalProjectDataSet.Status;

@Component
class TimelineMonitorCalculator implements MonitorCalculator  {
    private static final Logger log = LoggerFactory.getLogger(ScopeMonitorCalculator.class);
    public static final String CANT_CALCULATE_TIMELINE_WARNING = "Can't calculate Timeline warning: Project hasn't risk percentage configured.";
    public static final String CANT_CALCULATE_TIMELINE_UNEXPECTED = "Can't calculate Timeline: Unexpected error.";

    private final BudgetChartCalculator budgetChartCalculator;

    @Autowired
    public TimelineMonitorCalculator(BudgetChartCalculator budgetChartCalculator) {
        this.budgetChartCalculator = budgetChartCalculator;
    }

    @Override
    @Cacheable(value = CacheConfiguration.STRATEGICAL_DASHBOARD, key="{'timeline', #project.getProjectKey(), #timezone}")
    public MonitorData calculate(ProjectFilterConfiguration project, ZoneId timezone) {
        MonitorDataBuilder monitorTimeline = MonitorData.builder()
                .withLabel("Timeline Forecast")
                .withIcon("#icon-notebook")
                .withStatusDetails("Gray is displayed when there is not enough data to measure project progress.");

        monitorTimeline
            .expectedDetails("Project end date that is beforehand set up on the Taskboard tool.")
            .warningDetails("Range of dates which begins with the expected end date and ends up with the sum of expected end date plus risk.")
            .actualDetails("Actual end date, according to the current scope progress.");

        try {
            monitorTimeline.statusDetails = "Gray is displayed when there is not enough data to measure project progress.";
            BudgetChartData budgetChartData = budgetChartCalculator.calculate(timezone, project);

            DateTimeFormatter customFormat = DateTimeFormatter.ofPattern("MMMd/YYYY");

            LocalDate startDate = project.getStartDate().orElseThrow(() -> new IllegalStateException());
            LocalDate deliveryDate = project.getDeliveryDate().orElseThrow(() -> new IllegalStateException());

            String formattedDeliveryDate = deliveryDate.format(customFormat);

            LocalDate limitDeliveryDate = getLimitDeliveryDate(project, startDate, deliveryDate);
            String formattedLimitDeliveryDate = limitDeliveryDate.format(customFormat);
            String formattedProjectionDate;

            LocalDate projectionDate = budgetChartData.projectionDate;

            if (projectionDate == null) {
                monitorTimeline.actualDetails("Can't calculate Timeline actual: Budget hasn't Projection Date.");
                formattedProjectionDate = CANT_CALCULATE_MESSAGE;
            } else {
                formattedProjectionDate = projectionDate.format(customFormat);
            }

            if (deliveryDate.isEqual(limitDeliveryDate)) {
                monitorTimeline.warningValue(CANT_CALCULATE_MESSAGE);
                monitorTimeline.warningDetails(CANT_CALCULATE_TIMELINE_WARNING);
            } else {
                monitorTimeline.warningValue(formattedDeliveryDate + " - " + formattedLimitDeliveryDate);
            }

            Status statusEnum = verifyTimelineStatus(deliveryDate, limitDeliveryDate, projectionDate);

            monitorTimeline.withStatusDetails(verifyTimelineStatusDetails(statusEnum));
            monitorTimeline.expectedValue(formattedDeliveryDate);
            monitorTimeline.actualValue(formattedProjectionDate);
            monitorTimeline.withStatus(statusEnum.status());
        } catch (ClusterNotConfiguredException | ProjectDatesNotConfiguredException e) {
            log.error(e.getMessage(), e);
            return monitorTimeline.withError("Can't calculate Timeline: " +  e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return monitorTimeline.withError(CANT_CALCULATE_TIMELINE_UNEXPECTED);
        }

        return monitorTimeline.build();
    }

    private static Status verifyTimelineStatus(LocalDate deliveryDate, LocalDate limitDeliveryDate, LocalDate projectionDate) {

        boolean projectionDateExists = projectionDate != null;

        if (projectionDateExists && (projectionDate.isBefore(deliveryDate) || projectionDate.isEqual(deliveryDate)))
            return StrategicalProjectDataSet.Status.NORMAL;
        else if (projectionDateExists && projectionDate.isBefore(limitDeliveryDate))
            return StrategicalProjectDataSet.Status.ALERT;

        return StrategicalProjectDataSet.Status.DANGER;
    }

    private static String verifyTimelineStatusDetails(Status status) {
        switch(status) {
            case ALERT:
                return "The actual end date is within the warning range.";
            case DANGER:
                return "The end date is older than the maximum date of the warning range.";
            default:
                return "The actual end date is earlier than the beginning date of the warning range.";
        }
    }

    private static LocalDate getLimitDeliveryDate(ProjectFilterConfiguration project, LocalDate startDate, LocalDate deliveryDate) {
        int diffBetweenDates = Period.between(startDate, deliveryDate).getDays();
        int daysOfRisk = project.getRiskPercentage() != null ? (int) (diffBetweenDates * project.getRiskPercentage().doubleValue()) : 0;

        return deliveryDate.plusDays(daysOfRisk);
    }

}
