package objective.taskboard.followup.kpi.enviroment;

public interface KpiDataPointAsserter<P> {
    public static final double DELTA = 0.1;
    public void doAssert(P subject);
}