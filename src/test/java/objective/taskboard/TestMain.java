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
package objective.taskboard;

import java.sql.SQLException;
import javax.sql.DataSource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

import objective.taskboard.driver.H2DriverNoCommit;

@EnableScheduling
@SpringBootApplication
public class TestMain {
    @Bean
    public DataSource targetDataSource() throws SQLException {
        TransactionAwareDataSourceProxy transactionAwareDataSourceProxy = new TransactionAwareDataSourceProxy();
        SingleConnectionDataSource singleConn = new SingleConnectionDataSource();
        singleConn.setUrl("jdbc:h2-no-commit:file:./db2;trace_level_file=3");
        singleConn.setDriverClassName(H2DriverNoCommit.class.getName());
        singleConn.setPassword("");
        singleConn.setUsername("sa");
        transactionAwareDataSourceProxy.setTargetDataSource(singleConn);
        return transactionAwareDataSourceProxy;
    }
    
    public static void main(String[] args) {
        System.setProperty("spring.datasource.data", "classpath:/populate_db.sql");
        JiraMockServer.begin();
        SpringApplication.run(TestMain.class, new String[]{"--server.port=8900"});
    }    
}