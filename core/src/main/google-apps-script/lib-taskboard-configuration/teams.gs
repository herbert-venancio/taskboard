function fetchTeams() {
  try {
    sheetStatus("FETCHING FROM SERVER...");
    r = apicall("teams")
  }catch(e) {
    sheetStatus("");
    throw e;
  }
  teamList = JSON.parse(r.getContentText());
  var teamSheet = SpreadsheetApp.getActiveSpreadsheet().getActiveSheet();
  clearAndPresentStatus("REFRESHING...");
  
  var col = 2;
  var allPossibleMembers = []
  var teamNames = []
  for(teamIndex in teamList) {
    var team = teamList[teamIndex];
    teamSheet.getRange(1,col++).setValue(team.teamName);
    teamNames.push(team.teamName)
    team.teamMembers.forEach(function(member) {
      if (allPossibleMembers.indexOf(member) == -1)
        allPossibleMembers.push(member)
    })
  }
  
  var row = 2;
  allPossibleMembers.sort().forEach(function(member) {
    col = 2;
    teamList.forEach(function(team) {
      teamSheet.getRange(row,col).setFontWeight("normal");
      if (team.teamMembers.indexOf(member)>=0) {
        teamSheet.getRange(row,col).setValue("X");
        teamSheet.getRange(row,col).setFontWeight("bold")
      }
      col++;
    });
    teamSheet.getRange(row++,1).setValue(member);
  })
  sheetStatus("");
}

function updateTeams() {
  sheetStatus("Updating Taskboard Teams");
  var sheet = SpreadsheetApp.getActiveSpreadsheet().getActiveSheet();
  var data = updateElementWithChildren(sheet, "teamName", "teamMembers");
  apipatch("teams", data);
  sheetStatus("");
  try {
    apicall("/cache/configuration")
    apicall("/cache/issues")
  }catch(e) {
    throw new ErrorMessage("Failure to update cache");
  }
}
