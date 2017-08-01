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
package objective.taskboard.followup;

import java.io.IOException;
import java.nio.file.Path;

public interface UpdateFollowUpService {

    /**
     * Validates that template can be used as Follow-up Spreadsheet template
     * @param decompressed
     */
    void validateTemplate(Path decompressed);

    /**
     * Update 'From Jira' tab from spreadsheet template
     * @param decompressed
     * @param fromJiraTemplate
     * @throws IOException
     */
    void updateFromJiraTemplate(Path decompressed, Path fromJiraTemplate) throws IOException;

    /**
     * Update 'sharedStrings.xml' with data from template
     * @param decompressed
     * @param sharedStringsInitial
     * @throws IOException
     */
    void updateSharedStringsInitial(Path decompressed, Path sharedStringsInitial) throws IOException;

    /**
     * Delete unused files since they are generated with data from jira/taskboard/follow-up.
     * @param decompressed
     * @throws IOException
     */
    void deleteGeneratedFiles(Path decompressed) throws IOException;

}
