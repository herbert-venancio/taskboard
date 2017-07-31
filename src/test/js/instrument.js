/**
 * Instruments all files from src/main/resources/static/
 * for code coverage and writes to target/classes/static/
 */

'use strict';

var istanbul = require('istanbul');
var scriptHook = require('html-script-hook');
var fs = require("fs");
var path = require("path");
var readdirp = require('readdirp');

var srcDir = "src/main/resources/static/";
var outDir = "target/test-classes/static/";

getFileListFromSrcDir()
  .then(instrumentAll)
  .catch(function (err) {
    console.log(err);
  });

// ---

function getFileListFromSrcDir() {
  var fileList = [];
  return new Promise(function (resolve, reject) {
    readdirp({ root: srcDir, fileFilter: ['*.html', '*.js'], directoryFilter: ['!bower_components'] }
      , function(fileInfo) {
        fileList.push(fileInfo.path);
      }
      , function (err, res) {
        if(err) {
          reject(err);
        } else {
          resolve(fileList);
        }
      }
    );
  });
}

function instrumentAll(fileList) {
  function mkdirp(targetDir) {
    const sep = path.sep;
    const initDir = path.isAbsolute(targetDir) ? sep : '';
    targetDir.split(sep).reduce((parentDir, childDir) => {
      const curDir = path.resolve(parentDir, childDir);
      if (!fs.existsSync(curDir)) {
        fs.mkdirSync(curDir);
      }

      return curDir;
    }, initDir);
  }

  // create a new Istanbul instrumenter
  var instrumenter = new istanbul.Instrumenter();

  fileList.forEach(file => {
    var srcFile = srcDir + file;
    var outFile = outDir + file;
    var content = fs.readFileSync(srcFile, 'utf8');
    if(file.endsWith(".html")) {
      // parse the HTML file and replace it with a HTML file where the code has been instrumented
      // (using the gotScript() callback)
      content = scriptHook(content, {scriptCallback: gotScript});

      // catch the code in the script section
      function gotScript(code, loc) {
        // replace the existing code with instrumented code
        return instrumenter.instrumentSync(code, file);
      }
    } else {
      content = instrumenter.instrumentSync(content, file);
    }
    mkdirp(path.dirname(outFile));
    fs.writeFileSync(outFile, content, 'utf8');
  });
}