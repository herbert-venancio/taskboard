package objective.taskboard.followup.kpi;

import static objective.taskboard.utils.DateTimeUtils.parseDateTime;

import java.time.ZonedDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public class StatusTransitionBuilder {
    
    public enum DefaultStatus {
            OPEN("Open"), 
            TODO("To Do"), 
            DOING("Doing"), 
            TO_REVIEW("To Review"), 
            REVIEW("Review"), 
            DONE("Done");
            
            private String name;

            private DefaultStatus(String name) {
                this.name = name;
            }
        
    };
    
    private ChainDto firstChainDto = new ChainDto();
    private Map<DefaultStatus,StatusTransition> transitions = new EnumMap<>(DefaultStatus.class);
    
    public StatusTransition getTransition(DefaultStatus status) {
        if(!transitions.containsKey(status))
            throw new IllegalArgumentException(String.format("The status %s was not found on transitions. Is it correctly built?", status.name));
        return transitions.get(status);
    }
    
    public StatusTransitionBuilder addTransition(DefaultStatus transition) {
        ChainDto chainDto = new ChainDto(transition, firstChainDto);
        addChain(chainDto);
        return this;
    }
    
    public StatusTransitionBuilder addTransition(DefaultStatus transition, String date) {
        ChainDto chainDto = new ChainDto(transition, date, firstChainDto);
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

    public Optional<StatusTransition> build() {
        return firstChainDto.buildChain(transitions);
    }
    
    public StatusTransition buildOrCry() {
        Optional<StatusTransition> first = firstChainDto.buildChain(transitions);
        if(!first.isPresent())
            throw new IllegalArgumentException("Statuses misconfigured");
        return first.get();
    }
   
    
    private static class ChainDto {
        private DefaultStatus transition;
        private Optional<ZonedDateTime> date = Optional.empty();
        private Optional<ChainDto> next = Optional.empty();

        private ChainDto() {
        }

        private ChainDto(DefaultStatus transition, ChainDto next) {
            this.transition = transition;
            this.next = Optional.of(next);
        }
        
        private ChainDto(DefaultStatus transition, String date, ChainDto next) {
            this(transition, next);
            this.date = Optional.of(parseDateTime(date));
        }
        
        private Optional<StatusTransition> buildChain(Map<DefaultStatus,StatusTransition> transitions,Optional<StatusTransition> nextChain) {
            StatusTransition chain = date.isPresent() ? new DatedStatusTransition(transition.name, date.get(), nextChain) : new StatusTransition(transition.name, nextChain);
            transitions.put(transition, chain);
            return Optional.of(chain);
        }

        private Optional<StatusTransition> buildChain(Map<DefaultStatus,StatusTransition> transitions) {
            if (!next.isPresent()) {
                return Optional.empty();
            }

            Optional<StatusTransition> nextChain = next.get().buildChain(transitions);
            return buildChain(transitions, nextChain);
        }


    }
    

}
