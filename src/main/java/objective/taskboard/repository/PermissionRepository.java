package objective.taskboard.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import objective.taskboard.auth.PermissionRoles;

@Service
public class PermissionRepository {

    public static final String DASHBOARD_TACTICAL = "dashboard.tactical";
    public static final String DASHBOARD_OPERATIONAL = "dashboard.operational";
    public static final String FOLLOWUP_DOWNLOAD = "followup.download"; 
    public static final String FOLLOWUP_UPLOAD = "followup.upload"; 

    private Map<String, PermissionRoles> permissionsMap = new HashMap<>();

    {
        permissionsMap.put(DASHBOARD_TACTICAL,    new PermissionRoles(DASHBOARD_TACTICAL,    "Administrators", "Developers", "KPI"));
        permissionsMap.put(DASHBOARD_OPERATIONAL, new PermissionRoles(DASHBOARD_OPERATIONAL, "Administrators", "Developers"));
        permissionsMap.put(FOLLOWUP_DOWNLOAD,     new PermissionRoles(FOLLOWUP_DOWNLOAD,     "Administrators"));
        permissionsMap.put(FOLLOWUP_UPLOAD,       new PermissionRoles(FOLLOWUP_UPLOAD,       "Administrators"));
    }

    public List<PermissionRoles> findAll() {
        return new ArrayList<PermissionRoles>(permissionsMap.values());
    }

    public PermissionRoles findByName(String name) {
        return permissionsMap.getOrDefault(name, new PermissionRoles());
    }

}
