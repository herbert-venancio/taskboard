package objective.taskboard.google;

import java.io.IOException;
import java.util.Optional;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.google.api.client.auth.oauth2.DataStoreCredentialRefreshListener;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStore;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.sheets.v4.Sheets;

import objective.taskboard.auth.CredentialsHolder;

@Component
@Profile({"prod", "dev"})
class GoogleApiServiceHttp implements GoogleApiService {
    private static final String APPLICATION_NAME = "Objective Taskboard";
    
    private final GoogleApiConfig config;
    private final JsonFactory jsonFactory;
    private final HttpTransport httpTransport;
    private final DataStore<StoredCredential> credentialDataStore;

    @Autowired
    public GoogleApiServiceHttp(GoogleApiConfig config){
        this.config = config;

        try {
            jsonFactory = JacksonFactory.getDefaultInstance();
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            credentialDataStore = config.getCredentialDataStore();

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean verifyAuthorization() throws IOException {
        Optional<GoogleCredential> optionalCredential = getCredential();
        if (!optionalCredential.isPresent())
            return false;
        
        GoogleCredential credential = optionalCredential.get();

        if (credential.getExpiresInSeconds() == null || credential.getExpiresInSeconds() < 60) {
            try {
                credential.refreshToken();

            } catch (TokenResponseException e) {
                if (e.getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
                    removeCredential();
                    return false;
                }
            }
        }
        
        Oauth2 oauth2 = new Oauth2.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
        
        try {
            oauth2.tokeninfo().setAccessToken(credential.getAccessToken()).execute();
            return true;
            
        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
                removeCredential();
                return false;
            }
            
            throw e;
        }
    }
    
    private Sheets getSheets() {
        Optional<GoogleCredential> credential = getCredential();
        
        if (!credential.isPresent())
            throw new RuntimeException("There is no Google credential to user " + getLoggedUser());
        
        return new Sheets.Builder(httpTransport, jsonFactory, credential.get())
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
    
    @Override
    public SpreadsheetsManager buildSpreadsheetsManager() {
    	Sheets sheets = getSheets();
    	return new SpreadsheetsManager(sheets.spreadsheets());
    }

    @Override
    public void createAndStoreCredential(String authorizationCode) {
        try {
            String userId = getLoggedUser();
            GoogleClientSecrets clientSecrets = config.getClientSecrets();
            
            GoogleAuthorizationCodeTokenRequest request = new GoogleAuthorizationCodeTokenRequest(
                    httpTransport,
                    jsonFactory, 
                    "https://www.googleapis.com/oauth2/v4/token",
                    clientSecrets.getDetails().getClientId(), 
                    clientSecrets.getDetails().getClientSecret(),
                    authorizationCode, 
                    "postmessage");

            GoogleTokenResponse tokenResponse = request.execute();
            GoogleCredential credential = newCredential().setFromTokenResponse(tokenResponse);

            credentialDataStore.set(userId, new StoredCredential(credential));

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private GoogleCredential newCredential() {
        return new GoogleCredential.Builder()
                .setTransport(httpTransport)
                .setJsonFactory(jsonFactory)
                .setClientSecrets(config.getClientSecrets())
                .addRefreshListener(new DataStoreCredentialRefreshListener(getLoggedUser(), credentialDataStore))
                .build();
    }

    @Override
    public Optional<GoogleCredential> getCredential() {
        StoredCredential storedCredential;
        try {
            storedCredential = credentialDataStore.get(getLoggedUser());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        
        if (storedCredential == null)
            return Optional.empty();
        
        GoogleCredential credential = newCredential();
        credential.setAccessToken(storedCredential.getAccessToken());
        credential.setRefreshToken(storedCredential.getRefreshToken());
        credential.setExpirationTimeMilliseconds(storedCredential.getExpirationTimeMilliseconds());

        return Optional.of(credential);
    }

    private String getLoggedUser() {
        return CredentialsHolder.username();
    }

    @Override
    public void removeCredential() {
        try {
            credentialDataStore.delete(getLoggedUser());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
