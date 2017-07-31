package objective.taskboard.google;

import java.io.IOException;
import java.util.Optional;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;

public interface GoogleApiService {

    boolean verifyAuthorization() throws IOException;

    SpreadsheetsManager buildSpreadsheetsManager();

    void createAndStoreCredential(String authorizationCode);

    Optional<GoogleCredential> getCredential();

    void removeCredential();

}