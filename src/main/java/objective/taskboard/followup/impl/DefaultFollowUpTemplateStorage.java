package objective.taskboard.followup.impl;

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

import objective.taskboard.followup.FollowUpTemplate;
import objective.taskboard.followup.FollowUpTemplateStorage;
import org.springframework.stereotype.Service;

@Service
public class DefaultFollowUpTemplateStorage implements FollowUpTemplateStorage {

    private FollowUpTemplate template;

    @Override
    public FollowUpTemplate getDefaultTemplate() {
        return new FollowUpTemplate(
            resolve("followup-template/sharedStrings-initial.xml")
            , resolve("followup-template/sharedStrings-template.xml")
            , resolve("followup-template/sharedStrings-si-template.xml")
            , resolve("followup-template/sheet7-template.xml")
            , resolve("followup-template/sheet7-row-template.xml")
            , resolve("followup-template/Followup-template.xlsm")
        );
    }

    @Override
    public FollowUpTemplate getTemplate() {
        return template;
    }

    @Override
    public void updateTemplate(FollowUpTemplate template) {
        this.template = template;
    }

    // ---

    private static String resolve(String resourceName) {
        return DefaultFollowUpTemplateStorage.class.getClassLoader().getResource(resourceName).getFile();
    }
}
