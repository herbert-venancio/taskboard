package objective.taskboard.utils;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

/**
 * Created by herbert on 18/07/17.
 */
public class IOUtilitiesTest {

    @Test
    public void asPath_fromFile_unixStyle() {
        File file = new File("/dir/subdir");
        Path path = IOUtilities.asPath(file);
        assertThat(path, is(Paths.get("/dir/subdir")));
    }

    @Test
    public void asPath_fromFile_windowsStyle() {
        File file = new File("c:\\dir\\subdir");
        Path path = IOUtilities.asPath(file);
        assertThat(path, is(Paths.get("c:\\dir\\subdir")));
    }

    @Test
    public void asPath_fromFileUrl() throws MalformedURLException {
        URL fileUrl = new URL("file:/dir/subdir");
        Path path = IOUtilities.asPath(fileUrl);
        assertThat(path, is(Paths.get("/dir/subdir")));
    }

    @Test
    public void asPath_fromJarUrl() throws MalformedURLException {
        URL jarUrl = IOUtilitiesTest.class.getResource("test.jar");
        jarUrl = new URL("jar:" + jarUrl.toString() + "!/test/res");
        Path path = IOUtilities.asPath(jarUrl);
        assertThat(path, not(nullValue()));
    }

    @Test
    public void asPath_fromFileURI() throws URISyntaxException {
        URI fileURI = new URI("file:/dir/subdir");
        Path path = IOUtilities.asPath(fileURI);
        assertThat(path, is(Paths.get("/dir/subdir")));
    }

    @Test
    public void asPath_fromJarURI() throws URISyntaxException {
        URL jarUrl = IOUtilitiesTest.class.getResource("test.jar");
        URI jarURI = new URI("jar:" + jarUrl.toString() + "!/test/res");
        Path path = IOUtilities.asPath(jarURI);
        assertThat(path, not(nullValue()));
    }
}
