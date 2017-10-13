package objective.taskboard.jira;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.web.client.HttpClientErrorException;

public class FrontEndMessageException extends RuntimeException {

    private static final long serialVersionUID = -3096553868407268805L;

    public FrontEndMessageException(HttpClientErrorException e) {
        super(parseExceptionMessage(e), e);
    }

    private static String parseExceptionMessage(HttpClientErrorException e) {
        List<String> errors = new ArrayList<>();
        errors.addAll(getErrorsFromResponse(e));
        errors.addAll(getErrorsMessagesFromResponse(e));
        return errorsListToString(errors);
    }

    private static String errorsListToString(List<String> errors) {
        return String.join("\n", errors);
    }

    private static List<String> getErrorsFromResponse(HttpClientErrorException error) {
        List<String> errors = new ArrayList<>();
        try {
            JSONObject jsonObj = new JSONObject(error.getResponseBodyAsString());
            JSONObject jsonErrors = (JSONObject) jsonObj.get("errors");
            Iterator<?> itErrors = jsonErrors.keys();
            while(itErrors.hasNext()) {
                String key = (String) itErrors.next();
                errors.add((String) jsonErrors.get(key));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
        return errors;
    }

    private static List<String> getErrorsMessagesFromResponse(HttpClientErrorException error) {
        List<String> errors = new ArrayList<>();
        try {
            JSONObject jsonObj = new JSONObject(error.getResponseBodyAsString());
            JSONArray jsonErrors = (JSONArray) jsonObj.get("errorMessages");
            for(int i = 0; i < jsonErrors.length(); i++)
                errors.add(jsonErrors.getString(i));
        } catch (JSONException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
        return errors;
    }

}