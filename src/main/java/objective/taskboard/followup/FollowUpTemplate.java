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

import lombok.Getter;

import java.nio.file.Path;

@Getter
public class FollowUpTemplate {

    private Path pathSharedStringsInitial;
    private Path pathSharedStringsTemplate;
    private Path pathSISharedStringsTemplate;
    private Path pathSheet7Template;
    private Path pathSheet7RowTemplate;
    private Path pathFollowupTemplateXLSM;
    private Path pathTable7Template;

    public FollowUpTemplate(Path pathSharedStringsInitial
            , Path pathSharedStringsTemplate
            , Path pathSISharedStringsTemplate
            , Path pathSheet7Template
            , Path pathSheet7RowTemplate
            , Path pathFollowupTemplateXLSM
            , Path pathTable7Template) {
        this.pathSharedStringsInitial = pathSharedStringsInitial;
        this.pathSharedStringsTemplate = pathSharedStringsTemplate;
        this.pathSISharedStringsTemplate = pathSISharedStringsTemplate;
        this.pathSheet7Template = pathSheet7Template;
        this.pathSheet7RowTemplate = pathSheet7RowTemplate;
        this.pathFollowupTemplateXLSM = pathFollowupTemplateXLSM;
        this.pathTable7Template = pathTable7Template;
    }
}
