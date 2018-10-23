package objective.taskboard.domain.converter;

import javax.persistence.AttributeConverter;

import objective.taskboard.data.UserTeam.UserTeamRole;

public class UserTeamRoleConverter implements AttributeConverter<UserTeamRole, String> {

    @Override
    public String convertToDatabaseColumn(UserTeamRole attribute) {
        return attribute.toString().toLowerCase();
    }

    @Override
    public UserTeamRole convertToEntityAttribute(String dbData) {
        return UserTeamRole.valueOf(dbData.toUpperCase());
    }

}
