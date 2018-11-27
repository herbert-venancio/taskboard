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
package objective.taskboard.rules;

import org.apache.commons.io.FileUtils;
import org.junit.rules.ExternalResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Clear new files in a folder after test. Old filed are maintained.
 */
public class CleanupDataFolderRule extends ExternalResource {

    private final Path folder;
    private List<Path> beforeList = Collections.emptyList();

    public CleanupDataFolderRule(Path folder) {
        this.folder = folder;
    }

    @Override
    protected void before() throws Throwable {
        if(Files.exists(folder))
            beforeList = Files.list(folder).collect(Collectors.toList());
    }

    @Override
    protected void after() {
        if(beforeList == Collections.<Path>emptyList()) {
            FileUtils.deleteQuietly(folder.toFile());
        } else {
            try {
                Files.list(folder)
                        .filter(file -> !beforeList.contains(file))
                        .forEach(newFile -> FileUtils.deleteQuietly(newFile.toFile()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}