package objective.taskboard.issueBuffer;

/*-
 * [LICENSE]
 * Taskboard
 * - - -
 * Copyright (C) 2015 - 2016 Objective Solutions
 * - - -
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

public enum WebhookEvent {

    ISSUE_CREATED(Category.ISSUE),
    ISSUE_UPDATED(Category.ISSUE),
    ISSUE_DELETED(Category.ISSUE),
    WORKLOG_UPDATED(Category.ISSUE),
    VERSION_CREATED(Category.VERSION),
    VERSION_UPDATED(Category.VERSION),
    VERSION_DELETED(Category.VERSION);

    public final Category category;

    private WebhookEvent(Category type) {
        this.category = type;
    }

    public boolean isTypeVersion() {
        return category == Category.VERSION;
    }

    public enum Category {
        ISSUE
        , VERSION
        }
}
