package objective.taskboard.followup.kpi;

import java.util.Optional;

import objective.taskboard.followup.kpi.StatusTransitionBuilder.DefaultStatus;

public class IssueKpiBuilder {

    private final String pKey;
    private final String type;
    private final KpiLevel level;
    private StatusTransitionBuilder statusBuilder = new StatusTransitionBuilder();

    public IssueKpiBuilder(String pKey, String type, KpiLevel level) {
        this.pKey = pKey;
        this.type = type;
        this.level = level;
    }

    public IssueKpiBuilder addTransition(DefaultStatus step) {
        statusBuilder.addTransition(step);
        return this;
    }
    
    public IssueKpiBuilder addTransition(DefaultStatus step, String date) {
        statusBuilder.addTransition(step,date);
        return this;
    }

    public IssueKpi build() {
        Optional<StatusTransition> firstChain = statusBuilder.build();
        return new IssueKpi(pKey, type, level,firstChain);
    }


}
