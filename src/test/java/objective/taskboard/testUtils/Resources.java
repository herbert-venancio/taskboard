package objective.taskboard.testUtils;

import objective.taskboard.utils.IOUtilities;
import org.springframework.core.io.Resource;

public class Resources {

    public static Resource resolve(String resourceName) {
        return IOUtilities.asResource(Resources.class.getClassLoader().getResource(resourceName));
    }
}
