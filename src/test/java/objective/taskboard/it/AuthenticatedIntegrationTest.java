package objective.taskboard.it;

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

import org.junit.Before;

public abstract class AuthenticatedIntegrationTest extends AbstractUIWithCoverageIntegrationTest {

    @Before
    public final void doLogin() {
        LoginPage loginPage = LoginPage.to(webDriver);
        loginPage.login("foo", "bar");

        MainPage mainPage = MainPage.produce(webDriver);
        mainPage.waitUserLabelToBe("Foo");
        mainPage.lane("Operational")
            .boardStep("Done")
            .assertIssueList(
                    "TASKB-638",
                    "TASKB-678",
                    "TASKB-679",
                    "TASKB-656",
                    "TASKB-657",
                    "TASKB-658",
                    "TASKB-660",
                    "TASKB-662"
                    );
    }

}
