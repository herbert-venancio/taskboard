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

package objective.taskboard.followup.impl;

import static objective.taskboard.utils.ZipUtils.unzip;
import static objective.taskboard.utils.ZipUtils.zip;
import static org.apache.commons.io.FileUtils.deleteQuietly;

import objective.taskboard.followup.FollowUpTemplate;
import objective.taskboard.followup.FollowUpTemplateStorage;
import objective.taskboard.followup.FollowUpTemplateValidator;
import objective.taskboard.followup.UpdateFollowUpService;
import objective.taskboard.utils.IOUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class DefaultFollowUpTemplateStorage implements FollowUpTemplateStorage {

    private Path templateRoot = Paths.get("data/followup-templates");

    @Autowired
    private UpdateFollowUpService updateFollowUpService;

    @Override
    public FollowUpTemplate getTemplate(String path) {
        Path templatePath = templateRoot.resolve(path);
        return new FollowUpTemplate(
                resolve(templatePath, "sharedStrings-initial.xml")
                , resolve("followup-template/sharedStrings-template.xml")
                , resolve("followup-template/sharedStrings-si-template.xml")
                , resolve(templatePath, "sheet7-template.xml")
                , resolve("followup-template/sheet7-row-template.xml")
                , resolve(templatePath, "Followup-template.xlsm")
                , resolve("followup-template/table7-template.xml")
        );
    }

    @Override
    public String storeTemplate(File template, FollowUpTemplateValidator validator) throws IOException {
        return storeTemplate(new FileInputStream(template), validator);
    }

    @Override
    public String storeTemplate(InputStream stream, FollowUpTemplateValidator validator) throws IOException {
        if(!Files.exists(templateRoot))
            Files.createDirectories(templateRoot);

        Path pathFollowup = Files.createTempDirectory(templateRoot, "Followup");
        Path tempFolder = pathFollowup.resolve("temp");
        unzip(stream, tempFolder);
        try {
            validator.validate(tempFolder);
            updateFollowUpService.updateFromJiraTemplate(tempFolder, pathFollowup.resolve("sheet7-template.xml"));
            updateFollowUpService.updateSharedStringsInitial(tempFolder, pathFollowup.resolve("sharedStrings-initial.xml"));
            updateFollowUpService.deleteGeneratedFiles(tempFolder);
            zip(tempFolder, pathFollowup.resolve("Followup-template.xlsm"));
            deleteQuietly(tempFolder.toFile());
        } catch (Exception e) {
            deleteQuietly(pathFollowup.toFile());
            throw e;
        }
        return templateRoot.relativize(pathFollowup).toString();
    }

    @Override
    public void deleteFile(String templatePath) {
        Path template = templateRoot.resolve(templatePath);
        deleteQuietly(template.toFile());
    }

    private static Resource resolve(String resourceName) {
        return IOUtilities.asResource(DefaultFollowUpTemplateStorage.class.getClassLoader().getResource(resourceName));
    }

    private Resource resolve(Path templatePath, String relativePath) {
        return IOUtilities.asResource(templatePath.resolve(relativePath));
    }

}
