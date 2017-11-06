
function fetchWip() {
  try {
    sheetStatus("FETCHING FROM SERVER");
    var response = apicall("wips");
  }catch(e) {
    sheetStatus("");
    throw e;
  }

  clearAndPresentStatus("REFRESHING...");
  
  var data = JSON.parse(response.getContentText());
  var wipSheet = SpreadsheetApp.getActiveSpreadsheet().getActiveSheet();
  var teams = Object.keys(data.wipsByTeam);
  var stepColByStepId = {};
  var wipColStart = 2;
  var teamRowStart = 3;
  var currentCol = wipColStart;

  wipSheet.getRange(1, wipColStart, 1, wipSheet.getMaxColumns()).breakApart();
  
  data.lanes.forEach(function(lane) {
    wipSheet.getRange(1, currentCol, 1, lane.steps.length).merge().setValue(lane.name);
    
    lane.steps.forEach(function(step) {
      var stepHeader = step.name + " (" + step.id + ")";

      wipSheet.getRange(2, currentCol).setValue(stepHeader);
      stepColByStepId[step.id] = currentCol;
      currentCol++;
    });
  });

  teams.forEach(function(team, teamIndex) {
    var teamWip = data.wipsByTeam[team];
    var row = teamRowStart + teamIndex;

    wipSheet.getRange(row, 1).setValue(team);
    
    teamWip.forEach(function(wip) {
      wipSheet.getRange(row, stepColByStepId[wip.stepId]).setValue(wip.wip);
    });
  });

  sheetStatus("");
}

function updateWip() {
  sheetStatus("Updating Taskboard Wip");
  
  var wipSheet = SpreadsheetApp.getActiveSpreadsheet().getActiveSheet();
  var stepIdByCol = {};
  var stepIdPattern = /\((\d+)\)/;
  var currentCol = 2;
  var stepHeader;

  while (stepHeader = currentCelValue = wipSheet.getRange(2, currentCol).getValue()) {
    var match = stepIdPattern.exec(stepHeader.trim());

    if (!match) {
      SpreadsheetApp.getUi().alert("Invalid step header: '" + stepHeader + "'\nExpected format: 'Step Description (step id)'. E.g. 'Done (11)'");
      return;
    }

    stepIdByCol[currentCol] = parseInt(match[1]);
    currentCol++;
  }

  var updateData = [];
  var row = 3;
  var team;
  
  while(team = wipSheet.getRange(row, 1).getValue()) {
    Object.keys(stepIdByCol).forEach(function(stepCol) {
      var stepId = stepIdByCol[stepCol];
      var celValue = wipSheet.getRange(row, stepCol).getValue();
      
      if (celValue != "")
        updateData.push({team: team, stepId: stepId, wip: parseInt(celValue)});
    });

    row++;
  }

  apiput("wips", updateData)
  sheetStatus("");
}