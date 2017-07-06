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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
public class FollowUpFacadeTest {

    @Spy
    private DefaultFollowUpTemplateStorage followUpTemplateStorage;

    @Mock
    private FollowupDataProvider provider;

    @Spy
    private UpdateFollowUpService updateFollowUpService = new DefaultUpdateFollowUpService();

    @InjectMocks
    private FollowUpFacade followUpFacade = new DefaultFollowUpFacade();

    @Test
    public void upload() throws IOException {
        MultipartFile file = new MockMultipartFile("file", FollowUpFacadeTest.class.getResourceAsStream("OkFollowupTemplate.xlsm"));
        followUpFacade.updateTemplate(file);
    }
}
