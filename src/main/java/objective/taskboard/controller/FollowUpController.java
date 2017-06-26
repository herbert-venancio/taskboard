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

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.followup.FollowUpGenerator;
import objective.taskboard.followup.FollowupDataProvider;

@RestController
@RequestMapping("/api/followup")
public class FollowUpController {

    @Autowired
    private FollowupDataProvider provider;

    @RequestMapping
    public ResponseEntity<Object> download() {
        try {
            FollowUpGenerator followupGenerator = new FollowUpGenerator(provider);
            ByteArrayResource resource = followupGenerator.generate();
            return ResponseEntity.ok()
                  .contentLength(resource.contentLength())
                  .header("Content-Disposition","attachment; filename=Followup.xlsm")
                  .body(resource);
        } catch (Exception e) {
            return new ResponseEntity<Object>(e.getMessage(), INTERNAL_SERVER_ERROR);
        }
    }

}
