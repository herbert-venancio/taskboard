package objective.taskboard.auth.authorizer.permission;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static objective.taskboard.auth.authorizer.Permissions.IMPERSONATE;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.auth.LoggedUserDetails;

@Service
public class ImpersonatePermission extends BaseTargettedPermission {

    private static final Logger log = LoggerFactory.getLogger(ImpersonatePermission.class);

    public static final String IMPERSONATE_HEADER = "obj-impersonate";

    private final TaskboardAdministrationPermission taskboardAdministrationPermission;
    private final UserVisibilityPermission userVisibilityPermission;

    @Autowired
    public ImpersonatePermission(
            LoggedUserDetails loggedUserDetails,
            TaskboardAdministrationPermission taskboardAdministrationPermission,
            UserVisibilityPermission userVisibilityPermission) {
        super(IMPERSONATE, loggedUserDetails);
        this.taskboardAdministrationPermission = taskboardAdministrationPermission;
        this.userVisibilityPermission = userVisibilityPermission;
    }

    @Override
    public boolean isAuthorized(LoggedUserDetails loggedUserDetails, String target) {
        List<String> errors = new ArrayList<>();

        if (!taskboardAdministrationPermission.isAuthorized(loggedUserDetails))
            errors.add(taskboardAdministrationPermission.name() +": false.");

        if (!userVisibilityPermission.isAuthorized(loggedUserDetails, target))
            errors.add(userVisibilityPermission.name() +" for "+ target +": false.");

        if (!errors.isEmpty())
            log.warn("User \""+ loggedUserDetails.defineUsername() +"\" checked authorization for \""+ name() +"\" without having permission.\n"
                    + "Reasons to deny:\n"
                    + errors.stream().map(e -> "- " + e).collect(joining("\n")));

        return errors.isEmpty();
    }

    @Override
    public List<String> applicableTargets() {
        return emptyList();
    }

}
