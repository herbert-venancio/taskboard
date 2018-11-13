package objective.taskboard.controller;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.auth.authorizer.permission.UserVisibilityPermission;
import objective.taskboard.jira.JiraService;
import objective.taskboard.jira.data.JiraUser.UserDetails;

@RestController
@RequestMapping("/ws/users")
public class UserController {

    @Autowired
    private JiraService jiraBean;

    @Autowired
    private UserVisibilityPermission userVisibilityPermission;

    @GetMapping(path = "search")
    public ResponseEntity<?> usersWhoseNameContains(
            @RequestParam("query") String userQuery,
            @RequestParam(required=false, defaultValue="false") boolean onlyNames) {
        List<UserDetails> usersFound = getUsersVisibleToLoggedInUserByQuery(userQuery);

        return onlyNames
            ? new ResponseEntity<>(usersFound.stream().map(user -> user.name).collect(toList()), OK)
            : new ResponseEntity<>(usersFound, OK);
    }

    @RequestMapping("logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    private List<UserDetails> getUsersVisibleToLoggedInUserByQuery(String userQuery) {
        List<UserDetails> usersFound = jiraBean.findUsers(userQuery);
        return usersFound.stream()
                .filter(user -> userVisibilityPermission.isAuthorizedFor(user.name))
                .collect(toList());
    }


}
