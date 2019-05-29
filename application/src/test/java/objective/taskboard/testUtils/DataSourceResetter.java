package objective.taskboard.testUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayDeque;
import java.util.Deque;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;

import org.hibernate.internal.SessionImpl;
import org.javers.repository.sql.JaversSqlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class DataSourceResetter {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager txManager;

    @Autowired
    private JaversSqlRepository javersSqlRepository;

    private Deque<Savepoint> savepointStack = new ArrayDeque<>();

    @PostConstruct
    private void ensureJaversSchema() {
        TransactionTemplate tmpl = new TransactionTemplate(txManager);
        tmpl.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                javersSqlRepository.ensureSchema();
            }
        });
    }

    public void savepoint() {
        try {
            savepointStack.push(getConnection().setSavepoint());
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public void rollback() {
        if (savepointStack.isEmpty())
            return;
        try {
            getConnection().rollback(savepointStack.pop());
            javersSqlRepository.evictCache();
            javersSqlRepository.evictSequenceAllocationCache();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private Connection getConnection() {
        return ((SessionImpl) entityManager.getDelegate()).connection();
    }
}
