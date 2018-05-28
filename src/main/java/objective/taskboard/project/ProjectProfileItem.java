package objective.taskboard.project;

import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.apache.commons.lang3.Validate;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.domain.TaskboardEntity;

@Entity
@Table(name = "project_profile_item")
@NamedQueries({
    @NamedQuery(
            name="ProjectProfileItem.listByProject", 
            query="SELECT p FROM ProjectProfileItem p WHERE p.project = :project ORDER BY p.roleName, p.allocationStart")
})
public class ProjectProfileItem extends TaskboardEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id")
    private ProjectFilterConfiguration project;

    private String roleName;
    private Integer peopleCount;
    private LocalDate allocationStart;
    private LocalDate allocationEnd;
    
    public ProjectProfileItem(ProjectFilterConfiguration project, String roleName, Integer peopleCount, LocalDate allocationStart, LocalDate allocationEnd) {
        setProject(project);
        setRoleName(roleName);
        setPeopleCount(peopleCount);
        setAllocationPeriod(allocationStart, allocationEnd);
    }

    protected ProjectProfileItem() {} //JPA
    
    public ProjectFilterConfiguration getProject() {
        return project;
    }
    
    public void setProject(ProjectFilterConfiguration project) {
        Validate.notNull(project);
        this.project = project;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        Validate.notBlank(roleName);
        this.roleName = roleName;
    }

    public Integer getPeopleCount() {
        return peopleCount;
    }

    public void setPeopleCount(int peopleCount) {
        if (peopleCount < 0)
            throw new IllegalArgumentException("People count should be a positive value");

        this.peopleCount = peopleCount;
    }

    public LocalDate getAllocationStart() {
        return allocationStart;
    }

    public LocalDate getAllocationEnd() {
        return allocationEnd;
    }

    public void setAllocationPeriod(LocalDate start, LocalDate end) {
        Validate.notNull(start);
        Validate.notNull(end);
        
        if (end.isBefore(start))
            throw new IllegalArgumentException("Allocation start date should be after or equals allocation end date");
        
        this.allocationStart = start;
        this.allocationEnd = end;
    }
}
