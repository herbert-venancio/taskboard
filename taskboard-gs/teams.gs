function fetchTeams() {
  var ss = SpreadsheetApp.getActiveSpreadsheet();
  var teamSheet = ss.getSheets()[0];
  
  var r = apicall("teams")
  teamList = JSON.parse(r.getContentText());
  
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
  teamSheet.getRange(row, column, numRows, numColumns)
  
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
}

function updateTeams() {
  var sheet = SpreadsheetApp.getActiveSpreadsheet().getSheets()[0];
  var data = updateElementWithChildren(sheet, "teamName", "teamMembers");
  apipatch("teams", data);
}

