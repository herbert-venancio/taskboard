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

package objective.taskboard.database.directory;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import objective.taskboard.TaskboardProperties;

@Component
public class DataBaseDirectory {

    private TaskboardProperties taskboardProperties;

    @Autowired
    public DataBaseDirectory(TaskboardProperties taskboardProperties) {
        this.taskboardProperties = taskboardProperties;
    }

    public Path path(String path) {
        Path pathRootDataDirectory = Paths.get(taskboardProperties.getRootDataDirectory());
        return pathRootDataDirectory.resolve("data").resolve(path);
    }

}
