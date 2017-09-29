package objective.taskboard.issueBuffer;

import static java.time.LocalDate.of;
import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;

import objective.taskboard.data.Issue;

public class CardRepoTest {
    @Test
    public void whenPut_ShouldSaveLatestUpdatedDate() {
        CardRepo cardRepo = new CardRepo();

        Issue i1 = new Issue();
        i1.setRemoteIssueUpdatedDate(new Date(of(2027, 1, 1).toEpochDay()));
        cardRepo.put("a", i1);

        assertEquals(new Date(of(2027, 1, 1).toEpochDay()), cardRepo.getLastUpdatedDate().get());

        Issue i2 = new Issue();
        i2.setRemoteIssueUpdatedDate(new Date(of(2027, 1, 2).toEpochDay()));
        cardRepo.put("a", i2);

        assertEquals(new Date(of(2027, 1, 2).toEpochDay()), cardRepo.getLastUpdatedDate().get());

        Issue i3 = new Issue();
        i3.setRemoteIssueUpdatedDate(new Date(of(2028, 1, 2).toEpochDay()));
        cardRepo.put("a", i3);

        assertEquals(new Date(of(2028, 1, 2).toEpochDay()), cardRepo.getLastUpdatedDate().get());
    }

    @Test
    public void whenPutOnlyIfNewer_ShouldOnlyPutIssueIfPreviousInstanceIsOlder() {
        CardRepo cardRepo = new CardRepo();

        Issue i1 = new Issue();
        i1.setStatus(42l);
        i1.setUpdatedDate(new Date(of(2027, 1, 1).toEpochDay()));
        i1.setRemoteIssueUpdatedDate(new Date(of(2027, 1, 2).toEpochDay()));
        cardRepo.putOnlyIfNewer("a", i1);

        Issue i2 = new Issue();
        i2.setStatus(55l);
        i2.setUpdatedDate(new Date(of(2026, 1, 1).toEpochDay()));
        i2.setRemoteIssueUpdatedDate(new Date(of(2027, 1, 2).toEpochDay()));
        cardRepo.putOnlyIfNewer("a", i2);

        assertEquals(i1, cardRepo.get("a"));
    }

    @Test
    public void whenPutOnlyIfNewerWhereUpdateDateIsOlderButRemoteUpdateIsNewer_MakeSureCurrentPriorityIsKept() {
        CardRepo cardRepo = new CardRepo();

        Issue i1 = new Issue();
        i1.setPriorityOrder(200l);
        i1.setStatus(42l);
        i1.setUpdatedDate(new Date(of(2027, 1, 3).toEpochDay()));
        i1.setRemoteIssueUpdatedDate(new Date(of(2027, 1, 2).toEpochDay()));
        cardRepo.putOnlyIfNewer("a", i1);

        Issue i2 = new Issue();
        i2.setStatus(255l);
        i2.setPriorityOrder(300l);
        i2.setUpdatedDate(new Date(of(2027, 1, 1).toEpochDay()));
        i2.setRemoteIssueUpdatedDate(new Date(of(2027, 1, 3).toEpochDay()));
        cardRepo.putOnlyIfNewer("a", i2);

        assertEquals(255l, cardRepo.get("a").getStatus());
        assertEquals(new Long(200), cardRepo.get("a").getPriorityOrder());
    }
}