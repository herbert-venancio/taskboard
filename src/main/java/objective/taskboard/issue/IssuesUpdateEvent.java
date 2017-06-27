package objective.taskboard.issue;

import java.util.List;

import org.springframework.context.ApplicationEvent;

public class IssuesUpdateEvent extends ApplicationEvent {
    private static final long serialVersionUID = -4466242109782827382L;
    public final List<IssueUpdate> updates;
    
    public IssuesUpdateEvent(Object source, List<IssueUpdate> updates) {
        super(source);
        this.updates = updates;
    }
}
