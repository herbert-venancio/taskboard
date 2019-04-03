package objective.taskboard.followup.kpi.services;

public interface DSLBehavior<T> {
    public void execute(KpiEnvironment environment, T subject);
}
