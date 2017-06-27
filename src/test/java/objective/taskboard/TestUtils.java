package objective.taskboard;

import java.io.IOException;

import org.apache.commons.io.IOUtils;

public class TestUtils {
    public static String loadResource(Class<?> klass, String path){
        try {
            return IOUtils.toString(klass.getResourceAsStream(path), "UTF-8");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
