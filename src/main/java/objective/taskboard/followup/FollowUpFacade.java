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

import objective.taskboard.controller.TemplateData;
import objective.taskboard.issueBuffer.IssueBufferState;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface FollowUpFacade {

    FollowUpGenerator getGenerator();

    FollowUpGenerator getGenerator(String templateName);

    IssueBufferState getFollowupState();

    void createTemplate(String templateName, String projects, MultipartFile file) throws IOException;
    void updateTemplate(Long id, String templateName, String projects, Optional<MultipartFile> file) throws IOException;
    void deleteTemplate(Long id) throws IOException;

    List<TemplateData> getTemplatesForCurrentUser();
}
