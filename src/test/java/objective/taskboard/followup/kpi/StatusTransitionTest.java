package objective.taskboard.followup.kpi;

import static objective.taskboard.followup.kpi.StatusTransitionBuilder.DefaultStatus.DOING;
import static objective.taskboard.followup.kpi.StatusTransitionBuilder.DefaultStatus.DONE;
import static objective.taskboard.followup.kpi.StatusTransitionBuilder.DefaultStatus.TODO;
import static objective.taskboard.utils.DateTimeUtils.parseDateTime;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Optional;

import org.junit.Test;

public class StatusTransitionTest {
    @Test
    public void checkDate_fullTransition() {
        StatusTransitionBuilder builder = new StatusTransitionBuilder()
                                                .addTransition(TODO, "2020-01-01")
                                                .addTransition(DOING, "2020-01-02")
                                                .addTransition(DONE, "2020-01-03");
        
        StatusTransition first = builder.buildOrCry();
                        
        assertThat(first.givenDate(parseDateTime("2020-01-01")).get(), is(builder.getTransition(TODO)));
        assertThat(first.givenDate(parseDateTime("2020-01-02")).get(), is(builder.getTransition(DOING)));
        assertThat(first.givenDate(parseDateTime("2020-01-03")).get(), is(builder.getTransition(DONE)));
        assertThat(first.givenDate(parseDateTime("2020-01-04")).get(), is(builder.getTransition(DONE)));
    }
    
    @Test
    public void checkDate_withoutIntermediateTransition() {
        
        StatusTransitionBuilder builder = new StatusTransitionBuilder()
                .addTransition(TODO, "2020-01-01")
                .addTransition(DOING)
                .addTransition(DONE, "2020-01-03");

        StatusTransition first = builder.buildOrCry();

        assertThat(first.givenDate(parseDateTime("2020-01-01")).get(), is(builder.getTransition(TODO)));
        assertThat(first.givenDate(parseDateTime("2020-01-02")).get(), is(builder.getTransition(TODO)));
        assertThat(first.givenDate(parseDateTime("2020-01-03")).get(), is(builder.getTransition(DONE)));
        assertThat(first.givenDate(parseDateTime("2020-01-04")).get(), is(builder.getTransition(DONE)));

    }
    
    @Test
    public void checkDate_openIssue() {
        StatusTransition statusTransitions = new DatedStatusTransition("To Do", parseDateTime("2020-01-01"),Optional.empty());

        assertThat(statusTransitions.givenDate(parseDateTime("2020-01-01")).get(), is(statusTransitions));
        assertThat(statusTransitions.givenDate(parseDateTime("2020-01-02")).get(), is(statusTransitions));
    }
    
    @Test
    public void checkDate_futureIssue() {
        
        StatusTransitionBuilder builder = new StatusTransitionBuilder()
                .addTransition(TODO, "2020-01-02")
                .addTransition(DOING,"2020-01-03")
                .addTransition(DONE, "2020-01-04");

        StatusTransition first = builder.buildOrCry();

        assertThat(first.givenDate(parseDateTime("2020-01-01")).isPresent(), is(false) );
        assertThat(first.givenDate(parseDateTime("2020-01-02")).get(), is(builder.getTransition(TODO)));
        assertThat(first.givenDate(parseDateTime("2020-01-03")).get(), is(builder.getTransition(DOING)));
        assertThat(first.givenDate(parseDateTime("2020-01-04")).get(), is(builder.getTransition(DONE)));
    }

}
