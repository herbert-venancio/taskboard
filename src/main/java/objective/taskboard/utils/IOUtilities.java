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

package objective.taskboard.utils;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;

public class IOUtilities {
    public static final String ENCODE_UTF_8 = "UTF-8";

    public static String resourceToString(String path) {
        return resourceToString(IOUtilities.class, "/"+ path);
    }
    
    public static String resourceToString(Class<?> clazz, String path) {
        InputStream inputStream = clazz.getResourceAsStream(path);
        if (inputStream == null)
            return null;
        try {
            return IOUtils.toString(inputStream, ENCODE_UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Resource asResource(URL url) {
        return new UrlResource(url);
    }

    public static Resource asResource(File file) {
        return new FileSystemResource(file);
    }

    public static Resource asResource(Path path) {
        return new PathResource(path);
    }

    public static Resource asResource(byte[] bytes) {
        return new ByteArrayResource(bytes);
    }

    public static void write(File file, String data) throws IOException {
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(file);
            output.write(data.getBytes(ENCODE_UTF_8));
        } finally {
            if (output != null)
                output.close();
        }
    }
}
