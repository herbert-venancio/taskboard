package objective.taskboard.configuration.exception;

public final class DashboardConfigurationDuplicateException extends RuntimeException {
    public static final String MESSAGE = "A dashboard configuration already exists for project %s.";
    private static final long serialVersionUID = 1L;
    public DashboardConfigurationDuplicateException(String projectKey) {
        super(String.format(MESSAGE, projectKey));
    }
}