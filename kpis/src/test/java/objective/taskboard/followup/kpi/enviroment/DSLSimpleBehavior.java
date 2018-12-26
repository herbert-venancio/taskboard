package objective.taskboard.followup.kpi.enviroment;

public interface DSLSimpleBehavior<T> {

    public void behave(KpiEnvironment environment);

    public T then();
}
