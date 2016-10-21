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

import objective.taskboard.auth.CredentialsHolder;
import objective.taskboard.domain.UserPreferences;
import objective.taskboard.filterPreferences.UserPreferencesService;
import org.codehaus.jettison.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/ws/user-preferences")
public class UserPreferencesController {

    @Autowired
    UserPreferencesService service;

    @RequestMapping(path = "update", method = RequestMethod.POST)
    public Boolean updatePreferences(@RequestBody String preferences) throws JSONException {
        final String jiraUser = CredentialsHolder.username();
        UserPreferences userPreferences = new UserPreferences();
        userPreferences.setJiraUser(jiraUser);
        userPreferences.setPreferences(preferences);
        service.insertOrUpdate(userPreferences);
        return true;
    }

}