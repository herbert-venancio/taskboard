package objective.taskboard.it;

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

import java.io.IOException;

import org.junit.Test;

import objective.taskboard.RequestBuilder;
import objective.taskboard.TestUtils;

public class WebhookUpdateRefreshDialogIT extends AuthenticatedIntegrationTest {
    @Test
    public void whenUpdateHappensViaWebHook_RefreshToastShouldShowUP() throws IOException {
        // emulate a remote change
        RequestBuilder
            .url("http://localhost:4567/rest/api/latest/issue/TASKB-625")
            .body("{\"fields\":{\"assignee\":{\"name\":\"foo\"}},\"properties\":[]}")
            .put();
        
        MainPage mainPage = MainPage.produce(webDriver);
        String body = TestUtils.loadResource(getClass(), "/webhook/TASKB_625_updatePayload.json");
        RequestBuilder
            .url(getSiteBase()+"/webhook/TASKB")
            .header("Content-Type", "application/json")
            .body(body)
            .post();
        
        mainPage.typeSearch("TASKB-61");
        mainPage.refreshToast().assertVisible();
        mainPage.refreshToast().toggleShowHide();
        mainPage.assertVisibleIssues("TASKB-625");
        mainPage.refreshToast().toggleShowHide();
        mainPage.assertVisibleIssues("TASKB-610", "TASKB-611", "TASKB-612", "TASKB-613");
        mainPage.refreshToast().toggleShowHide();
        mainPage.refreshToast().dismiss();
        mainPage.refreshToast().assertNotVisible();
        mainPage.assertVisibleIssues("TASKB-610", "TASKB-611", "TASKB-612", "TASKB-613");
    }
}
