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

import objective.taskboard.RequestBuilder;

public class IssueBufferUpdateRefreshDialogIT extends AuthenticatedIntegrationTest {

    @Test
    public void givenChildrenNotVisibleBecauseOfParent_whenDeleteTheIssueLink_thenChildrenAndRefreshToastShouldShowUp() {
        MainPage.produce(webDriver)
            .typeSearch("TASKB-628").assertVisibleIssues()
            .typeSearch("TASKB-630").assertVisibleIssues()
            .typeSearch("TASKB-633").assertVisibleIssues()
            .typeSearch("TASKB-634").assertVisibleIssues()
            .clearSearch().errorToast().close();

        JiraMockController.enableSearchAfterInit();
        forceUpdateIssueBuffer();

        MainPage mainPage = MainPage.produce(webDriver);
        String[] updatedIssues = {"TASKB-630", "TASKB-633", "TASKB-634"};
        mainPage.assertUpdatedIssues(updatedIssues);
        mainPage.refreshToast().assertVisible().showOnlyUpdated();
        mainPage.assertVisibleIssues(updatedIssues);
        mainPage.refreshToast().dismiss();
        mainPage.lane("Demand").boardStep("Open").assertIssueList("TASKB-20");
        MainPage typeSearch = mainPage.typeSearch("TASKB-628");
		typeSearch.assertVisibleIssues();
    }

    private void forceUpdateIssueBuffer() {
        RequestBuilder.url(getSiteBase() + "/test/force-update-issue-buffer")
            .credentials("foo", "bar").get();
    }
}
