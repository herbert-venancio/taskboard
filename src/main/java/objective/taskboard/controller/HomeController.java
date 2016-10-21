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

import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import objective.taskboard.data.User;
import objective.taskboard.jira.JiraService;

@Controller
public class HomeController {

    @Autowired
    private JiraService jiraService;

    @RequestMapping("/")
    public String home(Model model) {
        User user = jiraService.getUser();
        model.addAttribute("user", serialize(user));
        return "index";
    }

    private String serialize(User user) {
        try {
            return new ObjectMapper().writeValueAsString(user);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
