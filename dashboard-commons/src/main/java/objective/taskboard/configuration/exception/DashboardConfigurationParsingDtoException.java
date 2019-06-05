package objective.taskboard.configuration.exception;

public abstract class DashboardConfigurationParsingDtoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DashboardConfigurationParsingDtoException(String message, Throwable cause) {
        super(message, cause);
    }

    public DashboardConfigurationParsingDtoException(String message) {
        super(message);
    }

    public static class DashboardConfigurationNullDtoException extends DashboardConfigurationParsingDtoException {
    
        private static final long serialVersionUID = 1L;
    
        public DashboardConfigurationNullDtoException() {
            super("Null DTO cannot be parsed.");
        }
    }

    public static class DashboardConfigurationNonPositiveDaysException extends DashboardConfigurationParsingDtoException {
    
        private static final long serialVersionUID = 1L;
        private static final String MESSAGE = "Timeline days to show must be positive.";
    
        public DashboardConfigurationNonPositiveDaysException(Throwable cause) {
            super(MESSAGE, cause);
        }
    }
}