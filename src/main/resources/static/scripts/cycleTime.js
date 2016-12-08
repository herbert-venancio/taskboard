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
        if (endDate < startDate) { return 0; }

        var startBusinessHours = getStartBusinessHours(startDate);
        var startDateAux = startDate < startBusinessHours ? startBusinessHours : startDate;

        var endBusinessHours = getEndBusinessHours(endDate);
        var endDateAux = endDate > endBusinessHours ? endBusinessHours : endDate;

        if (isSameDay(startDateAux, endDateAux))
            return toWorkedDays(endDateAux.getTime() - startDateAux.getTime());

        var millisecondsWorkedStartDate = getEndBusinessHours(startDateAux).getTime() - startDateAux.getTime();
        var millisecondsWorkedEndDate = endDateAux.getTime() - getStartBusinessHours(endDateAux).getTime();
        var millisecondsWorked = millisecondsWorkedStartDate + millisecondsWorkedEndDate;
        var current = getNextDay(startDateAux);

        while (!isSameDay(current, endDateAux)) {
            if (isNotWeekend(current.getDay()))
                millisecondsWorked += getOneWorkedDayInMillis();

            current = getNextDay(current);
        }

        return toWorkedDays(millisecondsWorked);
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

    isNotWeekend = function(day) {
        return day !== SUNDAY && day !== SATURDAY;
    };

    getOneWorkedDayInMillis = function() {
        return getEndBusinessHours(new Date()).getTime() - getStartBusinessHours(new Date()).getTime();
    };

    getNextDay = function(date) {
        var nextDay = new Date(date.getTime() + ONE_DAY_IN_MILLIS);
        return nextDay;
    };

    toWorkedDays = function(milliseconds) {
        return milliseconds / getOneWorkedDayInMillis();
    };

}

var cycleTime = new CycleTime();