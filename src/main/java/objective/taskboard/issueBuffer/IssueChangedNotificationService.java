package objective.taskboard.issueBuffer;

/*-
 * [LICENSE]
 * Taskboard
 * ---
 * Copyright (C) 2015 - 2016 Objective Solutions
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

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class IssueChangedNotificationService {
  
    private List<IssueChangedListener> listeners;
    
    public IssueChangedNotificationService(List<IssueChangedListener> listeners) {
        this.listeners = listeners;
    }
    
    public void notifyUpdated(String issueKey) {
        for (IssueChangedListener listener : listeners)
            listener.onIssueUpdate(issueKey);
    }
    
    public void notifyCreated(String issueKey) {
        for (IssueChangedListener listener : listeners)
            listener.onIssueCreated(issueKey);
    }
    
    public void notifyDeleted(String issueKey) {
        for (IssueChangedListener listener : listeners)
            listener.onIssueDeleted(issueKey);
    }
    
    public interface IssueChangedListener {
        default void onIssueUpdate(String issueKey) {};
        default void onIssueCreated(String issueKey) {};
        default void onIssueDeleted(String issueKey) {};
    }

}
