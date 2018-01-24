package objective.taskboard.testUtils;

import spark.Service;

import static org.apache.commons.lang3.StringUtils.capitalize;

public class JiraPluginMock {
    public void load(Service server) {
        server.get("/rest/projectbuilder/1.0/users/:user", (req, res) -> {
            String username = req.params(":user");

            if("unknown.user".equals(username)) {
                res.status(404);
                return "";
            }

            String data = loadMockData("users-" + username + ".response.json");
            if(data != null)
                return data;

            data = loadMockData("users-foo.response.json");
            return data.replaceAll("foo", username)
                    .replaceAll("Foo", capitalize(username));
        });
    }

    private static String loadMockData(String data) {
        return JiraMockServer.loadMockData("plugin/" + data);
    }
}
