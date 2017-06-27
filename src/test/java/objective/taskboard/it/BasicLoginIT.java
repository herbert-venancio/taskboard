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

import org.junit.After;
import org.junit.Test;

public class BasicLoginIT extends AbstractUIIntegrationTest {
    @Test
    public void testLoginSuccessful() {
        LoginPage loginPage = LoginPage.to(webDriver);
        loginPage.login("foo", "bar");
        
        MainPage mainPage = MainPage.produce(webDriver);
        mainPage.waitUserLabelToBe("foo");
    }
    
    @After
    public void tearDown() {
        if (webDriver == null) return;
        webDriver.close();
    }
}
