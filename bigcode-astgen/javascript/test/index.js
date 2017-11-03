const fs     = require('fs');
const path   = require('path');
const expect = require('chai').expect;
const temp   = require('temp').track();
const async  = require('async');

const bigcodeAST = require('..');

const FIXTURES_PATH = path.join(__dirname, 'fixtures');

function stripExtension(filename) {
  return filename.replace(path.extname(filename), '');
};

describe('bigcodeAST', function() {
  const asts = {};
  const sources = {};

  function processRawAST(ast) {
    const result = JSON.parse(ast);
    if (result[result.length - 1] === 0) {
      result.splice(result.length - 1, 1);
    }
    return result;
  }

  function generateContent(object, directoryName, options = {}) {
    const files = fs.readdirSync(path.join(FIXTURES_PATH, directoryName));
    files.forEach((filename) => {
      const filepath = path.join(FIXTURES_PATH, directoryName, filename);
      const key = stripExtension(filename);
      const content = fs.readFileSync(filepath, 'utf-8');
      object[key] = options.process ? options.process(content) : content;
    });
  }

  before(function() {
    generateContent(asts, 'asts', {process: processRawAST});
    generateContent(sources, 'sources');
  });

  describe('fromString', function() {
    it('should return js150 format', function() {
      Object.keys(asts).forEach((key) => {
        const expected = asts[key];
        const result = bigcodeAST.fromString(sources[key]);

        expect(result).to.deep.equal(expected);
      });
    });
  });

  describe('fromFile', function() {
    it('should parse file and return js150 format', function(cb) {
      const expected = asts['regress-472450-04'];
      const filepath = path.join(FIXTURES_PATH, 'sources/regress-472450-04.js');
      bigcodeAST.fromFile(filepath, (err, result) => {
        expect(result).to.deep.equal(expected);
        cb(err);
      });
    });
  });

  describe('bigcodeAST', function() {
    let resultsDir;

    beforeEach(function() {
      resultsDir = temp.mkdirSync('bigcodeAST');
    });

    it('should process all files in the directory', function(cb) {
      const pattern = path.join(FIXTURES_PATH, 'sources/*.js');
      const output = path.join(resultsDir, 'files');
      bigcodeAST({output: output, input: pattern}, function(err, count) {
        expect(err).to.be.null;
        expect(count).to.equal(4);

        const failedFile = path.join(resultsDir, 'files_failed.txt');
        async.parallel([
          (cb) => fs.readFile(path.join(resultsDir, 'files.txt'), 'utf8', cb),
          (cb) => fs.readFile(path.join(resultsDir, 'files.json'), 'utf8', cb),
          (cb) => fs.readFile(failedFile, 'utf8', cb),
        ], function(err, results) {
          expect(err).to.be.null;

          const files = results[0].split('\n').filter((f) => f.length > 0);
          const parsedAsts = results[1].split('\n')
                                       .filter((f) => f.length > 0)
                                       .map(JSON.parse);

          expect(files.length).to.eq(4);
          expect(parsedAsts.length).to.eq(4);

          files.forEach((filepath, i) => {
            const fileKey = stripExtension(path.basename(filepath));
            expect(parsedAsts[i]).to.deep.eq(asts[fileKey]);
          });

          cb();
        });
      });
    });

    afterEach(function() {
      temp.cleanupSync();
    });
  });
});
