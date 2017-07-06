package objective.taskboard.controller;

/*-
 * [LICENSE]
 * Taskboard
 * ---
 * Copyright (C) 2015 - 2017 Objective Solutions
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

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import objective.taskboard.followup.FollowUpFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.followup.FollowUpGenerator;
import objective.taskboard.followup.FollowupDataProvider;
import objective.taskboard.issueBuffer.IssueBufferState;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/ws/followup")
public class FollowUpController {

    @Autowired
    private FollowupDataProvider provider;

    @Autowired
    private FollowUpFacade followUpFacade;

    @RequestMapping
    public ResponseEntity<Object> download(@RequestParam("projects") String projects) {
        if (ObjectUtils.isEmpty(projects))
            return new ResponseEntity<>("You must provide a list of projects separated by comma", BAD_REQUEST);

        String [] includedProjects = projects.split(",");
        try {
            FollowUpGenerator followupGenerator = new FollowUpGenerator(provider);
            ByteArrayResource resource = followupGenerator.generate(includedProjects);
            return ResponseEntity.ok()
                  .contentLength(resource.contentLength())
                  .header("Content-Disposition","attachment; filename=Followup.xlsm")
                  .body(resource);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(method = RequestMethod.POST, consumes="multipart/form-data")
    public void upload(@RequestParam("file") MultipartFile file) throws IOException {
        followUpFacade.updateTemplate(file);
    }

    @RequestMapping("state")
    public IssueBufferState getState() {
        return provider.getFollowupState();
    }
}
