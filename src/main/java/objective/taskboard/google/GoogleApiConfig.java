package objective.taskboard.google;

import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.util.store.DataStore;

public interface GoogleApiConfig {

    DataStore<StoredCredential> getCredentialDataStore();

    GoogleClientSecrets getClientSecrets();

    String getClientId();
}
