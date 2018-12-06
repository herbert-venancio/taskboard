package objective.taskboard.project.config;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.project.ProjectProfileItem;
import objective.taskboard.project.ProjectProfileItemRepository;

public class ProjectProfileItemMockRepository implements ProjectProfileItemRepository {
    private final Map<Long, ProjectProfileItem> data = new HashMap<>();
    private long id = 0;

    @Override
    public List<ProjectProfileItem> listByProject(ProjectFilterConfiguration project) {
        return data.values().stream()
                .filter(i -> i.getProject().equals(project))
                .sorted(comparing(i -> i.getRoleName()))
                .collect(toList());
    }

    @Override
    public void add(ProjectProfileItem item) {
        item.setId(id++);
        data.put(item.getId(), item);
    }

    @Override
    public void remove(ProjectProfileItem item) {
        data.remove(item.getId());
    }
    
    public void assertData(String... expectedItems) {
        String expected = Stream.of(expectedItems)
                .map(i -> i.replaceAll("\\s+", " "))
                .collect(joining("\n"));

        String actual = data.values().stream()
                .sorted(comparing(i -> i.getRoleName()))
                .map(i -> itemToString(i))
                .collect(joining("\n"));
        
        assertEquals(expected, actual);
    }

    private static String itemToString(ProjectProfileItem i) {
        return String.format("%s | %s | %s | %s | %s", 
                i.getProject().getProjectKey(), 
                i.getRoleName(), 
                i.getPeopleCount(), 
                i.getAllocationStart(), 
                i.getAllocationEnd());
    }
}
