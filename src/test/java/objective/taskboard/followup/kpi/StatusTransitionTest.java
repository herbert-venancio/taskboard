package objective.taskboard.followup.kpi;

import static objective.taskboard.utils.DateTimeUtils.parseDateTime;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class StatusTransitionTest {
    @Test
    public void checkDate_fullTransition() {
        StatusTransitionChain done = new StatusTransition("Done", parseDateTime("2020-01-03"), new TerminalStateTransition());
        StatusTransitionChain doing = new StatusTransition("Doing", parseDateTime("2020-01-02"), done);
        StatusTransitionChain todo = new StatusTransition("To Do", parseDateTime("2020-01-01"), doing);

        assertThat(todo.givenDate(parseDateTime("2020-01-01")).get(), is(todo));
        assertThat(todo.givenDate(parseDateTime("2020-01-02")).get(), is(doing));
        assertThat(todo.givenDate(parseDateTime("2020-01-03")).get(), is(done));
        assertThat(todo.givenDate(parseDateTime("2020-01-04")).get(), is(done));
    }
    
    @Test
    public void checkDate_withoutIntermediateTransition() {
        StatusTransitionChain done = new StatusTransition("Done", parseDateTime("2020-01-03"), new TerminalStateTransition());
        StatusTransitionChain doing = new NoDateStatusTransition("Doing", done);
        StatusTransitionChain todo = new StatusTransition("To Do", parseDateTime("2020-01-01"), doing);

        assertThat(todo.givenDate(parseDateTime("2020-01-01")).get(), is(todo));
        assertThat(todo.givenDate(parseDateTime("2020-01-02")).get(), is(todo));
        assertThat(todo.givenDate(parseDateTime("2020-01-03")).get(), is(done));
        assertThat(todo.givenDate(parseDateTime("2020-01-04")).get(), is(done));

    }
    
    @Test
    public void checkDate_openIssue() {
        StatusTransitionChain statusTransitions = new StatusTransition("To Do", parseDateTime("2020-01-01"),
                new TerminalStateTransition());

        assertThat(statusTransitions.givenDate(parseDateTime("2020-01-01")).get(), is(statusTransitions));
        assertThat(statusTransitions.givenDate(parseDateTime("2020-01-02")).get(), is(statusTransitions));
    }
    
    @Test
    public void checkDate_futureIssue() {
        StatusTransitionChain done = new StatusTransition("Done", parseDateTime("2020-01-04"), new TerminalStateTransition());
        StatusTransitionChain doing = new StatusTransition("Doing", parseDateTime("2020-01-03"), done);
        StatusTransitionChain todo = new StatusTransition("To Do", parseDateTime("2020-01-02"), doing);

        assertThat(todo.givenDate(parseDateTime("2020-01-01")).isPresent(), is(false) );
        assertThat(todo.givenDate(parseDateTime("2020-01-02")).get(), is(todo));
        assertThat(todo.givenDate(parseDateTime("2020-01-03")).get(), is(doing));
        assertThat(todo.givenDate(parseDateTime("2020-01-04")).get(), is(done));
    }

}
