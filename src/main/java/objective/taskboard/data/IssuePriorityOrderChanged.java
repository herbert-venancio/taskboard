package objective.taskboard.data;

import org.springframework.context.ApplicationEvent;

public class IssuePriorityOrderChanged extends ApplicationEvent {

    private static final long serialVersionUID = -329521909076445400L;
    private final TaskboardIssue target;

    public IssuePriorityOrderChanged(Object source, TaskboardIssue target) {
        super(source);
        this.target = target;
    }

    public TaskboardIssue getTarget() {
        return target;
    }

}
