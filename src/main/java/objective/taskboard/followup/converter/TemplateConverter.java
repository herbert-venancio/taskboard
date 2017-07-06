package objective.taskboard.followup.converter;

import objective.taskboard.controller.TemplateData;
import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.data.Template;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class TemplateConverter implements Converter<Template, TemplateData> {

    @Override
    public TemplateData convert(Template source) {
        TemplateData target = new TemplateData();
        target.name = source.getName();
        target.projects = source.getProjects().stream().map(ProjectFilterConfiguration::getProjectKey).collect(Collectors.toList());
        return target;
    }
}
