package objective.taskboard.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/*
 * Utility class to publish events from classes that are not managed by spring.
 */
@Service
public class EventPublisherUtil implements ApplicationContextAware {

    private static ApplicationEventPublisher staticEventPublisher;
    
    @Autowired
    public ApplicationEventPublisher ep;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        staticEventPublisher = ep;
    }

    /**
     * Publishes an application event.
     * 
     * @param e the event to publish
     */
    public static void publishEvent(ApplicationEvent e) {
        staticEventPublisher.publishEvent(e);
    }
}