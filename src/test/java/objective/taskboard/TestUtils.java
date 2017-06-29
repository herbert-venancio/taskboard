package objective.taskboard;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

public class TestUtils {
    public static String loadResource(Class<?> klass, String path){
        try {
            InputStream stream = klass.getResourceAsStream(path);
            if (stream == null)
                return null;
            return IOUtils.toString(stream, "UTF-8");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
