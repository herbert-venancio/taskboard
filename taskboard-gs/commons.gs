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
  if (!options) 
    options = {};
  if (!options["headers"]) 
    options["headers"]={};
  
  options["headers"]["Authorization"]="Basic Z3MtdGFzay1ib2FyZDpyM20zbWIzcg==";
  
  url="http://e65w.objective.com.br:8082/api/"+path
  Logger.log(url)
  return UrlFetchApp.fetch(url,options)
}

function apipatch(path, data) {
  return apicall(path, {
    "method":"patch", 
    "contentType": "application/json",
    "payload": JSON.stringify(data)
  })
}


function apipost(path, data) {
  return apicall(path, {
    "method":"post", 
    "contentType": "application/json",
    "payload": JSON.stringify(data)
  })
}

function measureIni() {
  return new Date();
}

function measureEnd(previousDate) {
  Logger.log(new Date().getTime() - previousDate.getTime())
}
