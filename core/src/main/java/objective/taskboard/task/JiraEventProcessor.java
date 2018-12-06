package objective.taskboard.task;

public interface JiraEventProcessor {

    String getDescription();

    void processEvent();

}
