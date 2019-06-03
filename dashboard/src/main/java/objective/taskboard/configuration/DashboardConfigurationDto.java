package objective.taskboard.configuration;

import objective.taskboard.configuration.exception.DashboardConfigurationParsingDtoException.DashboardConfigurationNonPositiveDaysException;
import objective.taskboard.configuration.exception.DashboardConfigurationParsingDtoException.DashboardConfigurationNullDtoException;

public class DashboardConfigurationDto {
    public Long id;
    public int timelineDaysToDisplay;

    public static DashboardConfigurationDto of(DashboardConfiguration configuration) {
        DashboardConfigurationDto dto = new DashboardConfigurationDto();
        dto.id = configuration.getId();
        dto.timelineDaysToDisplay = configuration.getTimelineDaysToDisplay();
        return dto;
    }
    
    public static DashboardConfiguration parse(DashboardConfigurationDto dto) {
        validate(dto);
        DashboardConfiguration configuration = new DashboardConfiguration();
        configuration.setId(dto.id);
        try {
            configuration.setTimelineDaysToDisplay(dto.timelineDaysToDisplay);
        } catch (IllegalArgumentException exp) {
            throw new DashboardConfigurationNonPositiveDaysException(exp);
        }
        return configuration;
    }

    private static void validate(DashboardConfigurationDto dto) {
        if (dto == null)
            throw new DashboardConfigurationNullDtoException();
    }
}