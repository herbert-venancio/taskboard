package objective.taskboard.it;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.After;

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
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.safaribooks.junitattachments.CaptureFile;
import com.safaribooks.junitattachments.RecordAttachmentRule;

import objective.taskboard.RequestBuilder;
import objective.taskboard.RequestResponse;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {UIConfig.class})
public abstract class AbstractUIIntegrationTest {
    protected WebDriver webDriver;
    private static final ExecutorService service = Executors.newSingleThreadExecutor();
    private static final long TIMEOUT_IN_SECONDS = 120;
    
    @Before
    public void setup() throws InterruptedException, ExecutionException, TimeoutException {
        waitServerReady();
        
        System.setProperty("webdriver.gecko.driver", "drivers/linux/marionette/64bit/geckodriver");
        webDriver = new FirefoxDriver();
        webDriver.manage().window().setSize(new Dimension(1024,768));
    }
    
    @CaptureFile(extension = "html")
    public String capturedDom = null;

    @CaptureFile(extension = "png")
    public byte[] capturePage = null;

    @After
    public void cleanupThread() {
        if (webDriver == null) return;
        try {
            // capture the dom
            capturedDom = webDriver.getPageSource();
    
            // capture a screenshot
            if (webDriver instanceof TakesScreenshot) {
                capturePage = ((TakesScreenshot) webDriver)
                        .getScreenshotAs(OutputType.BYTES);
            }
    
            webDriver.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Rule
    public RecordAttachmentRule recordArtifactRule = new RecordAttachmentRule(this);


    private void waitServerReady() throws InterruptedException, ExecutionException, TimeoutException {
        final Future<Void> f = service.submit(() -> {  
            while (true) {
                try {
                    RequestResponse response = RequestBuilder
                        .url(getSiteBase() + "/ws/issues/issue-buffer-state")
                        .credentials("foo", "bar").get();
                    
                    if (response.responseCode < 400)
                        if (response.content.equals("ready") || response.content.equals("updating"))
                            return null;
                } catch(Exception e) {
                    // just assume 'not ready'
                }
                System.out.println("Integration tests: Server not ready... waiting");
                Thread.sleep(1000);
            }
        });
        try {
            f.get(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
        } catch(TimeoutException ex) {
            throw new IllegalStateException("Server did not come up after " + TIMEOUT_IN_SECONDS + " seconds");
        }
    }

    public static String getSiteBase(){
        return "http://localhost:8900/";
    }

}
