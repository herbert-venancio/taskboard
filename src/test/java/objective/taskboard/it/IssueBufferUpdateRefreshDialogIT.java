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
            .clearSearch();

        JiraMockController.enableSearchAfterInit();
        forceUpdateIssueBuffer();

        MainPage mainPage = MainPage.produce(webDriver);
        mainPage.errorToast().close();
        mainPage.refreshToast().assertVisible();
        mainPage.typeSearch("TASKB-628").assertVisibleIssues()
            .clearSearch();
        mainPage.lane("Deployable").boardStep("Open").assertIssueList("TASKB-236", "TASKB-630", "TASKB-640");
        mainPage.lane("Operational").boardStep("Open").assertIssueList("TASKB-633", "TASKB-634", "TASKB-647");
        mainPage.refreshToast().toggleShowHide();
        mainPage.assertVisibleIssues("TASKB-630", "TASKB-633", "TASKB-634");
    }

    private void forceUpdateIssueBuffer() {
        RequestBuilder.url(getSiteBase() + "/test/force-update-issue-buffer")
            .credentials("foo", "bar").get();
    }

}
