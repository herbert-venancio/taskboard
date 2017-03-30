function fetchWip() {
  var ss = SpreadsheetApp.getActiveSpreadsheet();
  var wipSheet = ss.getSheets()[1];
  
  var wipStatus = [];
  col=2;
  while (wipSheet.getRange(1,col).getValue()!="") {
    wipStatus.push(wipSheet.getRange(1,col).getValue());
    col++;
  }
  
  r = apicall("wips")
  wipList = JSON.parse(r.getContentText());
  
  row = 2;
  for(team in wipList) {
    wipSheet.getRange(row,1).setValue(team);
    col = 2;
    
    for (j in wipStatus) {
      wipSheet.getRange(row,col++).setValue(wipList[team]["statusWip"][wipStatus[j]]);
    }
    row++;
  }
}

function updateWip()
{
  var ss = SpreadsheetApp.getActiveSpreadsheet();
  var wipSheet = ss.getSheets()[1];  
  var wipStatus = [];
  col=2;
  while (wipSheet.getRange(1,col).getValue()!="") {
    wipStatus.push(wipSheet.getRange(1,col).getValue());
    col++;
  }
  
  updateData=[]
  row = 2;
  while(team = wipSheet.getRange(row,1).getValue()) {
    data = {}
    data.team = team;
    data.statusWip={}
    col = 2;
    for (iw in wipStatus) {
      data.statusWip[wipStatus[iw]] = wipSheet.getRange(row,col).getValue();
      col++;
    }

    updateData.push(data);   
    row++;
  }
  apipatch("wips", updateData)
}
