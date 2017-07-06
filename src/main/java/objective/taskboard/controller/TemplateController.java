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

import com.google.common.collect.Lists;
import objective.taskboard.followup.FollowUpFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/templates")
public class TemplateController {

    @Autowired
    private FollowUpFacade followUpFacade;

    @RequestMapping
    public List<TemplateData> get() {
        List<TemplateData> list = new ArrayList<>();
        {
            TemplateData item1 = new TemplateData();
            item1.name = "template1";
            item1.projects = Lists.newArrayList("TASKB");
            list.add(item1);
        }
        {
            TemplateData item2 = new TemplateData();
            item2.name = "template2";
            item2.projects = Lists.newArrayList("PROJ1");
            list.add(item2);
        }
        {
            TemplateData item3 = new TemplateData();
            item3.name = "template3";
            item3.projects = Lists.newArrayList("TASKB", "PROJ1");
            list.add(item3);
        }
        list.addAll(followUpFacade.getTemplatesForCurrentUser());
        return list;
    }

    @RequestMapping(method = RequestMethod.POST, consumes="multipart/form-data")
    public void upload(@RequestParam("file") MultipartFile file
            , @RequestParam("name") String templateName
            , @RequestParam("projects") String projects) throws IOException {

        followUpFacade.createTemplate(templateName, projects, file);
    }
}
