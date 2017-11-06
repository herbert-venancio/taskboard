function sheetStatus(statusText) {
  SpreadsheetApp.getActiveSpreadsheet().getActiveSheet().getRange(1,1).setValue(statusText)
  SpreadsheetApp.flush();
}

function alertMsg(txt)
{
  SpreadsheetApp.getUi().alert(txt)
}

function clearAndPresentStatus(statusText) {
  var sheet = SpreadsheetApp.getActiveSpreadsheet().getActiveSheet();
  sheet.getRange(1,1).setValue(statusText)
  SpreadsheetApp.flush();
  
  sheet.clearContents();
  
  // just to make sure the status will show up even if a flush happens automatically
  sheet.getRange(1,1).setValue(statusText)
}

function updateElementWithChildren(sheet, keyName, collectionName)
{  
  var mainelement = [];
  
  var col = 2;
  while(projectKey = sheet.getRange(1, col++).getValue()) {
  	anElement = {};
  	anElement[keyName] = projectKey
  	anElement[collectionName]=[];
    mainelement.push(anElement);
  }  

  var bolds = [];
  var row = 2;
  while(child = sheet.getRange(row, 1).getValue()) {
    var boldLine = [];
    bolds.push(boldLine);
    for (idx=0; idx < mainelement.length; idx++) {
      var cell = sheet.getRange(row, idx+2);
      if (cell.getValue()) {
        mainelement[idx][collectionName].push(child)
        boldLine.push("bold");
      }
      else
        boldLine.push("normal");
    }
    row++;
  }
  sheet.getRange(2,2, bolds.length, bolds[0].length).setFontWeights(bolds);
  
  var data = []
  for(i in mainelement) 
    data.push(mainelement[i]);

  return data;
}


function apicall(path, options) {
  var taskboardUrl = "https://taskboard"; //TODO configure
  var taskboardAuthHeader = "Basic xxx";  //TODO configure
  
  if (!options) 
    options = {};
  if (!options["headers"]) 
    options["headers"]={};
  
  options["headers"]["Authorization"] = taskboardAuthHeader;

  var url = taskboardUrl + (path.indexOf("/") == 0 ? "/" : "/api/") + path;

  try {
    return UrlFetchApp.fetch(url,options)
  }catch(e) {
    Logger.log("An error happened when invoking the API for URL " + url + " Error: " + e);
    throw e;
  }
}

function apipatch(path, data) {
  d = apicall(path, {
    "method":"patch", 
    "contentType": "application/json",
    "payload": JSON.stringify(data)
  })
  apicall("/cache/configuration");
  return d;
}

function apiput(path, data) {
  d = apicall(path, {
    "method":"put", 
    "contentType": "application/json",
    "payload": JSON.stringify(data)
  })
  apicall("/cache/configuration");
  return d;
}

function apipost(path, data) {
  d = apicall(path, {
    "method":"post", 
    "contentType": "application/json",
    "payload": JSON.stringify(data)
  })
  apicall("/cache/configuration");
  return d;
}

function measureIni() {
  return new Date();
}

function measureEnd(previousDate) {
  Logger.log(new Date().getTime() - previousDate.getTime())
}

function ErrorMessage(msg) {
  this.message = msg;
}
