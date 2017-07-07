package objective.taskboard.followup;

import objective.taskboard.followup.data.Template;

import java.util.List;

public interface TemplateService {
    void saveTemplate(String templateName, String projects, String path);

    List<Template> findTemplatesForProjectKeys(List<String> projectKeys);

    Template getTemplate(String templateName);
}
