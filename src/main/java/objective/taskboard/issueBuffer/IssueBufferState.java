package objective.taskboard.issueBuffer;

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

public enum IssueBufferState {
    uninitialised(false),
    initialising(false),
    initialisationError(false),
    requiresReindex(true),
    updating(true),
    ready(true),
    updateError(true);
    
    private boolean initialized;

    private IssueBufferState(boolean initialized) {
        this.initialized = initialized;
    }
    
    public boolean isInitialized() {
        return initialized;
    }
    
    public IssueBufferState start() {
        if (this == uninitialised || this == initialisationError) 
            return initialising;
         
        return updating;
    }
    
    public IssueBufferState done() {
        return ready;
    }
    
    public IssueBufferState error() {
        if (this == updating) 
            return updateError;
         
        return initialisationError;
    }
}
