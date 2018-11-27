package objective.taskboard.project.config.changeRequest;

public class ChangeRequestBaselineRemovalException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    public ChangeRequestBaselineRemovalException() {
        super("The first change request of a project, named 'Baseline' must never be removed.");
    }

}
