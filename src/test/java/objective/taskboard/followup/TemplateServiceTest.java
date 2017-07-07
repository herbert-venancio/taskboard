package objective.taskboard.followup;

import com.google.common.collect.Lists;
import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.data.Template;
import objective.taskboard.followup.impl.DefaultTemplateService;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;
import objective.taskboard.repository.ProjectFilterConfigurationRepository;
import objective.taskboard.repository.TemplateRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = TemplateServiceTest.Configuration.class)
public class TemplateServiceTest {

    private static final String PROJ1 = "PROJ1";
    private static final String PROJ2 = "PROJ2";

    @EntityScan(
            basePackageClasses = {
                    ProjectFilterConfiguration.class
                    , Template.class})
    public static class Configuration {
        @Bean
        public JpaRepositoryFactoryBean<TemplateRepository, Template, Long> templateRepository() {
            return new JpaRepositoryFactoryBean<>(TemplateRepository.class);
        }
        @Bean
        public TemplateService templateService() {
            return new DefaultTemplateService();
        }
        @Bean
        public JpaRepositoryFactoryBean<ProjectFilterConfigurationRepository, ProjectFilterConfiguration, Long> projectFilterRepository() {
            return new JpaRepositoryFactoryBean<>(ProjectFilterConfigurationRepository.class);
        }
        @Bean
        public ProjectFilterConfigurationCachedRepository projectFilterConfigurationCachedRepository() {
            return new ProjectFilterConfigurationCachedRepository();
        }
    }

    @Autowired
    private ProjectFilterConfigurationCachedRepository projectFilterConfigurationCachedRepository;

    @Autowired
    private TemplateService templateService;

    @Before
    public void createProjects() {
        List<String> keys = Lists.newArrayList(PROJ1, PROJ2);
        for(String projectKey : keys) {
            ProjectFilterConfiguration proj = new ProjectFilterConfiguration();
            proj.setProjectKey(projectKey);
            projectFilterConfigurationCachedRepository.save(proj);
        }
    }

    @Test
    public void givenTemplateForPROJ1_whenSearchForPROJ1_thenReturnOneResult() {
        // given
        repositoryHasTemplateAssociatedWith(PROJ1);

        // when
        List<Template> result = templateService.findTemplatesForProjectKeys(Collections.singletonList(PROJ1));

        // then
        assertThat(result, hasSize(1));
    }

    @Test
    public void givenTemplateForPROJ2_whenSearchForPROJ1_thenReturnNoResults() {
        // given
        repositoryHasTemplateAssociatedWith(PROJ2);

        // when
        List<Template> result = templateService.findTemplatesForProjectKeys(Collections.singletonList(PROJ1));

        // then
        assertThat(result, hasSize(0));
    }

    @Test
    public void givenTemplateForPROJ1PROJ2_whenSearchForPROJ1_thenReturnNoResults() {
        // given
        repositoryHasTemplateAssociatedWith(PROJ1, PROJ2);

        // when
        List<Template> result = templateService.findTemplatesForProjectKeys(Collections.singletonList(PROJ1));

        // then
        assertThat(result, hasSize(0));
    }

    @Test
    public void givenTemplateForPROJ1PROJ2_whenSearchForPROJ1PROJ2_thenReturnOneResult() {
        // given
        repositoryHasTemplateAssociatedWith(PROJ1, PROJ2);

        // when
        List<Template> result = templateService.findTemplatesForProjectKeys(Arrays.asList(PROJ1, PROJ2));

        // then
        assertThat(result, hasSize(1));
    }

    // ---

    private void repositoryHasTemplateAssociatedWith(String... projectKeys) {
        String templateName = "Test Template";
        String templatePath = "test-path";

        String commaSeparatedProjectKeys = String.join(",", projectKeys);
        templateService.saveTemplate(templateName, commaSeparatedProjectKeys, templatePath);
    }
}