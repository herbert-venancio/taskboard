package objective.taskboard.configuration.exception;

public final class DashboardConfigurationNotFoundException extends RuntimeException {
    public static final String MESSAGE = "Missing dashboard configuration for project %s.";
    private static final long serialVersionUID = 1L;
    public DashboardConfigurationNotFoundException(String projectKey) {
        super(String.format(MESSAGE, projectKey));
    }
}