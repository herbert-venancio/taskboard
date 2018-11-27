package objective.taskboard.issueBuffer;

import org.springframework.stereotype.Component;

@Component
public class CardRepoServiceMock implements CardRepoService {

    @Override
    public CardRepo from(String cache) {
        return new CardRepo();
    }

}
