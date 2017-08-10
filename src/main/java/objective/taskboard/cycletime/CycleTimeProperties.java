package objective.taskboard.cycletime;

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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@Component
@ConfigurationProperties("cycletime")
@Validated
public class CycleTimeProperties {
    @Valid
    private Time startBusinessHours = new Time(9, 0, "am");
    @Valid
    private Time endBusinessHours = new Time(6, 0, "pm");
    @Data
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Time {
        @NotNull
        private Integer hour;
        @NotNull
        private Integer minute;
        @NotNull
        @NotEmpty
        private String period;
    }
}
