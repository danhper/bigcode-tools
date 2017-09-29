const fs     = require('fs');
const path   = require('path');
const expect = require('chai').expect;

const astGenerator = require('..');

const FIXTURES_PATH = path.join(__dirname, 'fixtures');

function stripExtension(filename) {
  return filename.replace(path.extname(filename), '');
};

describe('bigcode-ast', function() {
  describe('fromString', function() {
    const asts = {};
    const sources = {};

    function generateContent(object, directoryName, options = {}) {
      const files = fs.readdirSync(path.join(FIXTURES_PATH, directoryName));
      files.forEach((filename) => {
        const filepath = path.join(FIXTURES_PATH, directoryName, filename);
        const key = stripExtension(filename);
        const content = fs.readFileSync(filepath, 'utf-8');
        object[key] = options.json ? JSON.parse(content) : content;
      });
    }

    before(function() {
      generateContent(asts, 'asts', {json: true});
      generateContent(sources, 'sources');
    });

    it('should return js150 format', function() {
      Object.keys(asts).forEach((key) => {
        const expected = asts[key];
        const result = astGenerator.fromString(sources[key]);
        if (expected[expected.length - 1] === 0) {
          expected.splice(expected.length - 1, 1);
        }

        expect(result).to.deep.equal(expected);
      });
    });
  });
});
