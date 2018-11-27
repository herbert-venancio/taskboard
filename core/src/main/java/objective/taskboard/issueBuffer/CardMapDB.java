package objective.taskboard.issueBuffer;

import java.io.File;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.mapdb.serializer.SerializerString;

import objective.taskboard.data.Issue;

public class CardMapDB implements CardStorage {
    
    private final DB db;
    private Map<String, Object> metadataPersistence;
    private Map<String, Issue> issues;

    public CardMapDB(File file) {
        db = DBMaker.fileDB(file).transactionEnable().closeOnJvmShutdown().make();
        metadataPersistence = getStore("metadata");
        issues = getStore("cards");
    }

    @Override
    public void commit() {
        db.commit();
    }

    @Override
    public void rollback() {
        db.rollback();
    }

    @Override
    public Date getLastRemoteUpdatedDate() {
        return (Date) metadataPersistence.get("lastRemoteUpdatedDate");
    }

    @Override
    public void setLastRemoteUpdatedDate(Date t) {
        metadataPersistence.put("lastRemoteUpdatedDate", t);
    }

    @SuppressWarnings({ "unchecked" })
    private <T> Map<String, T> getStore(String name) {
        return db.hashMap(name).valueSerializer(Serializer.JAVA).keySerializer(new SerializerString()).createOrOpen();
    }

    @Override
    public void storeIssue(String key, Issue issue) {
        issues.put(key, issue);
    }

    @Override
    public void removeIssue(String key) {
        issues.remove(key);
    }

    @Override
    public Map<String, Issue> issues() {
        return issues;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<String> getCurrentProjects() {
        return (Set<String>) metadataPersistence.get("currentProjects");        
    }

    @Override
    public void putProjects(Set<String> currentProjects) {
        Set<String> m = getCurrentProjects();
        if (m == null)
            metadataPersistence.put("currentProjects", new LinkedHashSet<>(currentProjects));
        else 
            m.addAll(currentProjects);
    }
}
