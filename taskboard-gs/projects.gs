function fetchProjectsXTeams()
{
  var ss = SpreadsheetApp.getActiveSpreadsheet();
  var projectSheet = ss.getSheets()[2];
  
  var projectList = JSON.parse(apicall("projects").getContentText());
  var teamList = JSON.parse(apicall("teams").getContentText());
  
  var grid = [[""]];
  projectList.forEach(function(project) {
    grid[0].push(project.projectKey);
  });
  
  teamList.forEach(function(team) {
    var aRow = [team.teamName];
    
    projectList.forEach(function(project) {
      if (project.teams.indexOf(team.teamName)!=-1)
        aRow.push("X");
      else
        aRow.push("");    
    })
    grid.push(aRow)
  })
  
  projectSheet.getRange(1,1,grid.length,grid[0].length).setValues(grid);
}

function updateProjectsXTeams()
{
  var sheet = SpreadsheetApp.getActiveSpreadsheet().getSheets()[2];
  var data = updateElementWithChildren(sheet, "projectKey", "teams");
  
  apipatch("projects", data);
}

