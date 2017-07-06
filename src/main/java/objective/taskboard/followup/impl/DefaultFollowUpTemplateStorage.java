package objective.taskboard.followup.impl;

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

import objective.taskboard.followup.FollowUpTemplate;
import objective.taskboard.followup.FollowUpTemplateStorage;
import objective.taskboard.followup.FollowUpTemplateValidator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class DefaultFollowUpTemplateStorage implements FollowUpTemplateStorage {

    private Path templateRoot = Paths.get("uploaded-templates");

    @Override
    public FollowUpTemplate getDefaultTemplate() {
        return new FollowUpTemplate(
                resolve("followup-template/sharedStrings-initial.xml")
                , resolve("followup-template/sharedStrings-template.xml")
                , resolve("followup-template/sharedStrings-si-template.xml")
                , resolve("followup-template/sheet7-template.xml")
                , resolve("followup-template/sheet7-row-template.xml")
                , resolve("followup-template/Followup-template.xlsm")
                , resolve("followup-template/table7-template.xml")
        );
    }

    @Override
    public FollowUpTemplate getTemplate(String path) {
        return null;
    }

    @Override
    public String storeTemplate(File template, FollowUpTemplateValidator validator){
        return null;
    }

    @Override
    public String storeTemplate(InputStream stream, FollowUpTemplateValidator validator) throws IOException {
        if(!Files.exists(templateRoot))
            Files.createDirectory(templateRoot);
        Path pathFollowup = Files.createTempDirectory(templateRoot, "Followup");
        Path tempFolder = decompressTemplate(pathFollowup, stream);
        try {
            validator.validate(tempFolder);
        } catch (Exception e) {
            FileUtils.deleteQuietly(pathFollowup.toFile());
            throw e;
        }
        return templateRoot.relativize(pathFollowup).toString();
    }

    // ---

    private static URL resolve(String resourceName) {
        return DefaultFollowUpTemplateStorage.class.getClassLoader().getResource(resourceName);
    }

    private Path decompressTemplate(Path pathFollowup, InputStream stream) throws IOException {
        Path temp = pathFollowup.resolve("temp");

        ZipInputStream zipInputStream = null;
        try {
            zipInputStream = new ZipInputStream(stream);

            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                Path entryPath = temp.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                    continue;
                } else {
                    Files.createDirectories(entryPath.getParent());
                }
                Files.copy(zipInputStream, entryPath);
            }

            return temp;
        } finally {
            if (zipInputStream != null)
                IOUtils.closeQuietly(zipInputStream);
        }
    }
}
