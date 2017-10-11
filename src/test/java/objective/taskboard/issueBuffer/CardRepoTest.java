package objective.taskboard.issueBuffer;

import static objective.taskboard.testUtils.DateTimeUtilSupport.date;
import static org.junit.Assert.assertEquals;

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
        i1.setRemoteIssueUpdatedDate(date(2027, 1, 1));
        cardRepo.put("a", i1);

        assertEquals(date(2027, 1, 1), cardRepo.getLastUpdatedDate().get());

        Issue i2 = new Issue();
        i2.setRemoteIssueUpdatedDate(date(2027, 1, 2));
        cardRepo.put("a", i2);

        assertEquals(date(2027, 1, 2), cardRepo.getLastUpdatedDate().get());

        Issue i3 = new Issue();
        i3.setRemoteIssueUpdatedDate(date(2028, 1, 2));
        cardRepo.put("a", i3);

        assertEquals(date(2028, 1, 2), cardRepo.getLastUpdatedDate().get());
    }

    @Test
    public void whenPutOnlyIfNewer_ShouldOnlyPutIssueIfPreviousInstanceIsOlder() {
        CardRepo cardRepo = new CardRepo();

        Issue i1 = new Issue();
        i1.setIssueKey("a");
        i1.setStatus(42l);
        i1.setPriorityUpdatedDate(date(2027, 1, 1));
        i1.setRemoteIssueUpdatedDate(date(2027, 1, 2));
        cardRepo.putOnlyIfNewer(i1);

        Issue i2 = new Issue();
        i2.setIssueKey("a");
        i2.setStatus(55l);
        i2.setPriorityUpdatedDate(date(2026, 1, 1));
        i2.setRemoteIssueUpdatedDate(date(2027, 1, 1));
        cardRepo.putOnlyIfNewer(i2);

        assertEquals(i1, cardRepo.get("a"));
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
}