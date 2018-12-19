package objective.taskboard.project.config.changeRequest;

import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.Validate;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.domain.TaskboardEntity;

@Entity
@Table(name = "change_request")
public class ChangeRequest extends TaskboardEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id")
    private ProjectFilterConfiguration project;

    private String name;
    private LocalDate date;
    private Integer budgetIncrease;
    private Boolean isBaseline;

    public ChangeRequest(ProjectFilterConfiguration project, String name, LocalDate date, int budgetIncrease, boolean isBaseline) {
        setProject(project);
        setName(name);
        setDate(date);
        setBudgetIncrease(budgetIncrease);
        setIsBaseline(isBaseline);
    }

    protected ChangeRequest() {}

    public ProjectFilterConfiguration getProject() {
        return project;
    }

    public void setProject(ProjectFilterConfiguration projectId) {
        Validate.notNull(projectId);
        this.project = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        Validate.notBlank(name);
        this.name = name;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        Validate.notNull(date);
        this.date = date;
    }

    public Integer getBudgetIncrease() {
        return budgetIncrease;
    }

    public void setBudgetIncrease(Integer budgetIncrease) {
        if (budgetIncrease == null || budgetIncrease < 0)
            throw new IllegalArgumentException("Budget increase shouldn't be negative.");

        this.budgetIncrease = budgetIncrease;
    }

    public Boolean isBaseline() {
        return isBaseline;
    }

    public void setIsBaseline(Boolean isBaseline) {
        if (isBaseline == null)
            throw new IllegalArgumentException("IsBaseline it's a mandatory field.");
        this.isBaseline = isBaseline;
    }

}
