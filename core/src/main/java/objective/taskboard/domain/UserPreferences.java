package objective.taskboard.domain;

import static java.util.Collections.singletonList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


@Entity
@Table(name = "user_preferences")
@NamedQueries({
    @NamedQuery(
            name = "UserPreferences.findByJiraUser",
            query = "SELECT up FROM UserPreferences up WHERE up.jiraUser = :jiraUser")
})
public class UserPreferences extends TaskboardEntity {

    private static final Logger log = LoggerFactory.getLogger(UserPreferences.class);

    private String jiraUser;

    @Lob
    private String preferences;

    @Transient
    private Preferences preferencesObj = null;

    protected UserPreferences() {}

    public UserPreferences(String jiraUser) {
        this(jiraUser, new Preferences());
    }

    public UserPreferences(String jiraUser, Preferences preferences) {
        this.setJiraUser(jiraUser);
        this.setPreferences(preferences);
    }

    public String getJiraUser() {
        return this.jiraUser;
    }

    public Preferences getPreferences() {
        if (this.preferencesObj == null)
            this.preferencesObj = stringToPreferences(this.preferences);
        return this.preferencesObj;
    }

    public void setJiraUser(final String jiraUser) {
        Validate.notEmpty(jiraUser, "jiraUser required");
        this.jiraUser = jiraUser;
    }

    public void setPreferences(final Preferences preferences) {
        Validate.notNull(preferences, "preferences required");
        this.preferencesObj = preferences;
        this.preferences = preferencesToString(preferences);
    }

    private String preferencesToString(Preferences preferences) {
        try {
            return new ObjectMapper().writeValueAsString(preferences);
        } catch (JsonProcessingException e) {
            log.error("Error while generating \"preferences\" string", e);
            throw new IllegalArgumentException(e);
        }
    }

    private Preferences stringToPreferences(String preferences) {
        try {
            return new ObjectMapper().readValue(preferences, Preferences.class);
        } catch (IOException e) {
            log.error("Error while parsing \"preferences\" json", e);
            throw new IllegalArgumentException(e);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Preferences {
        public List<LevelPreference> levelPreferences = new ArrayList<>();
        public Map<String, Boolean> filterPreferences = new HashMap<>();
        public VisibilityConfiguration visibilityConfiguration = new VisibilityConfiguration();
        public List<LaneConfiguration> laneConfiguration = singletonList(new LaneConfiguration());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LevelPreference {
        public String level;
        public Boolean showLevel;
        public Boolean showHeader;
        public Boolean showLaneTeam;
        public Double weightLevel;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VisibilityConfiguration {
        public Boolean showSynthetic = false;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LaneConfiguration {
        public Boolean showCount = false;
    }

}
