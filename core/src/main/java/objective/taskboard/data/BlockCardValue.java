package objective.taskboard.data;

import java.io.Serializable;

public class BlockCardValue implements Serializable {

    private static final long serialVersionUID = 1L;

    private String lastBlockReason;
    private boolean shouldBlockAllSubtasks;

    public BlockCardValue() {}

    public BlockCardValue(String lastBlockReason, boolean shouldBlockAllSubtasks){
        this.lastBlockReason = lastBlockReason;
        this.shouldBlockAllSubtasks = shouldBlockAllSubtasks;
    }

    public String getLastBlockReason() {
        return lastBlockReason;
    }

    public boolean isShouldBlockAllSubtasks() {
        return shouldBlockAllSubtasks;
    }

    public void setShouldBlockAllSubtasks(boolean shouldBlockAllSubtasks) {
        this.shouldBlockAllSubtasks = shouldBlockAllSubtasks;
    }

    public void setLastBlockReason(String lastBlockReason) {
        this.lastBlockReason = lastBlockReason;
    }
}
