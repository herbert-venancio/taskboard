package objective.taskboard.utils;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

public class IOUtilities {
    public static String resourceToString(String path) {
        return resourceToString(IOUtilities.class, "/"+ path);
    }
    
    public static String resourceToString(Class<?> clazz, String path) {
        InputStream inputStream = clazz.getClassLoader().getResourceAsStream(path);
        try {
            return IOUtils.toString(inputStream, "UTF-8");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
