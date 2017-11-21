package objective.taskboard.task;

import java.util.Arrays;
import java.util.Optional;

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
public class ClearCacheEventProcessorFactory implements JiraEventProcessorFactory {

    @Autowired
    private CacheManager cacheManager;

    @Override
    public Optional<JiraEventProcessor> create(WebHookBody body, String projectKey) {
        if(body.webhookEvent.category != WebhookEvent.Category.VERSION)
            return Optional.empty();

        return Optional.of(new ClearCacheEventProcessor(
                CacheConfiguration.ALL_PROJECTS
                , CacheConfiguration.PROJECTS));
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
