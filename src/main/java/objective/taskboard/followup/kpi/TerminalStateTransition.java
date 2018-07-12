package objective.taskboard.followup.kpi;

import java.time.ZonedDateTime;
import java.util.Optional;

public class TerminalStateTransition implements StatusTransitionChain{

    @Override
    public Optional<StatusTransitionChain> givenDate(ZonedDateTime date) {
        return Optional.empty();
    }

    @Override
    public boolean isWithinDate(ZonedDateTime date) {
        return false;
    }

    @Override
    public boolean isStatus(String status) {
        return false;
    }
    
}
