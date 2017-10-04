package objective.taskboard.data;

import java.io.Serializable;

public class Subtask implements Serializable {
    private static final long serialVersionUID = -1394125985489540555L;
    public String issueKey;
    public String summary;
    
    public Subtask() {}
    
    public Subtask(String key, String summary) {
        this.issueKey = key;
        this.summary = summary;
    }
}
