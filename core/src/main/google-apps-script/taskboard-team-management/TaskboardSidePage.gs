<!DOCTYPE html>

<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap-theme.min.css" integrity="sha384-rHyoN1iRsVXV4nD0JutlnGaslCJuC7uwjduW9SVrLvRYooPp2bWYgmgJQIXwl/Sp" crossorigin="anonymous">

<style>
body {
  padding-left: 1em;
}
.messageError {
  color: red;
}
.messageSuccess {
  color: green;
}
</style>
<script type="text/javascript">
function refresh() {
  _m("messageSuccess","Refreshing started, please wait...");
  google.script.run.withFailureHandler(onFailure).withSuccessHandler(onRefreshSuccess).refreshSheetData()
}

function update() {
  _m("messageSuccess","Updating started, please wait...");
  google.script.run.withFailureHandler(onFailure).withSuccessHandler(onSaveSuccess).saveSheetData()
}

function onFailure(error) {
  _m("messageError", error.message+"<br><br>For authorization errors, go to<br><b>Taskboard Menu > Authorize sync</b>");
  alert("To use the sync, first authorize it by going to Taskboard Menu > Authorize sync");
}

function onRefreshSuccess() {
  _m("messageSuccess","Refresh complete");
}

function onSaveSuccess() {
  _m("messageSuccess","Save complete");
}

function _m(className, txt) {
   var _el = document.getElementById("message");
   _el.innerHTML = txt;
   _el.className = className
}

</script>
<h2>Taskboard Sync</h2>
<p>
  <input type="button" class="btn btn-primary" value="Refresh" onclick="refresh()">
  <input type="button" class="btn btn-primary" value="Save"    onclick="update()">
</p>
<p id='message'></p>
