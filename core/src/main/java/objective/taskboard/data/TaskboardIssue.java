package objective.taskboard.data;

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

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
public class TaskboardIssue implements Serializable {
    private static final long serialVersionUID = -1772950366555561419L;
    

    @Id
    private String issueKey;
    
    @Column
    private long priority;
    
    @Column(updatable=false)
    @CreationTimestamp
    private Date created;
    
    @Column
    @UpdateTimestamp
    private Date updated;

    public TaskboardIssue(String issueKey, long priority) {
        this.issueKey = issueKey;
        this.priority = priority;
    }
    
    public TaskboardIssue(){ }

    
    public long getPriority() {
        return priority;
    }
    
    public void setPriority(long priority) {
        this.priority = priority;
    }
    
    public String getIssueKey() {
        return issueKey;
    }
    
    public void setIssueKey(String issueKey) {
        this.issueKey = issueKey;
    }
    
    public Date getUpdated() {
        return updated;
    }
}
