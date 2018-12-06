package objective.taskboard.testUtils;

import org.springframework.stereotype.Component;

import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.util.store.DataStore;

import objective.taskboard.google.GoogleApiConfig;

@Component
public class GoogleApiConfigMock implements GoogleApiConfig {

    @Override
    public DataStore<StoredCredential> getCredentialDataStore() {
        return null;
    }
    
    @Override
    public GoogleClientSecrets getClientSecrets() {
        return null;
    }

    @Override
    public String getClientId() {
        return "11-nop";
    }

}
