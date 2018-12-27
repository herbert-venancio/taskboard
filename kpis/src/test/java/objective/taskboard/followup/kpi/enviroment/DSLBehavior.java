package objective.taskboard.followup.kpi.enviroment;

public interface DSLBehavior<T> {
    public void execute(KpiEnvironment environment, T subject);
}
