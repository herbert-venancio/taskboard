package objective.taskboard;

/*-
 * [LICENSE]
 * Taskboard
 * ---
 * Copyright (C) 2015 - 2017 Objective Solutions
 * ---
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * [/LICENSE]
 */


import static spark.Spark.get;
import static spark.Spark.post;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import spark.Spark;


public class JiraMockServer {
    public static void begin() { 
        Thread thread = new Thread(JiraMockServer::defineRoutesAndStart);
        thread.setDaemon(true);
        thread.start();;
    }
    
    public static void defineRoutesAndStart() {
        Spark.port(4567);
        Spark.exception(Exception.class, (ex, req, res) -> {
            ex.printStackTrace();
        });
        get("rest/api/latest/project",  (req, res) ->{
            return IOUtils.toString(JiraMockServer.class.getResourceAsStream("/project.response.txt"), "UTF-8");
        });
        
        get("/rest/api/latest/project/TASKB", (req, res) ->{
            return IOUtils.toString(JiraMockServer.class.getResourceAsStream("/project_TASKB.response.txt"), "UTF-8");
        });
        
        get("rest/api/latest/status",  (req, res) ->{
            return IOUtils.toString(JiraMockServer.class.getResourceAsStream("/status.response.txt"), "UTF-8");
        });
        
        get("rest/api/latest/priority",  (req, res) ->{
            return IOUtils.toString(JiraMockServer.class.getResourceAsStream("/priority.response.txt"), "UTF-8");
        });
        
        get("rest/api/latest/issuetype",  (req, res) ->{
            return IOUtils.toString(JiraMockServer.class.getResourceAsStream("/issuetype.response.txt"), "UTF-8");
        });
        
        get("rest/api/latest/user",  (req, res) ->{
            return IOUtils.toString(JiraMockServer.class.getResourceAsStream("/user.response.txt"), "UTF-8");
        });
        
        get("rest/api/latest/serverInfo",  (req, res) ->{
            return IOUtils.toString(JiraMockServer.class.getResourceAsStream("/serverInfo.response.txt"), "UTF-8");
        });
        post("/rest/api/latest/search", "application/json", (req,res) -> {
            @SuppressWarnings("rawtypes")
            Map searchData = gson.fromJson(req.body(), java.util.Map.class);
            return makeFakeRequest(Math.round((Double)searchData.get("startAt")));
        });
    }
    
    private static String makeFakeRequest(long l) {
        InputStream stream = JiraMockServer.class.getResourceAsStream("/searchIssueAt"+l+".txt");
        if (stream == null)
            return "{\"startAt\":0,\"maxResults\":100,\"total\":605,\"issues\":[]}";
        
        try {
            return IOUtils.toString(stream, "UTF-8");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
    
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
}
