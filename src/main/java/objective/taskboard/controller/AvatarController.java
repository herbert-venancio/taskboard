/*-
 * [LICENSE]
 * Taskboard
 * ---
 * Copyright (C) 2015 - 2018 Objective Solutions
 * ---
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

package objective.taskboard.controller;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import java.net.URL;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.jira.JiraProperties;
import objective.taskboard.jira.JiraService;
import objective.taskboard.jira.data.JiraUser;
import objective.taskboard.jira.endpoint.JiraEndpointAsLoggedInUser;
import objective.taskboard.utils.IOUtilities;

@RestController
@RequestMapping("/ws/avatar")
public class AvatarController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AvatarController.class);

    @Autowired
    private JiraService jiraService;

    @Autowired
    private JiraProperties jiraProperties;

    @Autowired
    private JiraEndpointAsLoggedInUser jiraEndpointAsUser;

    @GetMapping
    public ResponseEntity<Object> getAvatar(@RequestParam("username") String username, HttpServletResponse response) {
        try {
            if (username.isEmpty())
                return ResponseEntity.notFound().build();

            JiraUser jiraUser = jiraService.getJiraUser(username);
            URL url = jiraUser.getAvatarUri24().toURL();
            String contentType = url.openConnection().getContentType();
            Resource resource = IOUtilities.asResource(url);
            if (url.toString().contains(jiraProperties.getUrl()))
                resource = IOUtilities.asResource(jiraEndpointAsUser.readBytesFromURL(url));

            response.setHeader("Cache-Control", "private, max-age=21600");
            response.setHeader("Pragma", "cache");
            return ResponseEntity.ok()
                    .contentLength(resource.contentLength())
                    .header("Content-Type", contentType)
                    .body(resource);
        } catch (Exception e) {
            log.warn("Error getting avatar", e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .header("Content-Type", "text/html; charset=utf-8")
                    .body(defaultIfEmpty(e.getMessage(), e.toString()));
        }
    }

}
