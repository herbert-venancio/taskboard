package objective.taskboard.auth.authorizer;

import static java.util.Collections.unmodifiableList;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.auth.authorizer.permission.PerProjectPermission;
import objective.taskboard.auth.authorizer.permission.TargetlessPermission;
import objective.taskboard.auth.authorizer.permission.TargettedPermission;

@Service
public class PermissionRepository {

    private final List<TargetlessPermission> targetlessPermissions;
    private final List<TargettedPermission> targettedPermissions;
    private final List<PerProjectPermission> perProjectPermissions;

    @Autowired
    public PermissionRepository(
            List<TargetlessPermission> targetlessPermissions,
            List<TargettedPermission> targettedPermissions,
            List<PerProjectPermission> perProjectPermissions) {
        this.targetlessPermissions = unmodifiableList(targetlessPermissions);
        this.targettedPermissions = unmodifiableList(targettedPermissions);
        this.perProjectPermissions = unmodifiableList(perProjectPermissions);
    }

    public List<TargetlessPermission> findAllTargetless() {
        return targetlessPermissions;
    }

    public List<TargettedPermission> findAllTargetted() {
        return targettedPermissions;
    }

    public List<PerProjectPermission> findAllPerProjectPermissions() {
        return perProjectPermissions;
    }

}
