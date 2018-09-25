package objective.taskboard.followup.kpi;

import java.time.ZonedDateTime;
import java.util.Optional;

public class IssueKpiBuilder {

    private final String pKey;
    private final String type;
    private final KpiLevel level;
    private ChainDto firstChainDto = new ChainDto();

    public IssueKpiBuilder(String pKey, String type, KpiLevel level) {
        this.pKey = pKey;
        this.type = type;
        this.level = level;
    }

    public IssueKpiBuilder addChain(String status) {
        ChainDto chainDto = new ChainDto(status, firstChainDto);
        addChain(chainDto);
        return this;
    }
    
    private void addChain(ChainDto chainDto) {
        if(!firstChainDto.next.isPresent()) {
            chainDto.next = Optional.of(firstChainDto);
            firstChainDto = chainDto;
            return;
        }
            
        ChainDto lastIndex = firstChainDto;
        ChainDto before;
        do {
            before = lastIndex;
            lastIndex = lastIndex.next.get();
        } while(lastIndex.next.isPresent());
        chainDto.next = Optional.of(lastIndex);
        before.next = Optional.of(chainDto);
    }

    public IssueKpiBuilder addChain(String status, ZonedDateTime date) {
        ChainDto chainDto = new ChainDto(status, date, firstChainDto);
        addChain(chainDto);
        return this;
    }

    public IssueKpi build() {
        StatusTransitionChain firstChain = firstChainDto.buildChain();
        return new IssueKpi(pKey, type, level, firstChain);
    }

    private static class ChainDto {
        private String status;
        private Optional<ZonedDateTime> date = Optional.empty();
        private Optional<ChainDto> next = Optional.empty();

        private ChainDto() {
        }

        private ChainDto(String status, ChainDto next) {
            this.status = status;
            this.next = Optional.of(next);
        }

        private StatusTransitionChain buildChain(StatusTransitionChain nextChain) {
            if (date.isPresent())
                return new StatusTransition(status, date.get(), nextChain);
            return new NoDateStatusTransition(status, nextChain);
        }

        private StatusTransitionChain buildChain() {
            if (!next.isPresent()) {
                return new TerminalStateTransition();
            }

            StatusTransitionChain nextChain = next.get().buildChain();
            return buildChain(nextChain);
        }

        public ChainDto(String status, ZonedDateTime date, ChainDto next) {
            this(status, next);
            this.date = Optional.of(date);
        }
        
        @Override
        public String toString() {
            return status;
        }

    }

}
