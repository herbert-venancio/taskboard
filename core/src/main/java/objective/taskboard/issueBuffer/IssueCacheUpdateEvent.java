package objective.taskboard.issueBuffer;

import org.springframework.context.ApplicationEvent;

public class IssueCacheUpdateEvent extends ApplicationEvent {
    private static final long serialVersionUID = 4081476668900647625L;
    private final IssueBufferState state;

    public IssueCacheUpdateEvent(Object source, IssueBufferState state) {
        super(source);
        this.state = state;
    }

    public IssueBufferState getState() {
        return state;
    }
}
