package objective.taskboard.followup;

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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/followup")
public class FollowUpController {

    @RequestMapping
    public ResponseEntity download() {
//        File file2Upload = new File("src/main/resources/followup-template/Followup.xlsm");
//        Path path = Paths.get(file2Upload.getAbsolutePath());
//        ByteArrayResource resource = null;
//        try {
//            resource = new ByteArrayResource(Files.readAllBytes(path));
//        } catch (IOException e) {
//            System.out.println("there was an error getting the file bytes " + e.getMessage());
//        }
//
//        return ResponseEntity.ok()
//                .contentLength(file2Upload.length())
//                .header("Content-Disposition","attachment; filename=Followup.xlsm")
//                .body(resource);
        return null;
    }

}
