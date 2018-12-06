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

import org.junit.Test;

public class HierarchicalFilterIT extends AuthenticatedIntegrationTest {

    @Test
    public void whenIssueFilterIsEnabled_OnlyIssueAndItsChildrenAndParentShowUp() {
        MainPage mainPage = MainPage.produce(webDriver);
        mainPage.issue("TASKB-606").enableHierarchicalFilter();
        mainPage.assertVisibleIssues("TASKB-606", "TASKB-186", "TASKB-235", "TASKB-601", "TASKB-572");
    }

    @Test
    public void whenMiddleLaneIsDisabled_filterStillWorking() {
        MainPage mainPage = MainPage.produce(webDriver);
        mainPage.openMenuFilters().toggleLaneVisibilityAndReload("Deployable", mainPage);
        mainPage.issue("TASKB-606").enableHierarchicalFilter();
        mainPage.assertVisibleIssues("TASKB-606", "TASKB-601", "TASKB-572");
    }

}
