package objective.taskboard.monitor;

import static java.util.Arrays.asList;
import static objective.taskboard.monitor.MonitorCalculator.CANT_CALCULATE_MESSAGE;

import java.util.List;
import java.util.function.Supplier;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import objective.taskboard.followup.ProjectDatesNotConfiguredException;
import objective.taskboard.followup.cluster.ClusterNotConfiguredException;


@RunWith(Parameterized.class)
public class MonitorCalculatorTest {

    @Parameters(name = "{0}")
    public static List<Object[]> parameters() {
        return asList(
            parameters(
                      "Timeline"
                      , MonitorCalculatorDSL::forTimeline
                      , "Can't calculate Timeline: The project has no start or delivery date."
                      , "Can't calculate Timeline: No cluster configuration found.")
            , parameters(
                    "Cost"
                    , MonitorCalculatorDSL::forCost
                    , "Can't calculate Cost: The project has no start or delivery date."
                    , "Can't calculate Cost: No cluster configuration found.")
            , parameters(
                    "Scope"
                    , MonitorCalculatorDSL::forScope
                    , "Can't calculate Scope: The project has no start or delivery date."
                    , "Can't calculate Scope: No cluster configuration found.")
        );
    }

    private static Object[] parameters(String testName, Supplier<MonitorCalculatorDSL> dslSupplier, String expectedMessageNoProjectDates, String expectedMessageNoCluster) {
        return new Object[] {testName, dslSupplier, expectedMessageNoProjectDates, expectedMessageNoCluster};
    }

    @Parameterized.Parameter
    public String testName;

    @Parameterized.Parameter(1)
    public Supplier<MonitorCalculatorDSL> dslSupplier;

    @Parameterized.Parameter(2)
    public String expectedMessageNoProjectDates;

    @Parameterized.Parameter(3)
    public String expectedMessageNoCluster;

    @Test
    public void givenProjectDateNotConfigured_thenThrowProjectDatesNotConfiguredException() {
        givenServiceThrowing(new ProjectDatesNotConfiguredException())

        .whenCalculate()

        .then()
            .assertActual(CANT_CALCULATE_MESSAGE)
            .assertExpected(CANT_CALCULATE_MESSAGE)
            .assertStatus(null)
            .assertWarning(CANT_CALCULATE_MESSAGE)
            .assertErrors(expectedMessageNoProjectDates);
    }

    @Test
    public void givenClusterNotConfigured_thenThrowClusterNotConfiguredException() {
        givenServiceThrowing(new ClusterNotConfiguredException())

        .whenCalculate()

        .then()
            .assertActual(CANT_CALCULATE_MESSAGE)
            .assertExpected(CANT_CALCULATE_MESSAGE)
            .assertStatus(null)
            .assertWarning(CANT_CALCULATE_MESSAGE)
            .assertErrors(expectedMessageNoCluster);
    }

    private MonitorCalculatorDSL givenServiceThrowing(Exception exception) {
        return dslSupplier.get().givenServiceThrowing(exception);
    }
}
