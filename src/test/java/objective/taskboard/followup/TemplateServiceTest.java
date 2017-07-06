package objective.taskboard.followup;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.data.Template;
import objective.taskboard.repository.TemplateRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import sun.security.krb5.Config;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = Config.class)
public class TemplateServiceTest {

    @TestConfiguration
    @EntityScan(
            basePackageClasses = {
                    ProjectFilterConfiguration.class
                    , Template.class})
    public static class Config {
        @Bean
        public JpaRepositoryFactoryBean templateRepository() {
            return new JpaRepositoryFactoryBean(TemplateRepository.class);
        }
    }

    @Autowired
    private TemplateRepository templateRepository;

    @Test
    public void test1() {
        Template template = new Template();
        template.setName("Template 1");
        templateRepository.save(template);
        assertThat(templateRepository.count(), is(1L));
    }

    @Test
    public void test2() {
        {
            Template template = new Template();
            template.setName("Template 1");
            templateRepository.save(template);
        }
        {
            Template template = new Template();
            template.setName("Template 2");
            templateRepository.save(template);
        }
        assertThat(templateRepository.count(), is(2L));
    }

}