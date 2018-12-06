// run this file with browserify:
// npm install -g browserify
// browserify src/test/js/jira2md-browserify.js -o src/main/resources/static/scripts/jira2md.js
;(function(root) {
    'use strict'

    root.jira2md = require('jira2md');
})(window);