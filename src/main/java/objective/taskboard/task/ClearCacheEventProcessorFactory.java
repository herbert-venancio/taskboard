package objective.taskboard.task;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import objective.taskboard.config.CacheConfiguration;
import objective.taskboard.jira.data.WebHookBody;
import objective.taskboard.jira.data.WebhookEvent;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ClearCacheEventProcessorFactory extends BaseJiraEventProcessorFactory implements JiraEventProcessorFactory {

    @Autowired
    private CacheManager cacheManager;

    @Override
    public JiraEventProcessor create(WebHookBody body, String projectKey) {
        if(!belongsToAnyProject(projectKey))
            return null;

        if(body.webhookEvent.category == WebhookEvent.Category.PROJECT)
            return new ClearCacheEventProcessor(
                    CacheConfiguration.ALL_PROJECTS
                    , CacheConfiguration.PROJECTS);

        if(body.webhookEvent.category == WebhookEvent.Category.VERSION)
            return new ClearCacheEventProcessor(
                    CacheConfiguration.ALL_PROJECTS
                    , CacheConfiguration.PROJECTS);

        return null;
    }

    private class ClearCacheEventProcessor implements JiraEventProcessor {

        private final String[] cacheKeys;

        public ClearCacheEventProcessor(String... cacheKeys) {
            this.cacheKeys = cacheKeys;
        }

        @Override
        public String getDescription() {
            return "clear caches: " + Arrays.toString(cacheKeys);
        }

        @Override
        public void processEvent() {
            for(String key : cacheKeys)
                cacheManager.getCache(key).clear();
        }
    }
}
