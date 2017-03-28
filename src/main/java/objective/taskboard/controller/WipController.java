package objective.taskboard.controller;

import java.util.List;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.domain.WipConfiguration;
import objective.taskboard.repository.WipConfigurationRepository;

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

@RestController
@RequestMapping("/api/wip")
public class WipController {
    @Autowired
    private WipConfigurationRepository wipConfigRepo; 
    
    @RequestMapping(path="{teamName}")
    public ResponseEntity<WipUpdateData> getWip(@PathVariable("teamName") String teamName) {
        List<WipConfiguration> wipConfigurations = wipConfigRepo.findByTeam(teamName);
        
        final WipUpdateData response = new WipUpdateData();
        response.team = teamName;
        for (WipConfiguration wipConfiguration : wipConfigurations) 
            response.statusWip.put(wipConfiguration.getStatus(), wipConfiguration.getWip());
        
        return new ResponseEntity<WipUpdateData>(response, HttpStatus.FOUND);
    }
    
    @RequestMapping(path="{teamName}", method = RequestMethod.PATCH, consumes="application/json")
    public ResponseEntity<Void> updateWip(@PathVariable("teamName") String teamName, @RequestBody WipUpdateData update) {
        List<WipConfiguration> wipConfigurations = wipConfigRepo.findByTeam(teamName);
        for (WipConfiguration wipConfiguration : wipConfigurations) {
            if (update.statusWip.containsKey(wipConfiguration.getStatus())) {
                wipConfiguration.setWip(update.statusWip.get(wipConfiguration.getStatus()));
                wipConfigRepo.save(wipConfiguration);
                update.statusWip.remove(wipConfiguration.getStatus());
            }
        }
        // create non existing status
        for (Entry<String, Integer> newWip : update.statusWip.entrySet()) {
            WipConfiguration wipConfiguration = new WipConfiguration();
            wipConfiguration.setTeam(teamName);
            wipConfiguration.setStatus(newWip.getKey());
            wipConfiguration.setWip(newWip.getValue());
            wipConfigRepo.save(wipConfiguration);
        }
        
        return ResponseEntity.ok().build();
    }
}
