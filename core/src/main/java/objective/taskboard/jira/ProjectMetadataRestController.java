package objective.taskboard.jira;



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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.jira.client.JiraCreateIssue;

@RestController
public class ProjectMetadataRestController {

    @Autowired
    private ProjectService projectService;
    
    @RequestMapping(path = "/ws/issues/project-metadata", method = RequestMethod.GET)
    public ResponseEntity<JiraCreateIssue.ProjectMetadata> getProjectMetadata(@RequestParam(name = "projectKey") String projectKey) {
        return projectService.getProjectMetadata(projectKey)
                .map(ResponseEntity::ok)
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(path = "/ws/issues/issue-metadata", method = RequestMethod.GET)
    public Object getIssuesMetadata(@RequestParam(name = "issueKey") String issueKey) {
        return projectService.getIssueMeta(issueKey);
    }
}
