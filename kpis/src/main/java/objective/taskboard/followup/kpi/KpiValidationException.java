package objective.taskboard.followup.kpi;

import org.springframework.http.HttpStatus;

public class KpiValidationException  extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;
    private final HttpStatus status;
    
    public KpiValidationException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
    
}
