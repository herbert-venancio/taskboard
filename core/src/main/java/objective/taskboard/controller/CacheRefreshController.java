package objective.taskboard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.issueBuffer.IssueBufferService;
import objective.taskboard.task.RefreshCacheTask;

@RestController
@RequestMapping("cache")
public class CacheRefreshController {

    @Autowired
    private IssueBufferService issueBufferService;
    
    @Autowired
    private RefreshCacheTask refreshCacheTask;

    @Autowired
    private CacheManager cacheManager;

    @RequestMapping("issues")
    public String issues() {
        issueBufferService.updateIssueBuffer();

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
        refreshCacheTask.refreshProject();
        refreshCacheTask.refreshIssueTypeVisibility();

        cacheManager.getCacheNames().forEach(cache -> {
            cacheManager.getCache(cache).clear();
        });

        return "CONFIGURATION UPDATED";
    }

}