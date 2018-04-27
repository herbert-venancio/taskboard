package objective.taskboard.followup;

import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

public class FollowUpHistoryKeeperInitializer {

    @Autowired
    FollowUpHistoryKeeper followUpDataHistoryGeneratorJSONFiles;

    @PostConstruct
    public void initializer() {
        followUpDataHistoryGeneratorJSONFiles.initialize();
    }
}