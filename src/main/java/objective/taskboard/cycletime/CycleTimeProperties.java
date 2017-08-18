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
package objective.taskboard.cycletime;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties("cycletime")
@Validated
public class CycleTimeProperties {
    @Valid
    private Time startBusinessHours = new Time(9, 0, "am");
    @Valid
    private Time endBusinessHours = new Time(6, 0, "pm");

    public static class Time {
        @NotNull
        private Integer hour;
        @NotNull
        private Integer minute;
        @NotNull
        @NotEmpty
        private String period;

        public Integer getHour() {
            return this.hour;
        }

        public Integer getMinute() {
            return this.minute;
        }

        public String getPeriod() {
            return this.period;
        }

        public void setHour(final Integer hour) {
            this.hour = hour;
        }

        public void setMinute(final Integer minute) {
            this.minute = minute;
        }

        public void setPeriod(final String period) {
            this.period = period;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Time time = (Time) o;

            if (hour != null ? !hour.equals(time.hour) : time.hour != null) return false;
            if (minute != null ? !minute.equals(time.minute) : time.minute != null) return false;
            return period != null ? period.equals(time.period) : time.period == null;
        }

        @Override
        public int hashCode() {
            int result = hour != null ? hour.hashCode() : 0;
            result = 31 * result + (minute != null ? minute.hashCode() : 0);
            result = 31 * result + (period != null ? period.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Time{" +
                    "hour=" + hour +
                    ", minute=" + minute +
                    ", period='" + period + '\'' +
                    '}';
        }

        @java.beans.ConstructorProperties({"hour", "minute", "period"})
        private Time(final Integer hour, final Integer minute, final String period) {
            this.hour = hour;
            this.minute = minute;
            this.period = period;
        }
    }

    public Time getStartBusinessHours() {
        return this.startBusinessHours;
    }

    public Time getEndBusinessHours() {
        return this.endBusinessHours;
    }

    public void setStartBusinessHours(final Time startBusinessHours) {
        this.startBusinessHours = startBusinessHours;
    }

    public void setEndBusinessHours(final Time endBusinessHours) {
        this.endBusinessHours = endBusinessHours;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CycleTimeProperties that = (CycleTimeProperties) o;

        if (startBusinessHours != null ? !startBusinessHours.equals(that.startBusinessHours) : that.startBusinessHours != null)
            return false;
        return endBusinessHours != null ? endBusinessHours.equals(that.endBusinessHours) : that.endBusinessHours == null;
    }

    @Override
    public int hashCode() {
        int result = startBusinessHours != null ? startBusinessHours.hashCode() : 0;
        result = 31 * result + (endBusinessHours != null ? endBusinessHours.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CycleTimeProperties{" +
                "startBusinessHours=" + startBusinessHours +
                ", endBusinessHours=" + endBusinessHours +
                '}';
    }
}
