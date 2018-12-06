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

package objective.taskboard.controller;

import static org.springframework.http.HttpStatus.GONE;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Deprecated
@RestController
@RequestMapping("/api/teams")
public class TeamController {

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<Void> getTeams() {
        throw new TeamApiDiscontinuedException();
    }

    @RequestMapping(path="{teamName}", method = RequestMethod.GET)
    public ResponseEntity<String> getTeamMembers(@PathVariable("teamName") String teamName) {
        throw new TeamApiDiscontinuedException();
    }

    @RequestMapping(method = RequestMethod.PATCH, consumes="application/json")
    public ResponseEntity<String> updateTeamMembers(@RequestBody List<TeamControllerData> data) {
        throw new TeamApiDiscontinuedException();
    }

    @RequestMapping(path="{teamName}", method = RequestMethod.PATCH, consumes="application/json")
    public ResponseEntity<String> updateTeamMembers(@PathVariable("teamName") String teamName, @RequestBody TeamControllerData data) {
        throw new TeamApiDiscontinuedException();
    }

    @ResponseStatus(value = GONE, reason="Team API was discontinued.")
    public static class TeamApiDiscontinuedException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public TeamApiDiscontinuedException() {
            super();
        }
    }


}
