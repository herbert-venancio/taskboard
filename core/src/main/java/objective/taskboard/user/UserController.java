package objective.taskboard.user;

import static java.util.stream.Collectors.toList;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.jira.data.JiraUser.UserDetails;

@RestController
@RequestMapping("/ws/users")
class UserController {

    private final UserSearchService userSearchService;

    @Autowired
    public UserController(UserSearchService userSearchService) {
        this.userSearchService = userSearchService;
    }

    @GetMapping(path = "search")
    public ResponseEntity<?> usersWhoseNameContains(
            @RequestParam("query") String userQuery,
            @RequestParam(required=false, defaultValue="false") boolean onlyNames) {
        List<UserDetails> usersFound = userSearchService.getUsersVisibleToLoggedInUserByQuery(userQuery);

        return onlyNames
            ? ResponseEntity.ok(usersFound.stream().map(user -> user.name).collect(toList()))
            : ResponseEntity.ok(usersFound);
    }

    @RequestMapping("logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

}
