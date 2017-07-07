package objective.taskboard.followup;

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

import objective.taskboard.followup.impl.DefaultFollowUpFacade;
import objective.taskboard.followup.impl.DefaultFollowUpTemplateStorage;
import objective.taskboard.followup.impl.DefaultUpdateFollowUpService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class FollowUpFacadeTest {

    @Mock
    private TemplateService templateService;

    @Mock
    private FollowupDataProvider provider;

    @Spy
    private DefaultUpdateFollowUpService updateFollowUpService = new DefaultUpdateFollowUpService();

    @Spy
    @InjectMocks
    private DefaultFollowUpTemplateStorage followUpTemplateStorage = new DefaultFollowUpTemplateStorage();

    @InjectMocks
    private DefaultFollowUpFacade followUpFacade = new DefaultFollowUpFacade();

    @Test
    public void create() throws IOException {
        String templateName = "OkFollowupTemplate.xlsm";
        String projects = "TASKB,PROJ1";
        MultipartFile file = new MockMultipartFile("file", FollowUpFacadeTest.class.getResourceAsStream("OkFollowupTemplate.xlsm"));
        followUpFacade.createTemplate(templateName, projects, file);

        verify(templateService, only()).saveTemplate(any(), any(), any());
    }
}
