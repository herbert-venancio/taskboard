/*-
 * [LICENSE]
 * Taskboard
 * - - -
 * Copyright (C) 2015 - 2016 Objective Solutions
 * - - -
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * [/LICENSE]
 */

function CycleTime() {

    var ONE_DAY_IN_MILLIS = 24 * 60 * 60 * 1000;
    var SUNDAY = 0;
    var SATURDAY = 6;

    this.getCycleTime = function(startDate, endDate) {
        var startWorkDay = getStartWorkDay(startDate);
        var endWorkDay = getEndWorkDay(endDate);

        if (endWorkDay < startWorkDay)
            return 0;

        if (isSameDay(startWorkDay, endWorkDay))
            return toWorkedDays(endWorkDay.getTime() - startWorkDay.getTime());

        var millisecondsWorkedStartDate = getEndBusinessHours(startWorkDay).getTime() - startWorkDay.getTime();
        var millisecondsWorkedEndDate = endWorkDay.getTime() - getStartBusinessHours(endWorkDay).getTime();
        var millisecondsWorked = millisecondsWorkedStartDate + millisecondsWorkedEndDate;

        var currentDate = getNextDay(startWorkDay);
        while (!isSameDay(currentDate, endWorkDay)) {
            if (isWorkDay(currentDate))
                millisecondsWorked += getOneWorkedDayInMillis();

            currentDate = getNextDay(currentDate);
        }

        return toWorkedDays(millisecondsWorked);
    };

    getStartWorkDay = function(startDate) {
        var startBusinessHours = getStartBusinessHours(startDate);
        if (isWorkDay(startDate))
            return startDate < startBusinessHours ? startBusinessHours : startDate;

        var currentDate = getNextDay(startBusinessHours);
        while (!isWorkDay(currentDate))
            currentDate = getNextDay(currentDate);

        return currentDate;
    };

    getEndWorkDay = function(endDate) {
        var endBusinessHours = getEndBusinessHours(endDate);
        if (isWorkDay(endDate))
            return endDate > endBusinessHours ? endBusinessHours : endDate;

        var currentDate = getPreviousDay(endBusinessHours);
        while (!isWorkDay(currentDate))
            currentDate = getPreviousDay(currentDate);

        return currentDate;
    };

    getStartBusinessHours = function(date) {
        var hour = getHourPeriod(START_BUSINESS_HOURS.hour, START_BUSINESS_HOURS.period);
        return new Date(date.getFullYear(), date.getMonth(), date.getDate(), hour, START_BUSINESS_HOURS.minute, 0, 0);
    };

    getEndBusinessHours = function(date) {
        var hour = getHourPeriod(END_BUSINESS_HOURS.hour, END_BUSINESS_HOURS.period);
        return new Date(date.getFullYear(), date.getMonth(), date.getDate(), hour, END_BUSINESS_HOURS.minute, 0, 0);
    };

    getHourPeriod = function(hour, period) {
        return period == 'pm' ? hour + 12 : hour;
    };

    isSameDay = function(startDate, endDate) {
        return startDate.getDate() == endDate.getDate() &&
               startDate.getMonth() == endDate.getMonth() &&
               startDate.getFullYear() == endDate.getFullYear();
    };

    isWorkDay = function(date) {
        if (date.getDay() == SUNDAY || date.getDay() == SATURDAY)
            return false;

        for (var h in HOLIDAYS) {
            var holiday = new Date(HOLIDAYS[h].day);
            if (isSameDay(date, holiday))
                return false;
        }

        return true;
    };

    getOneWorkedDayInMillis = function() {
        return getEndBusinessHours(new Date()).getTime() - getStartBusinessHours(new Date()).getTime();
    };

    getNextDay = function(date) {
        return new Date(date.getTime() + ONE_DAY_IN_MILLIS);
    };

    getPreviousDay = function(date) {
        return new Date(date.getTime() - ONE_DAY_IN_MILLIS);;
    };

    toWorkedDays = function(milliseconds) {
        return milliseconds / getOneWorkedDayInMillis();
    };

}

var cycleTime = new CycleTime();
