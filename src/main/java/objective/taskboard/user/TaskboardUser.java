package objective.taskboard.user;

import java.time.Instant;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.apache.commons.lang3.Validate;

import objective.taskboard.domain.TaskboardEntity;

@Entity
public class TaskboardUser extends TaskboardEntity {

    @Column(unique = true)
    private String username;

    private boolean isAdmin;
    private Instant lastLogin;

    public TaskboardUser(String username) {
        setUsername(username);
    }

    protected TaskboardUser() {} //JPA

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        Validate.notBlank(username);
        this.username = username;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public Optional<Instant> getLastLogin() {
        return Optional.ofNullable(lastLogin);
    }

    public void setLastLogin(Instant lastLogin) {
        this.lastLogin = lastLogin;
    }
}
