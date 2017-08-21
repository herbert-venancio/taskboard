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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.issueBuffer.AllIssuesBufferService;
import objective.taskboard.issueBuffer.IssueBufferService;
import objective.taskboard.task.RefreshCacheTask;

@RestController
@RequestMapping("cache")
public class CacheRefreshController {

    @Autowired
    private IssueBufferService issueBufferService;
    
    @Autowired
    private AllIssuesBufferService allIssuesBufferService;

    @Autowired
    private RefreshCacheTask refreshCacheTask;

    @Autowired
    private CacheManager cacheManager;

    @RequestMapping("issues")
    public String issues() {
        issueBufferService.updateIssueBuffer();
        allIssuesBufferService.updateAllIssuesBuffer();

        return "ISSUES UPDATE STARTED";
    }

    @RequestMapping("issues/{key}")
    public String issue(@PathVariable("key") String key) {
        issueBufferService.updateIssueBuffer(key);

        return "ISSUE " + key + " UPDATED";
    }

    @RequestMapping("configuration")
    public String configuration() {
        refreshCacheTask.refreshUserTeam();
        refreshCacheTask.refreshTeam();
        refreshCacheTask.refreshTeamFilterConfiguration();
        refreshCacheTask.refreshFilter();
        refreshCacheTask.refreshLane();
        refreshCacheTask.refreshProject();
        refreshCacheTask.refreshIssueTypeVisibility();

        cacheManager.getCacheNames().forEach(cache -> {
            cacheManager.getCache(cache).clear();
        });

        return "CONFIGURATION UPDATED";
    }

}