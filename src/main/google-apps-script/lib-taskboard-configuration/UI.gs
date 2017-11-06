function checkAvailable(){
  // just check the lib is available
  return true;
}

function refreshSheetData() {
  var activeSheet = SpreadsheetApp.getActiveSpreadsheet().getActiveSheet();
  try {
    switch (activeSheet.getName()) {
      case "Teams x Members": fetchTeams(); 
        break;
      case "WIP": fetchWip();
        break;
      case "Projects x Teams": fetchProjectsXTeams(); 
        break;
    }
  }catch(e) {
    Logger.log("Could not fetch data from server. " + e);
    throw "Could not fetch data from server. " + e;
  }
}

function updateSheetData()
{
  var ui = SpreadsheetApp.getUi();
  
  var result = ui.alert('Please confirm','Are you sure you want to update?', ui.ButtonSet.YES_NO);  
  if (result == ui.Button.NO) {
    ui.alert('Update aborted');
    return;
  }

  var activeSheet = SpreadsheetApp.getActiveSpreadsheet().getActiveSheet();
  try {
    switch (activeSheet.getName()) {
      case "Teams x Members": updateTeams(); 
        break;
      case "WIP": updateWip();
        break;
      case "Projects x Teams": updateProjectsXTeams(); 
        break;
    }
  }catch(e) {
    Logger.log("Failed to send data to server. Reason: " + e);
    throw "Errors sending data. " + e;
  }
  finally{
    sheetStatus("");
  }
}