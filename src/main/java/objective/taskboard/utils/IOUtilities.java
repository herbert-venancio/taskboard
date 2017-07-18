package objective.taskboard.utils;

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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import org.apache.commons.io.IOUtils;

public class IOUtilities {
    public static String resourceToString(String path) {
        return resourceToString(IOUtilities.class, "/"+ path);
    }
    
    public static String resourceToString(Class<?> clazz, String path) {
        InputStream inputStream = clazz.getResourceAsStream(path);
        if (inputStream == null)
            return null;
        try {
            return IOUtils.toString(inputStream, "UTF-8");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Path asPath(URL url) {
        try {
            return asPath(url.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Path asPath(URI uri) {
        String scheme = uri.getScheme();
        if (scheme.equals("file")) {
            return Paths.get(uri);
        }

        if (!scheme.equals("jar")) {
            throw new IllegalArgumentException("Cannot convert to Path: " + uri);
        }

        String s = uri.toString();
        int separator = s.indexOf("!/");
        String entryName = s.substring(separator + 2).replace("!/", "/");
        URI fileURI = URI.create(s.substring(0, separator));

        try {
            FileSystem fs;
            try {
                fs = FileSystems.getFileSystem(fileURI);
            } catch (FileSystemNotFoundException e) {
                fs = FileSystems.newFileSystem(fileURI, Collections.emptyMap());
            }
            return fs.getPath(entryName);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }

    }

    public static Path asPath(File file) {
        return file.toPath();
    }
}
