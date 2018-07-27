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

    @Override
    public StatusTransitionChain find(String status) {
        return this;
    }
    
    @Override
    public Optional<ZonedDateTime> getDate() {
        return Optional.empty();
    }

}
