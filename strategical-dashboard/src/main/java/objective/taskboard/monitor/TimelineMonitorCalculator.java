package objective.taskboard.monitor;

import static java.util.Arrays.asList;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.ProjectDatesNotConfiguredException;
import objective.taskboard.followup.budget.BudgetChartData;
import objective.taskboard.followup.cluster.ClusterNotConfiguredException;
import objective.taskboard.monitor.StrategicalProjectDataSet.DataItem;
import objective.taskboard.monitor.StrategicalProjectDataSet.MonitorData;

@Component
class TimelineMonitorCalculator implements MonitorCalculator  {
    private static final Logger log = LoggerFactory.getLogger(ScopeMonitorCalculator.class);
    public static final String CANT_CALCULATE_TIMELINE_WARNING = "Can't calculate Timeline warning: Project hasn't risk percentage configured.";
    public static final String CANT_CALCULATE_TIMELINE_UNEXPECTED = "Can't calculate Timeline: Unexpected error.";

    private final MonitorDataService monitorDataService;

    @Autowired
    public TimelineMonitorCalculator(MonitorDataService monitorDataService) {
        this.monitorDataService = monitorDataService;
    }

    @Override
    public MonitorData calculate(ProjectFilterConfiguration project, ZoneId timezone) {
        MonitorData monitorTimeline = new MonitorData("Timeline Forecast", "#icon-notebook");

        DataItem expectedDataItem = new DataItem("(expected)", "Project end date that is beforehand set up on the Taskboard tool.");
        DataItem warningDataItem = new DataItem("(warning)", "Range of dates which begins with the expected end date and ends up with the sum of expected end date plus risk.");
        DataItem actualDataItem = new DataItem("(actual)", "Actual end date, according to the current scope progress.");

        try {
            monitorTimeline.statusDetails = "Gray is displayed when there is not enough data to measure project progress.";
            BudgetChartData budgetChartData = monitorDataService.getBudgetChartData(project, timezone);

            DateTimeFormatter customFormat = DateTimeFormatter.ofPattern("MMMd/YYYY");

            LocalDate startDate = project.getStartDate().orElseThrow(() -> new IllegalStateException());
            LocalDate deliveryDate = project.getDeliveryDate().orElseThrow(() -> new IllegalStateException());

            String formattedDeliveryDate = deliveryDate.format(customFormat);

            LocalDate limitDeliveryDate = getLimitDeliveryDate(project, startDate, deliveryDate);
            String formattedLimitDeliveryDate = limitDeliveryDate.format(customFormat);
            String formattedProjectionDate;

            LocalDate projectionDate = budgetChartData.projectionDate;

            if (projectionDate == null) {
                actualDataItem.details = "Can't calculate Timeline actual: Budget hasn't Projection Date.";
                formattedProjectionDate = CANT_CALCULATE_MESSAGE;
            } else {
                formattedProjectionDate = projectionDate.format(customFormat);
            }

            if (deliveryDate.isEqual(limitDeliveryDate)) {
                warningDataItem.text = CANT_CALCULATE_MESSAGE;
                warningDataItem.details = "Can't calculate Timeline warning: Project hasn't risk percentage configured.";
            } else {
                warningDataItem.text = formattedDeliveryDate + " - " + formattedLimitDeliveryDate;
            }

            String status = verifyTimelineStatus(deliveryDate, limitDeliveryDate, projectionDate);
            monitorTimeline.statusDetails = verifyTimelineStatusDetails(status);

            expectedDataItem.text = formattedDeliveryDate;

            actualDataItem.text = formattedProjectionDate;
            monitorTimeline.status = status;

            monitorTimeline.items = asList(expectedDataItem, warningDataItem, actualDataItem);
        } catch (ClusterNotConfiguredException | ProjectDatesNotConfiguredException e) {
            log.error(e.getMessage(), e);

            monitorTimeline.items = asList(expectedDataItem, warningDataItem, actualDataItem);
            monitorTimeline = MonitorData.withError(monitorTimeline, "Can't calculate Timeline: " +  e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage(), e);

            monitorTimeline.items = asList(expectedDataItem, warningDataItem, actualDataItem);
            monitorTimeline = MonitorData.withError(monitorTimeline, "Can't calculate Timeline: Unexpected error.");
        }

        return monitorTimeline;
    }

    private static String verifyTimelineStatus(LocalDate deliveryDate, LocalDate limitDeliveryDate, LocalDate projectionDate) {

        boolean projectionDateExists = projectionDate != null;

        if (projectionDateExists && (projectionDate.isBefore(deliveryDate) || projectionDate.isEqual(deliveryDate)))
            return "normal";
        else if (projectionDateExists && projectionDate.isBefore(limitDeliveryDate))
            return "alert";

        return "danger";
    }

    private static String verifyTimelineStatusDetails(String status) {
        String statusDetails = "The actual end date is earlier than the beginning date of the warning range.";
        
        if (status.equals("alert")) {
            statusDetails = "The actual end date is within the warning range.";
        } else if (status.equals("danger")) {
            statusDetails = "The end date is older than the maximum date of the warning range.";
        }

        return statusDetails;
    }

    private static LocalDate getLimitDeliveryDate(ProjectFilterConfiguration project, LocalDate startDate, LocalDate deliveryDate) {
        int diffBetweenDates = Period.between(startDate, deliveryDate).getDays();
        int daysOfRisk = project.getRiskPercentage() != null ? (int) (diffBetweenDates * project.getRiskPercentage().doubleValue()) : 0;

        LocalDate limitDeliveryDate = deliveryDate.plusDays(daysOfRisk);
        return limitDeliveryDate;
    }

}
