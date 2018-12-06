function fetchProjectsXTeams()
{
  var projectList;
  var teamList;
  try {
    sheetStatus("FETCHING FROM SERVER...");
    projectList = JSON.parse(apicall("projects").getContentText());
    teamList = JSON.parse(apicall("teams").getContentText());
    
  }catch(e) {
    sheetStatus("");
    throw e;
  }
  var projectSheet = SpreadsheetApp.getActiveSpreadsheet().getActiveSheet();
  clearAndPresentStatus("REFRESHING...")
  
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
  sheetStatus("");
}

function updateProjectsXTeams()
{
  sheetStatus("Updating Taskboard Projects");
  var sheet = SpreadsheetApp.getActiveSpreadsheet().getActiveSheet();
  var data = updateElementWithChildren(sheet, "projectKey", "teams");
  
  apipatch("projects", data);
  sheetStatus("");
}
