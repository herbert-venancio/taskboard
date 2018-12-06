package objective.taskboard.config;

import java.lang.reflect.Method;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import objective.taskboard.jira.data.JiraUser;
import objective.taskboard.jira.endpoint.JiraEndpointAsLoggedInUser;

@Component(LoggedInUserLocaleKeyGenerator.NAME)
@SessionScope
public class LoggedInUserLocaleKeyGenerator implements KeyGenerator {
    public static final String NAME = "loggedInUserLocaleKeyGenerator";

    @Autowired
    private JiraEndpointAsLoggedInUser jiraEndpointAsUser;

    private String userLocale;

    @Override
    public Object generate(Object target, Method method, Object... params) {
        if(userLocale != null)
            return userLocale;

        return userLocale = load();
    }

    private String load() {
        return jiraEndpointAsUser.request(JiraUser.Service.class).myself().locale;
    }
}
