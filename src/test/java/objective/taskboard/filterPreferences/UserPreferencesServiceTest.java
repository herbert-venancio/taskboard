package objective.taskboard.filterPreferences;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.domain.UserPreferences;
import objective.taskboard.repository.UserPreferencesRepository;
import objective.taskboard.testUtils.CredentialHolderUtils;

@RunWith(MockitoJUnitRunner.class)
public class UserPreferencesServiceTest {

    @Mock
    private UserPreferencesRepository repository;

    @InjectMocks
    private UserPreferencesService subject;

    @Test
    public void getLoggedUserPreferences_callReadFromRepositoryWithLoggedUserAsParam() {
        String userName1 = "USER_1";
        String userName2 = "USER_2";
        String preferences = "{}";

        Optional<UserPreferences> userPreferences1 = Optional.of(new UserPreferences(userName1, preferences));
        Optional<UserPreferences> userPreferences2 = Optional.of(new UserPreferences(userName2, preferences));

        when(repository.findOneByJiraUser(userName1)).thenReturn(userPreferences1);
        when(repository.findOneByJiraUser(userName2)).thenReturn(userPreferences2);

        CredentialHolderUtils.mockLoggedUser(userName1, "");
        assertEquals(subject.getLoggedUserPreferences(), userPreferences1);

        CredentialHolderUtils.mockLoggedUser(userName2, "");
        assertEquals(subject.getLoggedUserPreferences(), userPreferences2);
    }

    @Test
    public void save_ifThePreferencesAlreadyExist_thenUpdate() {
        String userName = "USER_1";
        Optional<UserPreferences> userPreferencesOpt = Optional.of(new UserPreferences(userName, "{}"));
        UserPreferences userPreferencesUpdated = new UserPreferences(userName, "{\"field\": \"value\"}");

        when(repository.findOneByJiraUser(userName)).thenReturn(userPreferencesOpt);

        subject.save(userPreferencesUpdated.getJiraUser(), userPreferencesUpdated.getPreferences());

        verify(repository, times(1)).updatePreferences(userPreferencesUpdated);
        verify(repository, never()).add(userPreferencesUpdated);
    }

    @Test
    public void save_ifThePreferencesDontExist_thenSave() {
        String userName = "USER_1";
        UserPreferences userPreferencesCreated = new UserPreferences(userName, "{\"field\": \"value\"}");

        when(repository.findOneByJiraUser(userName)).thenReturn(Optional.empty());

        subject.save(userPreferencesCreated.getJiraUser(), userPreferencesCreated.getPreferences());

        verify(repository, never()).updatePreferences(userPreferencesCreated);
        verify(repository, times(1)).add(userPreferencesCreated);
    }

}
