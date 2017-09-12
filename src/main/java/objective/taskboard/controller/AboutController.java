/*-
 * [LICENSE]
 * Taskboard
 * ---
 * Copyright (C) 2015 - 2017 Objective Solutions
 * ---
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

package objective.taskboard.controller;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;

import java.lang.management.ManagementFactory;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.TaskboardBuildProperties;

@RestController
@RequestMapping("/about")
public class AboutController {

    @Autowired
    private TaskboardBuildProperties taskboardBuildProperties;

    @RequestMapping
    public AboutResponse get() {
        AboutResponse aboutResponse = new AboutResponse();
        aboutResponse.version = taskboardBuildProperties.getVersion();
        aboutResponse.versionDate = taskboardBuildProperties.getVersionDate();
        aboutResponse.upTime = getUpTime();
        aboutResponse.startTime = getStartTime();
        return aboutResponse;
    }

    private String getUpTime() {
        long upTime = ManagementFactory.getRuntimeMXBean().getUptime();
        long days = MILLISECONDS.toDays(upTime);
        upTime -= DAYS.toMillis(days);
        long hours = MILLISECONDS.toHours(upTime);
        upTime -= HOURS.toMillis(hours);
        long minutes = MILLISECONDS.toMinutes(upTime);
        upTime -= MINUTES.toMillis(minutes);
        long seconds = MILLISECONDS.toSeconds(upTime);

        StringBuilder sb = new StringBuilder();
        if (days > 0)
            sb.append(days + " day" + getPluralOrNot(days));
        if (hours > 0)
            sb.append(hours + " hour" + getPluralOrNot(hours));
        if (minutes > 0)
            sb.append(minutes + " minute" + getPluralOrNot(minutes));
        sb.append(seconds + " second" + getPluralOrNot(seconds));

        return sb.toString();
    }

    private String getPluralOrNot(long number) {
        return number > 1 ? "s " : " ";
    }

    private String getStartTime() {
        long startTime = ManagementFactory.getRuntimeMXBean().getStartTime();
        return new DateTime(startTime).toString();
    }

}
