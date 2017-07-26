package objective.taskboard.google;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.FileDataStoreFactory;

@Component
@Profile({"prod", "dev"})
class GoogleApiConfigByProperty implements GoogleApiConfig {

    private final GoogleClientSecrets clientSecrets;
    private final DataStore<StoredCredential> credentialDataStore;

    @Autowired
    public GoogleApiConfigByProperty(
            @Value("${google-api.client-secrets-file}") String clientSecretsFile,
            @Value("${google-api.credential-store-dir}") String credentialStoreDir) 
            throws FileNotFoundException, IOException {
        
        this.clientSecrets = GoogleClientSecrets.load(
                JacksonFactory.getDefaultInstance(), 
                new FileReader(resolveHome(clientSecretsFile)));

        File credentialStoreDirFile = new File(resolveHome(credentialStoreDir));
        FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(credentialStoreDirFile);
        this.credentialDataStore = dataStoreFactory.getDataStore(StoredCredential.DEFAULT_DATA_STORE_ID);
    }

    private static String resolveHome(String clientSecretsPath) {
        return clientSecretsPath.replaceFirst("^~", System.getProperty("user.home"));
    }

    @Override
    public DataStore<StoredCredential> getCredentialDataStore() {
        return credentialDataStore;
    }
    
    @Override
    public GoogleClientSecrets getClientSecrets() {
        return clientSecrets;
    }
    
    @Override
    public String getClientId() {
        return clientSecrets.getDetails().getClientId();
    }
}
