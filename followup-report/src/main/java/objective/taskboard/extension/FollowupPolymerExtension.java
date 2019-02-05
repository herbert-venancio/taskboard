package objective.taskboard.extension;

import java.util.Optional;

import org.springframework.stereotype.Component;

@Component
public class FollowupPolymerExtension implements PolymerComponentExtension {

    @Override
    public String componentPath() {
        return "followup-report/followup-report.html";
    }

    @Override
    public Optional<String> topLevelComponentName() {
        return Optional.of("followup-report");
    }

}
