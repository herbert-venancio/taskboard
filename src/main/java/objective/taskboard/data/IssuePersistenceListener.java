package objective.taskboard.data;

import javax.persistence.PostUpdate;

import org.springframework.stereotype.Component;

import objective.taskboard.config.EventPublisherUtil;

@Component
public class IssuePersistenceListener {

    @PostUpdate
    public void postUpdate(TaskboardIssue target) {
        EventPublisherUtil.publishEvent(new IssuePriorityOrderChanged(this, target));
    }
}
