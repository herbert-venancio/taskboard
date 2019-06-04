package objective.taskboard.driver;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.h2.Driver;
import org.h2.message.DbException;

public class H2DriverNoCommit extends Driver {

    static {
        try {
            DriverManager.registerDriver(new H2DriverNoCommit());
        } catch (SQLException e) {
            DbException.traceThrowable(e);
        }
    }

    @Override
    public Connection connect(String url, Properties info) {
        if (!url.contains("h2-no-commit"))
            return null;

        try {
            final Connection delegate = super.connect(url.replace("h2-no-commit", "h2"), info);
            delegate.setAutoCommit(false);
            return (Connection) Proxy.newProxyInstance(
                    H2DriverNoCommit.class.getClassLoader()
                    , new Class<?>[] {Connection.class}
                    , (proxy, method, args) -> {
                        if ("setAutoCommit".equals(method.getName()))
                            return null;
                        if ("commit".equals(method.getName()))
                            return null;
                        if ("rollback".equals(method.getName()) && args == null)
                            return null;

                        return method.invoke(delegate, args);
                    });
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
