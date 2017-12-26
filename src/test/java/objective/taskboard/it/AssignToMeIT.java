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
package objective.taskboard.it;

import org.junit.Test;

public class AssignToMeIT extends AuthenticatedIntegrationTest {
    
    @Test
    public void whenAssignToMeIsClicked_ShouldUpdateIssueImmediatlyWithAssignedUser(){
        MainPage mainPage = MainPage.produce(webDriver);
        mainPage.errorToast().close();
        mainPage.issue("TASKB-625")
            .click()
            .issueDetails()
            .assignToMe()
            .assertAssigneeIs("foo")
            .closeDialog();
        
        mainPage.issue("TASKB-625").assertHasFirstAssignee();
    }
}
