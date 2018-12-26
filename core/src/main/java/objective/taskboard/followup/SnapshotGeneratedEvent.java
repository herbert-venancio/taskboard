package objective.taskboard.followup;

import org.springframework.context.ApplicationEvent;

public class SnapshotGeneratedEvent extends ApplicationEvent {
    private static final long serialVersionUID = -4466242109782827382L;

    public SnapshotGeneratedEvent(Object source) {
        super(source);
    }
}