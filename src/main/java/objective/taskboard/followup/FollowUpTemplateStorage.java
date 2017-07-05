package objective.taskboard.followup;

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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface FollowUpTemplateStorage {

    FollowUpTemplate getDefaultTemplate();

    /**
     * Uses a path returned by {@link #storeTemplate} to retrieve it
     * @param template
     * @return
     */
    FollowUpTemplate getTemplate(String template);

    /**
     * Stores a template and returns a unique relative path where it's stored
     * @param template
     * @return
     * @throws IOException
     */
    String storeTemplate(File template, FollowUpTemplateValidator validator) throws IOException;
    String storeTemplate(InputStream input, FollowUpTemplateValidator validator) throws IOException;
}
