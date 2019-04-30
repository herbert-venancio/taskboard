package objective.taskboard.filterPreferences;

import static java.util.Arrays.asList;
import static objective.taskboard.filterPreferences.CardFieldFilterUtils.ISSUE_TYPE_1_VALUE;
import static objective.taskboard.filterPreferences.CardFieldFilterUtils.ISSUE_TYPE_2_VALUE;
import static objective.taskboard.filterPreferences.CardFieldFilterUtils.ISSUE_TYPE_3_VALUE;
import static objective.taskboard.filterPreferences.CardFieldFilterUtils.PROJECT_1_VALUE;
import static objective.taskboard.filterPreferences.CardFieldFilterUtils.PROJECT_2_VALUE;
import static objective.taskboard.filterPreferences.CardFieldFilterUtils.PROJECT_3_VALUE;
import static objective.taskboard.filterPreferences.CardFieldFilterUtils.TEAM_1_VALUE;
import static objective.taskboard.filterPreferences.CardFieldFilterUtils.TEAM_2_VALUE;
import static objective.taskboard.filterPreferences.CardFieldFilterUtils.TEAM_3_VALUE;
import static objective.taskboard.filterPreferences.CardFieldFilterUtils.cardFieldFiltersAllValuesSelected;
import static objective.taskboard.filterPreferences.CardFieldFilterUtils.cardFieldFiltersAllValuesSelectedButProjectAllFalse;
import static objective.taskboard.filterPreferences.CardFieldFilterUtils.getFilterFieldValue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.data.CardFieldFilter;
import objective.taskboard.data.CardFieldFilter.FieldSelector;
import objective.taskboard.data.LaneConfiguration;
import objective.taskboard.database.TaskboardConfigToLaneConfigurationTransformer;
import objective.taskboard.domain.Lane;
import objective.taskboard.domain.UserPreferences;
import objective.taskboard.domain.UserPreferences.LevelPreference;
import objective.taskboard.domain.UserPreferences.Preferences;
import objective.taskboard.repository.UserPreferencesRepository;
import objective.taskboard.testUtils.CredentialHolderUtils;

@RunWith(MockitoJUnitRunner.class)
public class UserPreferencesServiceTest {

    private static final String USERNAME = "USERNAME";

    @Mock
    private UserPreferencesRepository repository;

    @InjectMocks
    private UserPreferencesService subject;

    @Before
    public void setup() {
        CredentialHolderUtils.mockLoggedUser(USERNAME, "");
    }

    @Test
    public void getLoggedUserPreferences_callReadFromRepositoryWithLoggedUserAsParam() {
        Optional<UserPreferences> userPreferences = Optional.of(new UserPreferences(USERNAME, generatePreferences()));

        when(repository.findOneByJiraUser(USERNAME)).thenReturn(userPreferences);

        assertEquals(subject.getLoggedUserPreferences(), userPreferences.get());

        verify(repository, times(1)).findOneByJiraUser(USERNAME);
        verifyNoMoreInteractions(repository);
    }

    @Test
    public void getLoggedUserPreferences_ifNoUserWasFoundOnTheRepository_returnNewUserPreferencesWithTheUsername() {
        when(repository.findOneByJiraUser(USERNAME)).thenReturn(Optional.empty());

        assertEquals(subject.getLoggedUserPreferences(), new UserPreferences(USERNAME, new Preferences()));

        verify(repository, times(1)).findOneByJiraUser(USERNAME);
        verifyNoMoreInteractions(repository);
    }

    @Test
    public void save_ifThePreferencesAlreadyExist_thenUpdate() {
        Optional<UserPreferences> userPreferencesOpt = Optional.of(new UserPreferences(USERNAME, generatePreferences()));
        UserPreferences userPreferencesUpdated = new UserPreferences(USERNAME);

        when(repository.findOneByJiraUser(USERNAME)).thenReturn(userPreferencesOpt);

        subject.save(userPreferencesUpdated.getJiraUser(), userPreferencesUpdated.getPreferences());

        verify(repository, times(1)).findOneByJiraUser(USERNAME);
        verify(repository, times(1)).updatePreferences(userPreferencesUpdated);
        verifyNoMoreInteractions(repository);
    }

    @Test
    public void save_ifThePreferencesDontExist_thenSave() {
        UserPreferences userPreferencesCreated = new UserPreferences(USERNAME, generatePreferences());

        when(repository.findOneByJiraUser(USERNAME)).thenReturn(Optional.empty());

        subject.save(userPreferencesCreated.getJiraUser(), userPreferencesCreated.getPreferences());

        verify(repository, times(1)).findOneByJiraUser(USERNAME);
        verify(repository, times(1)).add(userPreferencesCreated);
        verifyNoMoreInteractions(repository);
    }

    @Test
    public void applyPreferencesOnCardFieldFilter_ifFilterPreferencesIsNotEmpty_modifyValues() {
        List<CardFieldFilter> cardFieldFilters = cardFieldFiltersAllValuesSelected();
        when(repository.findOneByJiraUser(USERNAME)).thenReturn(Optional.of(new UserPreferences(USERNAME, generatePreferences())));

        subject.applyLoggedUserPreferencesOnCardFieldFilter(cardFieldFilters);

        assertEquals(getFilterFieldValue(cardFieldFilters, FieldSelector.ISSUE_TYPE, ISSUE_TYPE_1_VALUE).isSelected(), true);
        assertEquals(getFilterFieldValue(cardFieldFilters, FieldSelector.ISSUE_TYPE, ISSUE_TYPE_2_VALUE).isSelected(), false);
        assertEquals(getFilterFieldValue(cardFieldFilters, FieldSelector.ISSUE_TYPE, ISSUE_TYPE_3_VALUE).isSelected(), false);

        assertEquals(getFilterFieldValue(cardFieldFilters, FieldSelector.PROJECT, PROJECT_1_VALUE).isSelected(), false);
        assertEquals(getFilterFieldValue(cardFieldFilters, FieldSelector.PROJECT, PROJECT_2_VALUE).isSelected(), true);
        assertEquals(getFilterFieldValue(cardFieldFilters, FieldSelector.PROJECT, PROJECT_3_VALUE).isSelected(), false);

        assertEquals(getFilterFieldValue(cardFieldFilters, FieldSelector.TEAM, TEAM_1_VALUE).isSelected(), false);
        assertEquals(getFilterFieldValue(cardFieldFilters, FieldSelector.TEAM, TEAM_2_VALUE).isSelected(), false);
        assertEquals(getFilterFieldValue(cardFieldFilters, FieldSelector.TEAM, TEAM_3_VALUE).isSelected(), true);
    }

    @Test
    public void applyPreferencesOnCardFieldFilter_ifFilterPreferencesIsEmpty_dontDoAnything() {
        List<CardFieldFilter> cardFieldFiltersWithUserPreferences = cardFieldFiltersAllValuesSelectedButProjectAllFalse();
        List<CardFieldFilter> cardFieldFilters = cardFieldFiltersAllValuesSelectedButProjectAllFalse();

        when(repository.findOneByJiraUser(USERNAME)).thenReturn(Optional.of(new UserPreferences(USERNAME)));

        subject.applyLoggedUserPreferencesOnCardFieldFilter(cardFieldFiltersWithUserPreferences);

        assertThat((Object) cardFieldFiltersWithUserPreferences).isEqualToComparingFieldByFieldRecursively(cardFieldFilters);
    }

    @Test
    public void applyPreferencesOnLaneConfiguration_ifLevelPreferencesIsNotEmpty_modifyValues() {
        when(repository.findOneByJiraUser(USERNAME)).thenReturn(Optional.of(new UserPreferences(USERNAME, generatePreferences())));

        List<LaneConfiguration> lanesConfiguration = TaskboardConfigToLaneConfigurationTransformer.transform(asList(
                createLane("LANE_1", 1D, true, true, true),
                createLane("LANE_2", 2D, true, true, true),
                createLane("LANE_3", 3D, true, true, true)
                ));

        subject.applyLoggedUserPreferencesOnLaneConfiguration(lanesConfiguration);

        assertLevelPreference(lanesConfiguration, "LANE_1", true, false, false, 11D);
        assertLevelPreference(lanesConfiguration, "LANE_2", false, true, false, 22D);
        assertLevelPreference(lanesConfiguration, "LANE_3", false, false, true, 33D);
    }

    @Test
    public void applyPreferencesOnLaneConfiguration_ifLevelPreferencesIsEmpty_dontDoAnything() {
        when(repository.findOneByJiraUser(USERNAME)).thenReturn(Optional.of(new UserPreferences(USERNAME)));
        List<Lane> lanes = asList(
                createLane("LANE_1", 1D, true, true, true),
                createLane("LANE_2", 2D, true, true, true),
                createLane("LANE_3", 3D, true, true, true)
                );
        List<LaneConfiguration> lanesConfigurationWithUserPreferences = TaskboardConfigToLaneConfigurationTransformer.transform(lanes);
        List<LaneConfiguration> lanesConfiguration = TaskboardConfigToLaneConfigurationTransformer.transform(lanes);

        subject.applyLoggedUserPreferencesOnLaneConfiguration(lanesConfigurationWithUserPreferences);

        assertThat((Object) lanesConfigurationWithUserPreferences).isEqualToComparingFieldByFieldRecursively(lanesConfiguration);
    }

    private void assertLevelPreference(List<LaneConfiguration> lanesConfiguration, String level, boolean showLevel, boolean showHeader, boolean showLaneTeam, double weight) {
        LaneConfiguration laneConfigurationSelected = lanesConfiguration.stream()
                .filter(laneConfiguration -> laneConfiguration.getLevel().equals(level))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No LaneConfiguration found with \""+ level +"\" level."));

        assertEquals(laneConfigurationSelected.isShowLevel(), showLevel);
        assertEquals(laneConfigurationSelected.isShowHeader(), showHeader);
        assertEquals(laneConfigurationSelected.isShowLaneTeam(), showLaneTeam);
        assertTrue(laneConfigurationSelected.getWeight() == weight);
    }

    private Preferences generatePreferences() {
        Preferences preferences = new Preferences();

        preferences.filterPreferences.put(ISSUE_TYPE_1_VALUE, true);
        preferences.filterPreferences.put(ISSUE_TYPE_2_VALUE, false);
        preferences.filterPreferences.put(ISSUE_TYPE_3_VALUE, false);

        preferences.filterPreferences.put(PROJECT_1_VALUE, false);
        preferences.filterPreferences.put(PROJECT_2_VALUE, true);
        preferences.filterPreferences.put(PROJECT_3_VALUE, false);

        preferences.filterPreferences.put(TEAM_1_VALUE, false);
        preferences.filterPreferences.put(TEAM_2_VALUE, false);
        preferences.filterPreferences.put(TEAM_3_VALUE, true);

        preferences.levelPreferences = asList(
                createLevelPreference("LANE_1", true, false, false, 11D),
                createLevelPreference("LANE_2", false, true, false, 22D),
                createLevelPreference("LANE_3", false, false, true, 33D)
                );

        return preferences;
    }

    private LevelPreference createLevelPreference(String level, boolean showLevel, boolean showHeader, boolean showLaneTeam, double weight) {
        LevelPreference levelPrerence = new LevelPreference();
        levelPrerence.level = level;
        levelPrerence.showLevel = showLevel;
        levelPrerence.showHeader = showHeader;
        levelPrerence.showLaneTeam = showLaneTeam;
        levelPrerence.weightLevel = weight;
        return levelPrerence;
    }

    public static Lane createLane(String name, double weight, boolean showHeader, boolean showLaneTeam, boolean ShowParentIconInSynthetic) {
        Lane lane = new Lane();
        lane.setName(name);
        lane.setWeight(weight);
        lane.setShowHeader(showHeader);
        lane.setShowLaneTeam(showLaneTeam);
        lane.setShowParentIconInSynthetic(ShowParentIconInSynthetic);

        lane.setId(1L);
        lane.setOrdem(1);
        lane.setStages(asList());
        lane.setRules(asList());
        return lane;
    }

}
