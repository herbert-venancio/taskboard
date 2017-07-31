package objective.taskboard.it;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openqa.selenium.JavascriptExecutor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by herbert on 21/07/17.
 */
public class AbstractUIWithCoverageIntegrationTest extends AbstractUIIntegrationTest {

    @Rule
    public final TestName testName = new TestName();

    @After
    public void saveCoverage() {
        String coverageReport = (String) ((JavascriptExecutor)webDriver).executeScript("return JSON.stringify(window.__coverage__);");
        File f = new File("target/istanbul-reports/" + testName.getMethodName() + ".json");
        try {
            if(f.getParentFile() != null)
                f.getParentFile().mkdirs();
            f.createNewFile();
            IOUtils.write(coverageReport, new FileOutputStream(f), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
