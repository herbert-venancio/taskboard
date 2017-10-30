/**
 * Generates lcov report with combined coverage from all UI tests
 */

'use scrict';

try {
    var combine = require('istanbul-combine');
    var fs = require("fs");

    combineReports();
    convertToRelativePaths();
}catch(e){
    console.log("FAILED TO GENERATE JS COVERAGE REPORTS");
}

// ---

function combineReports() {
  var opts = {
    dir: 'target/istanbul-reports',             // output directory for combined report(s)
    pattern: 'target/istanbul-reports/*.json',  // json reports to be combined
    print: 'summary',                           // print to the console (summary, detail, both, none)
    base:'src/main/resources/static/', // base directory for resolving absolute paths, see karma bug
    reporters: {
      //html: { /* html reporter options */ },
      //lcov: { /* etc. */ }
      lcovonly: { }
    }
  };

  combine.sync(opts);
}

function convertToRelativePaths() {
  var lcovinfo = fs.readFileSync('target/istanbul-reports/lcov.info', 'utf8');
  lcovinfo = lcovinfo.toString().replace(/^(SF:)(.*)(src\/main\/resources.*)$/gm, '$1$3');
  fs.writeFileSync('target/lcov.info', lcovinfo, 'utf8');
}
