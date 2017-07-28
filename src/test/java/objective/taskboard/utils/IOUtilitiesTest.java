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
import org.junit.Test;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import static objective.taskboard.utils.IOUtilities.ENCODE_UTF_8;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class IOUtilitiesTest {

    @Test
    public void asResource_fromURL() throws IOException {
        URL jar = IOUtilitiesTest.class.getResource("test.jar");
        Resource resource = IOUtilities.asResource(new URL("jar:" + jar.toString() + "!/test/res/file1.txt"));
        String content = IOUtils.toString(resource.getInputStream(), ENCODE_UTF_8);
        assertThat(content, is("File 1 content"));
    }

    @Test
    public void asResource_fromFile() throws IOException {
        File file = new File("target/test-classes/objective/taskboard/utils/file2.txt");
        Resource resource = IOUtilities.asResource(file);
        String content = IOUtils.toString(resource.getInputStream(), ENCODE_UTF_8);
        assertThat(content, is("File 2 content"));
    }

    @Test
    public void asResource_fromPath() throws IOException {
        Path path = Paths.get("target/test-classes/objective/taskboard/utils/file1.txt");
        Resource resource = IOUtilities.asResource(path);
        String content = IOUtils.toString(resource.getInputStream(), ENCODE_UTF_8);
        assertThat(content, is("File 1 content"));
    }
}
