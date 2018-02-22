package objective.taskboard.jira;

/*-
 * [LICENSE]
 * Taskboard
 * - - -
 * Copyright (C) 2015 - 2016 Objective Solutions
 * - - -
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * [/LICENSE]
 */

import java.util.Map.Entry;
import java.util.Optional;

import org.springframework.http.HttpStatus;

import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.util.ErrorCollection;

public class JiraServiceException extends RuntimeException {

    private static final long serialVersionUID = 4426760513185384513L;
    
    private final HttpStatus statusCode;

    public JiraServiceException(RestClientException cause) {
        super(trataMensagemRestClientException(cause), cause);
        this.statusCode = cause.getStatusCode().isPresent() ? 
                HttpStatus.valueOf(cause.getStatusCode().get()) : 
                null;
    }

    public Optional<HttpStatus> getStatusCode() {
        return Optional.ofNullable(statusCode);
    }
    
    private static String trataMensagemRestClientException(RestClientException ex) {
        StringBuilder sbErrorMessages = new StringBuilder();
        sbErrorMessages.append("Could not execute command:\n");

        boolean tryOnJira = false;
        for (ErrorCollection errorCollection : ex.getErrorCollections()) {
            for (Entry<String, String> entry : errorCollection.getErrors().entrySet()) {
                sbErrorMessages.append("\t- ").append(entry.getValue()).append("\n");
                tryOnJira = true;
            }
            for (String errorMessage : errorCollection.getErrorMessages())
                sbErrorMessages.append("\t- ").append(errorMessage).append("\n");
        }

        if (tryOnJira)
            sbErrorMessages.append("\t* Try this operation on Jira.");

        return sbErrorMessages.toString();
    }
}
