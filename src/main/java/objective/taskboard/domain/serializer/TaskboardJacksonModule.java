package objective.taskboard.domain.serializer;

import com.fasterxml.jackson.databind.module.SimpleModule;
import objective.taskboard.utils.DateTimeUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class TaskboardJacksonModule extends SimpleModule {

    public TaskboardJacksonModule() {
        super(TaskboardJacksonModule.class.getName());

        addSerializer(LocalDate.class, DateTimeUtils.LocalDateTimeStampSerializer.INSTANCE);
    }
}
