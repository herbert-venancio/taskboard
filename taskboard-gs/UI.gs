function onOpen() {
  SpreadsheetApp.getUi() // Or DocumentApp or FormApp.
      .createMenu('Taskboard Menu')
      .addItem('Show sidebar', 'showSidebar')
      .addToUi();
}

function showSidebar() {
  var html = HtmlService.createHtmlOutputFromFile('TaskboardSidePage')
      .setTitle('Taskboard Sync')
      .setWidth(300);
  
  SpreadsheetApp.getUi().showSidebar(html);
}

function refreshSheetData() {
  var activeSheet = SpreadsheetApp.getActiveSpreadsheet().getActiveSheet();
  switch (activeSheet.getIndex()) {
    case 1: fetchTeams(); 
      break;
    case 2: fetchWip();
      break;
    case 3: fetchProjectsXTeams(); 
      break;
  }
}

function updateSheetData()
{
  var ui = SpreadsheetApp.getUi();
  
  var result = ui.alert('Please confirm','Are you sure you want to continue?', ui.ButtonSet.YES_NO);  
  if (result == ui.Button.NO) {
    ui.alert('Update aborted');
    return;
  }

  var activeSheet = SpreadsheetApp.getActiveSpreadsheet().getActiveSheet();
  switch (activeSheet.getIndex()) {
    case 1: updateTeams(); 
      break;
    case 2: updateWip();
      break;
    case 3: updateProjectsXTeams(); 
      break;
  }
  SpreadsheetApp.getUi().alert("Update complete")
}

