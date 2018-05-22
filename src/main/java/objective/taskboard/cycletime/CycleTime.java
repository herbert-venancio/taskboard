package objective.taskboard.cycletime;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import objective.taskboard.data.Holiday;
import objective.taskboard.jira.JiraProperties;
import objective.taskboard.utils.Clock;

@Component
public class CycleTime {

    private CycleTimeProperties cycleTimeProperties;
    private HolidayService holidayService;
    private Clock clock;
    private JiraProperties jiraProperties;

    @Autowired
    public CycleTime(CycleTimeProperties cycleTimeProperties, HolidayService holidayService, Clock clock, JiraProperties jiraProperties) {
        this.cycleTimeProperties = cycleTimeProperties;
        this.holidayService = holidayService;
        this.clock = clock;
        this.jiraProperties = jiraProperties;
    }

    public Optional<Double> getCycleTime(Instant startDateInstant, ZoneId timezone, Long status) {

        if (!isIssueOnCalculableStatus(status))
            return Optional.empty();

        final Instant instantToCalc = clock.now();
        final ZonedDateTime startDate = ZonedDateTime.ofInstant(startDateInstant, timezone);
        final ZonedDateTime endDate = ZonedDateTime.ofInstant(instantToCalc, timezone);

        final ZonedDateTime startWorkDay = getStartWorkDay(startDate);
        final ZonedDateTime endWorkDay = getEndWorkDay(endDate);

        if (endWorkDay.isBefore(startWorkDay))
            return Optional.ofNullable(0D);

        if (isSameDay(startWorkDay, endWorkDay)) {
            Double workedDays = toWorkedDays(
                    endWorkDay.toInstant().toEpochMilli() - startWorkDay.toInstant().toEpochMilli(),
                    instantToCalc,
                    timezone);
            
            return Optional.ofNullable(workedDays);
        }

        final long millisecondsWorkedStartDate = getEndBusinessHours(startWorkDay).toInstant().toEpochMilli() - startWorkDay.toInstant().toEpochMilli();
        final long millisecondsWorkedEndDate = endWorkDay.toInstant().toEpochMilli() - getStartBusinessHours(endWorkDay).toInstant().toEpochMilli();
        long millisecondsWorked = millisecondsWorkedStartDate + millisecondsWorkedEndDate;

        ZonedDateTime currentDate = startWorkDay.plusDays(1);
        while (!isSameDay(currentDate, endWorkDay)) {
            if (isWorkDay(currentDate))
                millisecondsWorked += getOneWorkedDayInMillis(instantToCalc, timezone);

            currentDate = currentDate.plusDays(1);
        }

        Double workedDays = toWorkedDays(millisecondsWorked, instantToCalc, timezone);
        
        return Optional.ofNullable(workedDays);
    }
    
    private ZonedDateTime getStartWorkDay(ZonedDateTime startDate) {
        final ZonedDateTime startBusinessHours = getStartBusinessHours(startDate);
        if (isWorkDay(startDate))
            return startDate.isBefore(startBusinessHours) ? startBusinessHours : startDate;

        ZonedDateTime currentDate = startBusinessHours.plusDays(1);
        while (!isWorkDay(currentDate))
            currentDate = currentDate.plusDays(1);

        return currentDate;
    }

    private ZonedDateTime getEndWorkDay(ZonedDateTime endDate) {
        final ZonedDateTime endBusinessHours = getEndBusinessHours(endDate);
        if (isWorkDay(endDate))
            return endDate.isAfter(endBusinessHours) ? endBusinessHours : endDate;

        ZonedDateTime currentDate = endBusinessHours.minusDays(1);
        while (!isWorkDay(currentDate))
            currentDate = currentDate.minusDays(1);

        return currentDate;
    }

    private ZonedDateTime getStartBusinessHours(ZonedDateTime startDate) {
        final int hour = getHourPeriod(cycleTimeProperties.getStartBusinessHours().getHour(), cycleTimeProperties.getStartBusinessHours().getPeriod());
        return ZonedDateTime.of(startDate.getYear(), startDate.getMonthValue(), startDate.getDayOfMonth(),
                hour, cycleTimeProperties.getStartBusinessHours().getMinute(), 0, 0, startDate.getZone());
    }

    private ZonedDateTime getEndBusinessHours(ZonedDateTime endDate) {
        int hour = getHourPeriod(cycleTimeProperties.getEndBusinessHours().getHour(), cycleTimeProperties.getEndBusinessHours().getPeriod());
        return ZonedDateTime.of(endDate.getYear(), endDate.getMonthValue(), endDate.getDayOfMonth(),
                hour, cycleTimeProperties.getStartBusinessHours().getMinute(), 0, 0, endDate.getZone());
    }

    private int getHourPeriod(Integer hour, String period) {
        return "pm".equals(period) ? hour + 12 : hour;
    }

    private boolean isSameDay(ZonedDateTime startDate, ZonedDateTime endDate) {
        return startDate.getYear() == endDate.getYear() &&
                startDate.getDayOfYear() == endDate.getDayOfYear();
    }

    private boolean isWorkDay(ZonedDateTime date) {
        if (date.getDayOfWeek().equals(DayOfWeek.SUNDAY) || date.getDayOfWeek().equals(DayOfWeek.SATURDAY))
            return false;

        for (Holiday holyday : holidayService.getHolidays()) {
            final ZonedDateTime holydayCalendar = ZonedDateTime.ofInstant(holyday.getDay().toInstant(), date.getZone());
            if (isSameDay(date, holydayCalendar))
                return false;
        }

        return true;
    }

    private long getOneWorkedDayInMillis(Instant instant, ZoneId zoneId) {
        final ZonedDateTime dayToCalc = ZonedDateTime.ofInstant(instant, zoneId);
        final ZonedDateTime endBusinessHour = getEndBusinessHours(dayToCalc);
        final ZonedDateTime startBusinessHour = getStartBusinessHours(dayToCalc);
        return endBusinessHour.toInstant().toEpochMilli() - startBusinessHour.toInstant().toEpochMilli();
    }

    private Double toWorkedDays(long milliseconds, Instant instant, ZoneId zoneId) {
        return (double) milliseconds / (double) getOneWorkedDayInMillis(instant, zoneId);
    }

    private boolean isIssueOnCalculableStatus(Long status) {
        return !(jiraProperties.getStatusesCompletedIds().contains(status) ||
                jiraProperties.getStatusesCanceledIds().contains(status) ||
                jiraProperties.getStatusesDeferredIds().contains(status));
    }
    
}
