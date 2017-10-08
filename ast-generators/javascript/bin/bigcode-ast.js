#!/usr/bin/env node

const astGenerator = require('..');
const yargs = require('yargs');

const argv = yargs
    .alias('f', 'files')
    .alias('o', 'output-dir')
    .demandOption(['files', 'output-dir'])
    .usage('Usage: $0 -f <files> -o <output-dir>')
    .example('$0 -f src/**/*.js -o result',
             'parse all JS files in src dir and output ASTs in result dir')
    .describe('files', 'Glob pattern of files to parse')
    .describe('output-dir', 'The directory where to put the results')
    .help('h')
    .alias('h', 'help')
    .argv;

astGenerator(argv, (err, count) => {
  if (err !== null) {
    console.error('failed: ' + err);
  } else {
    console.log(`generated ASTs for ${count} files`);
  }
});
