/**
 * @OnlyCurrentDoc
 */
function onOpen() {
  if (!TaskboardTeamConfigurationLib.checkAvailable())
    return;

  SpreadsheetApp.getUi() // Or DocumentApp or FormApp.
      .createMenu('Taskboard Menu')
      .addItem('Show sidebar', 'showSidebar')
      .addItem('Authorize sync', 'authorizeSheetData')
      .addToUi();
}

function showSidebar() {
  if (!TaskboardTeamConfigurationLib.checkAvailable())
    return;
  
  var html = HtmlService.createHtmlOutputFromFile('TaskboardSidePage')
      .setTitle('Taskboard Sync')
      .setWidth(300);
  
  SpreadsheetApp.getUi().showSidebar(html);
}

function authorizeSheetData() {
  // this function can be empty, it's only used to allow the user to authorize the data access
}

function refreshSheetData() {
  TaskboardTeamConfigurationLib.refreshSheetData();
}

function saveSheetData()
{
  TaskboardTeamConfigurationLib.updateSheetData();
}