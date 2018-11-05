package objective.taskboard.followup.kpi.enviroment;

import static objective.taskboard.utils.DateTimeUtils.parseDateTime;

import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

import org.hamcrest.Matchers;
import org.junit.Assert;

import objective.taskboard.followup.kpi.DatedStatusTransition;
import objective.taskboard.followup.kpi.StatusTransition;

public class StatusTransitionBuilder {
     
    private ChainDto firstChainDto = new ChainDto();
    private Map<DefaultStatus,StatusTransition> transitions = new LinkedHashMap<>();
    
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
    
    public Map<String,ZonedDateTime> getReversedTransitions() {
        LinkedList<ChainDto> reversedOrder = new LinkedList<>();
        
        
        ChainDto currentIndex = firstChainDto;
        while(currentIndex.next.isPresent()) {
           reversedOrder.push(currentIndex);
           currentIndex = currentIndex.next.get();
        }
        
        Map<String,ZonedDateTime> transitions = new LinkedHashMap<>();
        for (ChainDto chainDto : reversedOrder) {
            transitions.put(chainDto.transition.name, chainDto.date.orElse(null));
        }
        
        return transitions;
    }
    
    public Long lastTransitionStatusId() {
        
        ChainDto currentIndex = firstChainDto;
        Optional<ChainDto> lastOne = Optional.empty();
        while(currentIndex.next.isPresent()) {
           
           if(currentIndex.date.isPresent()) {
               ZonedDateTime currentDate = currentIndex.date.get();
               boolean isLastOne = lastOne.map( l-> !l.date.get().isAfter(currentDate)).orElse(true);
               if(isLastOne)
                   lastOne = Optional.of(currentIndex);
           }
           currentIndex = currentIndex.next.get();
        }
        
        return lastOne.map(l -> l.transition.id).orElse(0l);
    }
    
    public String[] getReversedStatusOrder() {
        LinkedList<String> reversedOrder = new LinkedList<>();
        ChainDto currentIndex = firstChainDto;
        
        while(currentIndex.next.isPresent()) {
            reversedOrder.push(currentIndex.transition.name);
            currentIndex = currentIndex.next.get();
        }
        
        return reversedOrder.toArray(new String[0]);
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
    
    public void assertEffort(DefaultStatus status, Long effort) {
        Assert.assertTrue(transitions.containsKey(status));
        Assert.assertThat(transitions.get(status).getEffort(), Matchers.is(effort));
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
            StatusTransition chain = date.isPresent() ? new DatedStatusTransition(transition.name, date.get(),transition.isProgressingStatus, nextChain) : new StatusTransition(transition.name, transition.isProgressingStatus,nextChain);
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
    
    public static class DefaultStatus {
        public Long id;
        public String name;
        public boolean isProgressingStatus;

        public DefaultStatus(Long id, String name, boolean isProgressingStatus) {
            this(name,isProgressingStatus);
            this.id = id;
        }
        
        public DefaultStatus(String name, boolean isProgressingStatus) {
            this.name = name;
            this.isProgressingStatus = isProgressingStatus;
        }
    }

}
