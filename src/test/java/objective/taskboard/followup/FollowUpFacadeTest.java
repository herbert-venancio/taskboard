package objective.taskboard.followup;

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
