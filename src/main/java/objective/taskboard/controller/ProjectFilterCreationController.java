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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;

@RestController
@RequestMapping("/api/projectfilter")
public class ProjectFilterCreationController {
    
    @Autowired
    ProjectFilterConfigurationCachedRepository projectRepository;
    
    @RequestMapping(method = RequestMethod.GET)
    public List<ProjectFilterConfiguration> get() {
        return projectRepository.getProjects();
    }
    
    @RequestMapping(value="{projectKey}", method = RequestMethod.GET)
    public ResponseEntity<Void> get(@PathVariable("projectKey") String projectKey) {
        List<ProjectFilterConfiguration> projects = projectRepository.getProjects();
        for (ProjectFilterConfiguration projectFilterConfiguration : projects) {
            if (projectFilterConfiguration.getProjectKey().equals(projectKey))
                return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    @RequestMapping(method = RequestMethod.POST)
    public void create(@RequestParam(name = "projectKey") String projectKey) {
        List<ProjectFilterConfiguration> projects = projectRepository.getProjects();
        for (ProjectFilterConfiguration projectFilterConfiguration : projects) {
            if (projectFilterConfiguration.getProjectKey().equals(projectKey))
                return;
        }
        
        ProjectFilterConfiguration configuration = new ProjectFilterConfiguration();
        configuration.setProjectKey(projectKey);
        projectRepository.save(configuration);
    }
}
