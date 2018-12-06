package objective.taskboard.controller;

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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.jira.FieldMetadataService;
import objective.taskboard.jira.client.JiraFieldDataDto;

@RestController
public class FieldMetadataController {

    @Autowired
    private FieldMetadataService fieldMetadataService;

    @RequestMapping(path = "/ws/field/{id}", method = RequestMethod.GET)
    public JiraFieldDataDto getFieldMetadata(@PathVariable String id) {
        List<JiraFieldDataDto> fields = fieldMetadataService.getFieldsMetadata();
        for (JiraFieldDataDto field : fields)
            if (field.getId().equals(id))
                return field;
        return null;
    }

}
