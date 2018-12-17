package objective.taskboard.followup.kpi.properties;

import java.util.LinkedList;
import java.util.List;

import javax.validation.Valid;

import org.hibernate.validator.constraints.NotEmpty;

public class IssueTypeChildrenStatusHierarchy {

    @NotEmpty
    @Valid
    private List<Hierarchy> hierarchies = new LinkedList<>();
    
    
    public List<Hierarchy> getHierarchies() {
        return hierarchies;
    }

    public void setHierarchies(List<Hierarchy> hierarchies) {
        this.hierarchies = hierarchies;
    }

    public static class Hierarchy {

        @NotEmpty
        private String fatherStatus;

        private List<Long> childrenTypeId = new LinkedList<>();
        
        private List<String> childrenStatus = new LinkedList<>();
        
        public String getFatherStatus() {
            return fatherStatus;
        }

        public void setFatherStatus(String featureStatus) {
            this.fatherStatus = featureStatus;
        }
        
        public List<Long> getChildrenTypeIds() {
            return childrenTypeId;
        }
        
        public void setChildrenTypeId(List<Long> childrenTypeId) {
            this.childrenTypeId = childrenTypeId;
        }

        public List<String> getChildrenStatuses() {
            return childrenStatus;
        }

        public void setChildrenStatus(List<String> childrenStatus) {
            this.childrenStatus = childrenStatus;
        }

    }
}
