package objective.taskboard.jira.properties;

import java.util.HashMap;
import java.util.Map;

public abstract class StatusConfiguration {

    protected String[] demands;
    protected String[] tasks;
    protected String[] subtasks;
    
    public String[] getDemands() {
        return demands;
    }
    public String[] getTasks() {
        return tasks;
    }
    public String[] getSubtasks() {
        return subtasks;
    }
    public void setDemands(String[] demands) {
        this.demands = demands;
    }
    public void setTasks(String[] tasks) {
        this.tasks = tasks;
    }
    public void setSubtasks(String[] subtasks) {
        this.subtasks = subtasks;
    }
    
    public static class StatusCountingOnWip extends StatusConfiguration {}
    
    public static class FinalStatuses extends StatusConfiguration {}
    
    public static class StatusPriorityOrder extends StatusConfiguration {

        private Map<String, Integer> demandPriorityByStatus;
        private Map<String, Integer> taskPriorityByStatus;
        private Map<String, Integer> subtaskPriorityByStatus;

        private Map<String, Integer> initMap(String[] statusInOrder) {
            Map<String, Integer> map = new HashMap<>();
            for(int i = 0; i < statusInOrder.length; i++) {
                map.put(statusInOrder[i], i);
            }
            return map;
        }

        private void ensureDemandInitialized() {
            if (demandPriorityByStatus == null)
                demandPriorityByStatus = initMap(demands);
        }

        private void ensureTaskInitialized() {
            if (taskPriorityByStatus == null)
                taskPriorityByStatus = initMap(tasks);
        }

        private void ensureSubtaskInitialized() {
            if (subtaskPriorityByStatus == null)
                subtaskPriorityByStatus = initMap(subtasks);
        }

        public Integer getDemandPriorityByStatus(String status) {
            ensureDemandInitialized();
            return demandPriorityByStatus.get(status);
        }

        public Integer getTaskPriorityByStatus(String status) {
            ensureTaskInitialized();
            return taskPriorityByStatus.get(status);
        }
        public Integer getSubtaskPriorityByStatus(String status) {
            ensureSubtaskInitialized();
            return subtaskPriorityByStatus.get(status);
        }
        public String[] getDemandsInOrder() {
            return demands;
        }
        public String[] getTasksInOrder() {
            return tasks;
        }
        public String[] getSubtasksInOrder() {
            return subtasks;
        }
        
     }
    
}
