package objective.taskboard.issueBuffer;

import static objective.taskboard.testUtils.DateTimeUtilSupport.date;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import objective.taskboard.data.Issue;

public class CardRepoTest {
    @Test
    public void whenPut_ShouldSaveLatestUpdatedDate() {
        CardRepo cardRepo = new CardRepo();

        Issue i1 = new Issue();
        i1.setIssueKey("a");
        i1.setRemoteIssueUpdatedDate(date(2027, 1, 1));
        cardRepo.put("a", i1);

        assertEquals(date(2027, 1, 1), cardRepo.getLastUpdatedDate().get());

        Issue i2 = new Issue();
        i2.setIssueKey("a");
        i2.setRemoteIssueUpdatedDate(date(2027, 1, 2));
        cardRepo.put("a", i2);

        assertEquals(date(2027, 1, 2), cardRepo.getLastUpdatedDate().get());

        Issue i3 = new Issue();
        i3.setIssueKey("a");
        i3.setRemoteIssueUpdatedDate(date(2028, 1, 2));
        cardRepo.put("a", i3);

        assertEquals(date(2028, 1, 2), cardRepo.getLastUpdatedDate().get());
    }

    @Test
    public void whenPutOnlyIfNewer_ShouldOnlyPutIssueIfPreviousInstanceIsOlderAndPriorityOrderDifferent() {
        CardRepo cardRepo = new CardRepo();

        Issue i1 = new Issue();
        i1.setIssueKey("a");
        i1.setStatus(42l);
        i1.setPriorityOrder(1L);
        i1.setPriorityUpdatedDate(date(2027, 1, 1));
        i1.setRemoteIssueUpdatedDate(date(2027, 1, 2));
        cardRepo.putOnlyIfNewer(i1);

        Issue i2 = new Issue();
        i2.setIssueKey("a");
        i2.setStatus(55l);
        i2.setPriorityOrder(2L);
        i2.setPriorityUpdatedDate(date(2026, 1, 1));
        i2.setRemoteIssueUpdatedDate(date(2027, 1, 1));
        cardRepo.putOnlyIfNewer(i2);

        assertEquals(42l, cardRepo.get("a").getStatus());
        assertEquals(new Long(1), cardRepo.get("a").getPriorityOrder());

        Issue i3 = new Issue();
        i3.setIssueKey("a");
        i3.setStatus(55l);
        i3.setPriorityOrder(1L);
        i3.setPriorityUpdatedDate(date(2027, 1, 2));
        i3.setRemoteIssueUpdatedDate(date(2027, 1, 2));
        cardRepo.putOnlyIfNewer(i3);

        assertEquals(42l, cardRepo.get("a").getStatus());
        assertEquals(new Long(1), cardRepo.get("a").getPriorityOrder());
    }

    
    @Test
    public void whenPutOnlyIfNewerWhereUpdateDateIsOlderButRemoteUpdateIsNewer_MakeSureCurrentPriorityIsKept() {
        CardRepo cardRepo = new CardRepo();

        Issue i1 = new Issue();
        i1.setIssueKey("a");
        i1.setStatus(42l);
        i1.setPriorityOrder(200l);
        i1.setPriorityUpdatedDate(date(2027, 1, 3));
        i1.setRemoteIssueUpdatedDate(date(2027, 1, 2));
        cardRepo.putOnlyIfNewer(i1);

        Issue i2 = new Issue();
        i2.setIssueKey("a");
        i2.setStatus(255l);
        i2.setPriorityOrder(300l);
        i2.setPriorityUpdatedDate(date(2027, 1, 1));
        i2.setRemoteIssueUpdatedDate(date(2027, 1, 3));
        cardRepo.putOnlyIfNewer(i2);

        assertEquals(255l, cardRepo.get("a").getStatus());
        assertEquals(new Long(200), cardRepo.get("a").getPriorityOrder());
    }
    
    @Test
    public void whenPutOrRemove_ShouldSaveNewData() {
        CardStorage storage = new CardStorage() {
            Map<String, Issue> m = new LinkedHashMap<>();
            Date lastRemoteUpdatedDate;
            
            @Override
            public void rollback() {}
            
            @Override
            public void commit() {
            }

            @Override
            public Date getLastRemoteUpdatedDate() {
                return lastRemoteUpdatedDate;
            }

            @Override
            public void setLastRemoteUpdatedDate(Date t) {
                this.lastRemoteUpdatedDate = t;
            }

            @Override
            public void storeIssue(String key, Issue issue) {
                m.put(key, issue);
            }

            @Override
            public void removeIssue(String key) {
                m.remove(key);
            }

            @Override
            public Map<String, Issue> issues() {
                return m;
            }

            @Override
            public Set<String> getCurrentProjects() {
                return new LinkedHashSet<>();
            }

            @Override
            public void putProjects(Set<String> currentProjects) {
            }
        };
        CardRepo cardRepo = new CardRepo(storage);
        

        Issue i1 = new Issue();
        i1.setIssueKey("a");
        i1.setStatus(42l);
        i1.setPriorityOrder(200l);
        i1.setPriorityUpdatedDate(date(2027, 1, 3));
        i1.setRemoteIssueUpdatedDate(date(2027, 1, 2));
        cardRepo.putOnlyIfNewer(i1);

        assertEquals(0, storage.issues().size());
        cardRepo.commit();
        
        assertEquals(1, storage.issues().size());
        assertEquals(date(2027, 1, 2), storage.getLastRemoteUpdatedDate());
        
        cardRepo.remove("a");
        
        assertEquals(1, storage.issues().size());
        
        cardRepo.commit();
        
        assertEquals(0, storage.issues().size());
        
        cardRepo.putOnlyIfNewer(i1);
        cardRepo.commit();
        storage.issues().remove("a");
        
        cardRepo.commit();
        assertEquals(0, storage.issues().size());
        
        cardRepo.setChanged("a");
        cardRepo.commit();
        assertEquals(1, storage.issues().size());
    }

    @Test
    public void whenPut_ShouldSetParentCardAndUpdateSubtasks() {
        CardRepo cardRepo = new CardRepo();

        Issue parent = new Issue();
        parent.setIssueKey("parent");
        parent.setRemoteIssueUpdatedDate(date(2027, 1, 1));
        cardRepo.put(parent.getIssueKey(), parent);
        assertEquals(0, cardRepo.get("parent").getSubtaskCards().size());

        Issue subtask = new Issue();
        subtask.setIssueKey("subtask");
        subtask.setParent(parent.getIssueKey());
        subtask.setRemoteIssueUpdatedDate(date(2027, 1, 2));
        assertFalse(subtask.getParentCard().isPresent());
        cardRepo.put(subtask.getIssueKey(), subtask);

        Set<Issue> subtaskCards = cardRepo.get("parent").getSubtaskCards();
        assertEquals(1, subtaskCards.size());
        Issue subtaskCard = subtaskCards.stream().findFirst().get();
        assertEquals("subtask", subtaskCard.getIssueKey());
        assertEquals(date(2027, 1, 2), subtaskCard.getRemoteIssueUpdatedDate());

        Issue parentCard = cardRepo.get("subtask").getParentCard().get();
        assertEquals("parent", parentCard.getIssueKey());
        assertEquals(date(2027, 1, 1), parentCard.getRemoteIssueUpdatedDate());

        Issue parent2 = new Issue();
        parent2.setIssueKey("parent");
        parent2.setRemoteIssueUpdatedDate(date(2027, 1, 3));
        cardRepo.put(parent2.getIssueKey(), parent2);
        Issue parentCard2 = cardRepo.get("subtask").getParentCard().get();
        assertEquals(date(2027, 1, 3), parentCard2.getRemoteIssueUpdatedDate());

        Issue subtask2 = new Issue();
        subtask2.setIssueKey("subtask");
        subtask2.setParent(parent2.getIssueKey());
        subtask2.setRemoteIssueUpdatedDate(date(2027, 1, 4));
        cardRepo.put(subtask2.getIssueKey(), subtask2);
        Set<Issue> subtaskCards2 = cardRepo.get("parent").getSubtaskCards();
        assertEquals(1, subtaskCards2.size());
        Issue subtaskCard2 = subtaskCards2.stream().findFirst().get();
        assertEquals("subtask", subtaskCard2.getIssueKey());
        assertEquals(date(2027, 1, 4), subtaskCard2.getRemoteIssueUpdatedDate());
    }
}