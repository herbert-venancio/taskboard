package objective.taskboard.configuration;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.Validate;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.domain.TaskboardEntity;

@Entity
@Table(name = "dashboard_configuration")
public class DashboardConfiguration extends TaskboardEntity {
    
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id")
    private ProjectFilterConfiguration project;
    
    @Column(nullable = false)
    private int timelineDaysToDisplay;

    public int getTimelineDaysToDisplay() {
        return timelineDaysToDisplay;
    }
    
    public ProjectFilterConfiguration getProject() {
        return project;
    }

    public void setProject(ProjectFilterConfiguration projectId) {
        Validate.notNull(projectId);
        this.project = projectId;
    }

    public void setTimelineDaysToDisplay(int timelineDaysToDisplay) {
        Validate.isTrue(timelineDaysToDisplay > 0, "The value must be greater than zero: %d", timelineDaysToDisplay);
        this.timelineDaysToDisplay = timelineDaysToDisplay;
    }

}
