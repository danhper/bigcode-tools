#!/usr/bin/env node

const astGenerator = require('..');

const filepath = process.argv[2];

astGenerator.fromFile(filepath, (err, result) => {
  require('fs').writeFileSync('test.json', JSON.stringify(result, null, 2));
});
