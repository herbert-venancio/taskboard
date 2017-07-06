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
package objective.taskboard.it;

import java.sql.SQLException;
import java.sql.Savepoint;

import javax.persistence.EntityManager;

import org.hibernate.internal.SessionImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.controller.CacheRefreshController;
import objective.taskboard.controller.IssuePriorityService;
import objective.taskboard.issueBuffer.IssueBufferService;

@RestController
@RequestMapping("/test")
public class TestExtraControllers {

    @Autowired
    private IssueBufferService issueBuffer;
    
    @Autowired
    EntityManager entityManager;
    
    @Autowired
    CacheRefreshController cacheRefreshController;
    
    @Autowired
    private IssuePriorityService prioService;

    private Savepoint _savepoint;
    
    @RequestMapping("resetbuffer")
    public void resetbuffer() {
        rollback();
        cacheRefreshController.configuration();
        prioService.reset();
        issueBuffer.reset();
        savepoint();
    }
    
    @RequestMapping("savepoint")
    public void savepoint() {
        try {
            _savepoint = ((SessionImpl) entityManager.getDelegate()).connection().setSavepoint();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
    
    @RequestMapping("rollback")
    public void rollback() {
        if (_savepoint == null)
            return;
        try {
            ((SessionImpl) entityManager.getDelegate()).connection().rollback(_savepoint);
            _savepoint = null;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}
