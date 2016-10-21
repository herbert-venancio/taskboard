/*
Copyright (c) 2015 The Polymer Project Authors. All rights reserved.
This code may only be used under the BSD style license found at http://polymer.github.io/LICENSE.txt
The complete set of authors may be found at http://polymer.github.io/AUTHORS.txt
The complete set of contributors may be found at http://polymer.github.io/CONTRIBUTORS.txt
Code distributed by Google as part of the polymer project is also
subject to an additional IP rights grant found at http://polymer.github.io/PATENTS.txt
*/

'use strict';

// Include Gulp & Tools We'll Use
var gulp = require('gulp');
var $ = require('gulp-load-plugins')();
var del = require('del');
var runSequence = require('run-sequence');
var browserSync = require('browser-sync');
var reload = browserSync.reload;
var merge = require('merge-stream');
var path = require('path');
var fs = require('fs');
var glob = require('glob');
var historyApiFallback = require('connect-history-api-fallback');
var gutil = require('gulp-util');
var debug = require('gulp-debug');


var baseDir = 'src/main/webapp';
var bowerComponentsDir = baseDir + '/bower_components';

var dest = gutil.env.dest || 'target/dest'

var AUTOPREFIXER_BROWSERS = [
  'ie >= 10',
  'ie_mob >= 10',
  'ff >= 30',
  'chrome >= 34',
  'safari >= 7',
  'opera >= 23',
  'ios >= 7',
  'android >= 4.4',
  'bb >= 10'
];

var styleTask = function (stylesPath, srcs) {
  return gulp.src(srcs.map(function(src) {
      return path.join(baseDir, stylesPath, src);
    }))
    .pipe(debug({title: stylesPath + ':'}))
    .pipe($.changed(stylesPath, {extension: '.css'}))
    .pipe($.autoprefixer(AUTOPREFIXER_BROWSERS))
    .pipe(gulp.dest('.tmp/' + stylesPath))
    .pipe($.if('*.css', $.cssmin()))
    .pipe(gulp.dest(dest + '/' + stylesPath))
    .pipe($.size({title: stylesPath}));
};

// Compile and Automatically Prefix Stylesheets
gulp.task('styles', function () {
  return styleTask('styles', ['/**/*.css']);
});

gulp.task('elements', function () {
  return styleTask('elements', ['**/*.css']);
});

// Lint JavaScript
gulp.task('jshint', function () {
  return gulp.src([
      baseDir + '/scripts/**/*.js',
      baseDir + '/elements/**/*.js',
      baseDir + '/elements/**/*.html'
    ])
    .pipe(reload({stream: true, once: true}))
    .pipe($.jshint.extract()) // Extract JS from .html files
    .pipe($.jshint())
    .pipe($.jshint.reporter('jshint-stylish'))
    .pipe($.if(!browserSync.active, $.jshint.reporter('fail')));
});

// Optimize Images
gulp.task('images', function () {
  return gulp.src(baseDir + '/images/**/*')
    .pipe(debug({title: 'images:'}))
    .pipe($.cache($.imagemin({
      progressive: true,
      interlaced: true
    })))
    .pipe(gulp.dest(path.join(dest, 'images')))
    .pipe($.size({title: 'images'}));
});

// Copy All Files At The Root Level (app)
gulp.task('copy', function () {
  var app = gulp.src([
    baseDir + '/*',
    '!' + baseDir + '/precache.json',
    '!' + baseDir + '/WEB-INF'
  ], {
    dot: true
  })
  	.pipe(debug({title: 'copy app:'}))
  	.pipe(gulp.dest(dest));

  var bower = gulp.src([
	  bowerComponentsDir + '/**/*'
	])
//  	.pipe(debug({title: 'copy bower:'}))
  	.pipe(gulp.dest(path.join(dest, 'bower_components')));

  var elements = gulp.src([baseDir + '/elements/**/*.html'])
  	.pipe(debug({title: 'copy elements:'}))
    .pipe(gulp.dest(path.join(dest, 'elements')));

  var swBootstrap = gulp.src([bowerComponentsDir + '/platinum-sw/bootstrap/*.js'])
  	.pipe(debug({title: 'copy bootstrap:'}))
    .pipe(gulp.dest(path.join(dest, 'elements/bootstrap')));

  var swToolbox = gulp.src([bowerComponentsDir + '/sw-toolbox/*.js'])
  	.pipe(debug({title: 'copy sw:'}))
    .pipe(gulp.dest(path.join(dest, 'sw-toolbox')));

  var vulcanized = gulp.src([baseDir + '/elements/elements.html'])
  	.pipe(debug({title: 'copy vulcanized:'}))
    .pipe($.rename('elements.vulcanized.html'))
    .pipe(gulp.dest(path.join(dest, 'elements')));

  return merge(app, bower, elements, vulcanized, swBootstrap, swToolbox)
    .pipe($.size({title: 'copy'}));
});

// Copy Web Fonts To Dist
gulp.task('fonts', function () {
  return gulp.src([baseDir + '/fonts/**'])
  	.pipe(debug({title: 'fonts:'}))
    .pipe(gulp.dest(path.join(dest, 'fonts')))
    .pipe($.size({title: 'fonts'}));
});

// Scan Your HTML For Assets & Optimize Them
gulp.task('html', function () {
  var assets = $.useref.assets({searchPath: ['.tmp', baseDir, dest]});

  return gulp.src([baseDir + '/**/*.html', '!' + baseDir + '/{elements,test}/**/*.html'])
  	.pipe(debug({title: 'html:'}))
    // Replace path for vulcanized assets
    .pipe($.if('*.html', $.replace('elements/elements.html', 'elements/elements.vulcanized.html')))
    .pipe(assets)
    // Concatenate And Minify JavaScript
    .pipe($.if('*.js', $.uglify({preserveComments: 'some'})))
    // Concatenate And Minify Styles
    // In case you are still using useref build blocks
    .pipe($.if('*.css', $.cssmin()))
    .pipe(assets.restore())
    .pipe($.useref())
    // Minify Any HTML
    .pipe($.if('*.html', $.minifyHtml({
      quotes: true,
      empty: true,
      spare: true
    })))
    // Output Files
    .pipe(gulp.dest(dest))
    .pipe($.size({title: 'html'}));
});

// Vulcanize imports
gulp.task('vulcanize', function () {
  var DEST_DIR = path.join(dest, 'elements');

  return gulp.src(path.join(dest, 'elements/elements.vulcanized.html'))
    .pipe($.vulcanize({
      stripComments: true,
      inlineCss: true,
      inlineScripts: true
    }))
    .pipe(debug({title: 'vulcanize:'}))
    .pipe(gulp.dest(DEST_DIR))
    .pipe($.size({title: 'vulcanize'}));
});

// Generate a list of files that should be precached when serving from 'dist'.
// The list will be consumed by the <platinum-sw-cache> element.
gulp.task('precache', function (callback) {
  var dir = dest;

  glob('{elements,scripts,styles}/**/*.*', {cwd: dir}, function(error, files) {
    if (error) {
      callback(error);
    } else {
      files.push('index.html', './', bowerComponentsDir + '/webcomponentsjs/webcomponents-lite.min.js');
      var filePath = path.join(dir, 'precache.json');
      fs.writeFile(filePath, JSON.stringify(files), callback);
    }
  });
});

// Clean Output Directory
gulp.task('clean', del.bind(null, ['.tmp', dest]));

// Watch Files For Changes & Reload
gulp.task('serve', ['styles', 'elements', 'images'], function () {
  browserSync({
    notify: false,
    logPrefix: 'PSK',
    snippetOptions: {
      rule: {
        match: '<span id="browser-sync-binding"></span>',
        fn: function (snippet) {
          return snippet;
        }
      }
    },
    // Run as an https by uncommenting 'https: true'
    // Note: this uses an unsigned certificate which on first access
    //       will present a certificate warning in the browser.
    // https: true,
    server: {
      baseDir: ['.tmp', baseDir],
      middleware: [ historyApiFallback() ],
      routes: {
        '/bower_components': 'bower_components'
      }
    }
  });

  gulp.watch([baseDir + '/**/*.html'], reload);
  gulp.watch([baseDir + '/styles/**/*.css'], ['styles', reload]);
  gulp.watch([baseDir + '/elements/**/*.css'], ['elements', reload]);
  gulp.watch([baseDir + '/{scripts,elements}/**/*.js'], ['jshint']);
  gulp.watch([baseDir + '/images/**/*'], reload);
});

// Build and serve the output from the dist build
gulp.task('serve:dist', ['default'], function () {
  browserSync({
    notify: false,
    logPrefix: 'PSK',
    snippetOptions: {
      rule: {
        match: '<span id="browser-sync-binding"></span>',
        fn: function (snippet) {
          return snippet;
        }
      }
    },
    // Run as an https by uncommenting 'https: true'
    // Note: this uses an unsigned certificate which on first access
    //       will present a certificate warning in the browser.
    // https: true,
    server: dest,
    middleware: [ historyApiFallback() ]
  });
});

// Build Production Files, the Default Task
gulp.task('default', ['clean'], function (cb) {
  runSequence(
    ['copy', 'styles'],
    'elements',
    ['jshint', 'images', 'fonts', 'html'],
    'vulcanize',
    cb);
    // Note: add , 'precache' , after 'vulcanize', if your are going to use Service Worker
});

// Load tasks for web-component-tester
// Adds tasks for `gulp test:local` and `gulp test:remote`
require('web-component-tester').gulp.init(gulp);

// Load custom tasks from the `tasks` directory
try { require('require-dir')('tasks'); } catch (err) {}
