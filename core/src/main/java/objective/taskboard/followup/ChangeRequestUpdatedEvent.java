package objective.taskboard.followup;

import org.springframework.context.ApplicationEvent;

public class ChangeRequestUpdatedEvent extends ApplicationEvent {
    private static final long serialVersionUID = -4466242109782827382L;

    public ChangeRequestUpdatedEvent(Object source) {
        super(source);
    }
}
