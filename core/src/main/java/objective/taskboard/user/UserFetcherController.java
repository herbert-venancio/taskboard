package objective.taskboard.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.jira.data.JiraUser.UserDetails;

@RestController
@RequestMapping("/api/users")
class UserFetcherController {

    private final UserSearchService userSearchService;

    @Autowired
    public UserFetcherController(UserSearchService userSearchService) {
        this.userSearchService = userSearchService;
    }

    @GetMapping(path = "search")
    public ResponseEntity<?> searchUser(
            @RequestParam("query") String query) {
        List<UserDetails> usersFound = userSearchService.getUsersVisibleToLoggedInUserByQuery(query);
        return ResponseEntity.ok(usersFound);
    }

}
