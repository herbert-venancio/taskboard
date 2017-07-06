/*-
 * [LICENSE]
 * Taskboard
 * ---
 * Copyright (C) 2015 - 2017 Objective Solutions
 * ---
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * [/LICENSE]
 */
package objective.taskboard.driver;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.h2.Driver;
import org.h2.message.DbException;

public class H2DriverNoCommit extends Driver {
    private static final Driver INSTANCE = new H2DriverNoCommit();

    static {
        try {
            DriverManager.registerDriver(INSTANCE);
        } catch (SQLException e) {
            DbException.traceThrowable(e);
        }
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        if (!url.contains("h2-no-commit")) 
            return null;
        
        Class<?> proxyClass = Proxy.getProxyClass(getClass().getClassLoader(), new Class[]{Connection.class});
        try {
            final Connection delegate = super.connect(url.replace("h2-no-commit","h2"), info);
            return (Connection)proxyClass.getConstructor(new Class[]{InvocationHandler.class}).newInstance(new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    if (method.getName().equals("commit")) {
                        System.out.println("=========== skipping commit() to preserve savepoint ===============");
                        return null;
                    }
                    
                    return method.invoke(delegate, args);
                }});
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
