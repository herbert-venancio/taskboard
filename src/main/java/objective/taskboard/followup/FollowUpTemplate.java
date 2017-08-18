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

import org.springframework.core.io.Resource;

public class FollowUpTemplate {

    private Resource pathSharedStringsInitial;
    private Resource pathSharedStringsTemplate;
    private Resource pathSISharedStringsTemplate;
    private Resource pathSheet7Template;
    private Resource pathSheet7RowTemplate;
    private Resource pathFollowupTemplateXLSM;
    private Resource pathTable7Template;

    public FollowUpTemplate(Resource pathSharedStringsInitial
            , Resource pathSharedStringsTemplate
            , Resource pathSISharedStringsTemplate
            , Resource pathSheet7Template
            , Resource pathSheet7RowTemplate
            , Resource pathFollowupTemplateXLSM
            , Resource pathTable7Template) {
        this.pathSharedStringsInitial = pathSharedStringsInitial;
        this.pathSharedStringsTemplate = pathSharedStringsTemplate;
        this.pathSISharedStringsTemplate = pathSISharedStringsTemplate;
        this.pathSheet7Template = pathSheet7Template;
        this.pathSheet7RowTemplate = pathSheet7RowTemplate;
        this.pathFollowupTemplateXLSM = pathFollowupTemplateXLSM;
        this.pathTable7Template = pathTable7Template;
    }

    public Resource getPathSharedStringsInitial() {
        return this.pathSharedStringsInitial;
    }

    public Resource getPathSharedStringsTemplate() {
        return this.pathSharedStringsTemplate;
    }

    public Resource getPathSISharedStringsTemplate() {
        return this.pathSISharedStringsTemplate;
    }

    public Resource getPathSheet7Template() {
        return this.pathSheet7Template;
    }

    public Resource getPathSheet7RowTemplate() {
        return this.pathSheet7RowTemplate;
    }

    public Resource getPathFollowupTemplateXLSM() {
        return this.pathFollowupTemplateXLSM;
    }

    public Resource getPathTable7Template() {
        return this.pathTable7Template;
    }
}
