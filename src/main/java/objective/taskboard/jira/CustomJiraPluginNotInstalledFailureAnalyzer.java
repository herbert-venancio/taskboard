package objective.taskboard.jira;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

public class CustomJiraPluginNotInstalledFailureAnalyzer
        extends AbstractFailureAnalyzer<CustomJiraPluginInstalledVerifier.CustomJiraPluginNotInstalledException> {

    @Override
    protected FailureAnalysis analyze(Throwable rootFailure
            , CustomJiraPluginInstalledVerifier.CustomJiraPluginNotInstalledException cause) {
        String description = "Could not retrieve response from Jira Plugin REST endpoint";
        String action = "Verify if the Jira Plugin is installed correctly";
        return new FailureAnalysis(description, action, cause);
    }
}