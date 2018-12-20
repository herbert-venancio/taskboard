package objective.taskboard.followup.budget;

public class ProjectNotFoundException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;
    
    public ProjectNotFoundException(String projectKey) {
        super("The project "+ projectKey+ " could not be found.");
    }

}
