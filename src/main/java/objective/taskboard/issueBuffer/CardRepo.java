package objective.taskboard.issueBuffer;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import objective.taskboard.data.Issue;

public class CardRepo  {
    private static final Logger log = LoggerFactory.getLogger(CardRepo.class);
    
    private CardStorage db;
    private Date lastRemoteUpdatedDate;
    private Map<String, Issue> cardByKey = new ConcurrentHashMap<>();
    private Set<String> unsavedCards = new HashSet<>();
    private Set<String> currentProjects = new HashSet<>();
       
    public CardRepo(CardStorage repodb) {
        this.db = repodb;
        if (repodb.issues().size() > 0) 
            cardByKey.putAll(repodb.issues());
        
        this.lastRemoteUpdatedDate = repodb.getLastRemoteUpdatedDate();
        this.currentProjects = repodb.getCurrentProjects();
    }

    public CardRepo() {
    }

    public Issue get(String key) {
        return cardByKey.get(key);
    }
    
    public synchronized boolean putOnlyIfNewer(Issue newValue) {
        Issue current = cardByKey.get(newValue.getIssueKey());
        if (current == null) {
            put(newValue.getIssueKey(), newValue);
            return true;
        }
        
        if (current.getPriorityUpdatedDate().after(newValue.getPriorityUpdatedDate()) &&
            current.getRemoteIssueUpdatedDate().after(newValue.getRemoteIssueUpdatedDate()))
            return false;
        
        if (current.getPriorityUpdatedDate().after(newValue.getPriorityUpdatedDate())) {
            newValue.setPriorityOrder(current.getPriorityOrder());
            newValue.setPriorityUpdatedDate(current.getPriorityUpdatedDate());
        }
        
        put(newValue.getIssueKey(), newValue);
        return true;
    }
    
    synchronized void put(String key, Issue value) {
        if (lastRemoteUpdatedDate == null)
            lastRemoteUpdatedDate = value.getRemoteIssueUpdatedDate();
        
        if (lastRemoteUpdatedDate.before(value.getRemoteIssueUpdatedDate()))
            lastRemoteUpdatedDate = value.getRemoteIssueUpdatedDate();
        
        if (cardByKey.containsKey(key)) 
            value.getSubtaskCards().addAll(cardByKey.get(key).getSubtaskCards());
        
        cardByKey.put(key, value);
        unsavedCards.add(key);
        addProject(value.getProjectKey());
    }
    
    public Optional<Date> getLastUpdatedDate() {
        return Optional.ofNullable(lastRemoteUpdatedDate);
    }
    
    public synchronized Optional<Set<String>> getCurrentProjects() {
        return Optional.ofNullable(currentProjects);
    }

    private void addProject(String projectKey) {
        if (currentProjects == null)
            currentProjects = new HashSet<>();
        currentProjects.add(projectKey);
    }    

    public synchronized void clear() {
        cardByKey.clear();
        lastRemoteUpdatedDate = null;
    }

    public synchronized Issue remove(String key) {
        unsavedCards.add(key);
        return cardByKey.remove(key);
    }

    public int size() {
        return cardByKey.size();
    }

    public Collection<Issue> values() {
        return cardByKey.values();
    }

    public Collection<String> keySet() {
        return cardByKey.keySet();
    }

    public synchronized void commit() {
        if (db == null)
            return;
        
        if (unsavedCards.size() == 0)
            return;
        
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        int unsavedIssueCount = unsavedCards.size();
        try {
            for (String key : unsavedCards) 
                if (cardByKey.get(key) == null)
                    db.removeIssue(key);
                else
                    db.storeIssue(key, cardByKey.get(key));
            
            db.setLastRemoteUpdatedDate(this.lastRemoteUpdatedDate);
            db.putProjects(currentProjects);
            db.commit();
            unsavedCards.clear();
        }catch(Exception e) {
            log.error("Failed to load save issues", e);
            db.rollback();
            return;
        }

        log.info("Data written in " + stopWatch.getTime() + " ms. Stored issues: " + unsavedIssueCount);
    }
    
    public synchronized void setChanged(String issueKey) {
        unsavedCards.add(issueKey);
    }
}
