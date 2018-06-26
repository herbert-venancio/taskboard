package objective.taskboard.project.config;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.jira.ProjectService;
import objective.taskboard.project.ProjectProfileItem;
import objective.taskboard.project.config.ProjectProfileConfigurationController.ProjectProfileItemDto;
import objective.taskboard.repository.PermissionRepository;

public class ProjectProfileConfigurationControllerTest {
    
    private ProjectService projectService = mock(ProjectService.class);
    private ProjectProfileItemMockRepository itemRepository = new ProjectProfileItemMockRepository();
    private ProjectProfileConfigurationController subject = new ProjectProfileConfigurationController(projectService, itemRepository);
    
    private ProjectFilterConfiguration superProject = mock(ProjectFilterConfiguration.class);
    
    @Before
    public void setup() {
        when(superProject.getProjectKey()).thenReturn("SP");
        when(projectService.getTaskboardProject("SP", PermissionRepository.ADMINISTRATIVE)).thenReturn(Optional.of(superProject));
    }

    @Test
    public void updateItems_ValidInput_ShouldChangeTheState() {
        ProjectProfileItem devItem    = new ProjectProfileItem(superProject, "Dev",     10.0, LocalDate.parse("2018-01-01"), LocalDate.parse("2018-02-25"));
        ProjectProfileItem frontItem  = new ProjectProfileItem(superProject, "Front",    8.0, LocalDate.parse("2018-01-01"), LocalDate.parse("2018-02-25"));
        ProjectProfileItem testerItem = new ProjectProfileItem(superProject, "Tester",   4.0, LocalDate.parse("2018-01-01"), LocalDate.parse("2018-02-25"));
        
        itemRepository.add(devItem);
        itemRepository.add(frontItem);
        itemRepository.add(testerItem);

        List<ProjectProfileItemDto> itemsDto = Arrays.asList(
                itemDto(null,              "UX",    4.5, LocalDate.parse("2018-01-01"), LocalDate.parse("2018-02-01")),
                itemDto(devItem.getId(),   "Dev",  12.0, LocalDate.parse("2018-01-01"), LocalDate.parse("2018-03-01")),
                itemDto(frontItem.getId(), "Front", 8.0, LocalDate.parse("2018-01-01"), LocalDate.parse("2018-02-25")));

        ResponseEntity<?> response = subject.updateItems("SP", itemsDto);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        itemRepository.assertData(
                "SP | Dev   | 12.0 | 2018-01-01 | 2018-03-01",
                "SP | Front |  8.0 | 2018-01-01 | 2018-02-25",
                "SP | UX    |  4.5 | 2018-01-01 | 2018-02-01");
    }

    @Test
    public void updateItems_UserIsNotAllowedToAdminTheProject_ShouldReturnNotFound() {
        when(projectService.getTaskboardProject("SP", PermissionRepository.ADMINISTRATIVE)).thenReturn(Optional.empty());
        
        ProjectProfileItem devItem = new ProjectProfileItem(superProject, "Dev", 10.0, LocalDate.parse("2018-01-01"), LocalDate.parse("2018-03-01"));
        itemRepository.add(devItem);
        
        List<ProjectProfileItemDto> itemsDto = Arrays.asList(
                itemDto(devItem.getId(), "Dev", 99.0, LocalDate.parse("2018-01-01"), LocalDate.parse("2018-03-01")));

        ResponseEntity<?> response = subject.updateItems("SP", itemsDto);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        itemRepository.assertData("SP | Dev | 10.0 | 2018-01-01 | 2018-03-01");
    }

    private static ProjectProfileItemDto itemDto(
            Long id,
            String roleName,
            Double peopleCount,
            LocalDate allocationStart,
            LocalDate allocationEnd) {

        ProjectProfileItemDto result = new ProjectProfileItemDto();
        result.id = id;
        result.roleName = roleName;
        result.peopleCount = peopleCount;
        result.allocationStart = allocationStart;
        result.allocationEnd = allocationEnd;

        return result;
    }
}
