package objective.taskboard.jira;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import retrofit.RetrofitError;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class FrontEndMessageException extends RuntimeException {

    private static final long serialVersionUID = -3096553868407268805L;

    public FrontEndMessageException(String message) {
        super(message);
    }

    public FrontEndMessageException(RetrofitError e) {
        super(RetrofitErrorParser.parseExceptionMessage(e));
    }

}
