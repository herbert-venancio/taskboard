package objective.taskboard.issueBuffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import objective.taskboard.data.Issue;
import objective.taskboard.jira.JiraProperties;
import objective.taskboard.jira.MetadataService;
import objective.taskboard.utils.LocalDateTimeProviderInterface;

public class CardRepo implements Serializable {
    private static final long serialVersionUID = 61443536083475176L;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IssueBufferService.class);
    
    private Date lastRemoteUpdatedDate;
    private ConcurrentHashMap<String, Issue> cardByKey = new ConcurrentHashMap<>();
    
    public Issue get(String key) {
        return cardByKey.get(key);
    }
    
    public Issue putOnlyIfNewer(String key, Issue value) {
        Issue old = cardByKey.get(key);
        if (old == null)
            return put(key, value);
        
        if (old.getUpdatedDate().after(value.getUpdatedDate())) {
            if (old.getRemoteIssueUpdatedDate().before(value.getRemoteIssueUpdatedDate())) {
                value.setPriorityOrder(old.getPriorityOrder());
                value.setUpdatedDate(old.getUpdatedDate());
                
                return put(key, value);
            }
            return old;
        }
        
        return put(key, value);
    }
    
    Issue put(String key, Issue value) {
        if (lastRemoteUpdatedDate == null)
            lastRemoteUpdatedDate = value.getRemoteIssueUpdatedDate();
        
        if (lastRemoteUpdatedDate.before(value.getRemoteIssueUpdatedDate()))
            lastRemoteUpdatedDate = value.getRemoteIssueUpdatedDate();
        return cardByKey.put(key, value);
    }
    
    public Optional<Date> getLastUpdatedDate() {
        return Optional.ofNullable(lastRemoteUpdatedDate);
    }

    public void clear() {
        cardByKey.clear();
    }

    public Issue remove(String key) {
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

    public static Optional<CardRepo> from(
            File cache, 
            JiraProperties jiraProperties, 
            MetadataService metaDataService, 
            LocalDateTimeProviderInterface localDateTimeProvider) {
        
        if (!cache.exists())
            return Optional.empty();
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(cache))) {
            CardRepo repo = (CardRepo)ois.readObject();
            repo.values().stream().forEach(c-> {
                c.setJiraProperties(jiraProperties);
                c.setMetaDataService(metaDataService);
                c.setLocalDateTimeProvider(localDateTimeProvider);
                c.setParentCard(repo.get(c.getParent()));
            });
            return Optional.of(repo);
        } catch (ClassNotFoundException | IOException e) {
            log.warn("Could not load cache. Removing and ignoring", e);
            cache.delete();
        }
        return Optional.empty();
    }

    public void writeTo(File cache) {
        try (ObjectOutputStream ois = new ObjectOutputStream(new FileOutputStream(cache))) {
            ois.writeObject(this);
        } catch (IOException e) {
            log.warn("Could not write cache", e);
            cache.delete();
        }
    }
}
