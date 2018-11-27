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
package objective.taskboard.repository;

import static java.util.stream.Collectors.toList;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableList;

import objective.taskboard.domain.Lane;
import objective.taskboard.domain.Step;

@Service
public class LaneCachedRepository {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LaneCachedRepository.class);
    @Autowired
    private LaneRepository laneRepository;

    private List<Lane> allLanes;
    private List<Step> allSteps;

    @PostConstruct
    private void load() {
        loadCache();
    }

    public List<Lane> getAll() {
        return ImmutableList.copyOf(allLanes);
    }

    public List<Step> getAllSteps() {
        return ImmutableList.copyOf(allSteps);
    }
    
    public void loadCache() {
        log.info("------------------------------ > LaneCachedRepository.loadCache()");
        
        allLanes = laneRepository.findAll();
        allSteps = allLanes.stream()
                .flatMap(l -> l.getStages().stream())
                .flatMap(s -> s.getSteps().stream())
                .collect(toList());
    }
}
