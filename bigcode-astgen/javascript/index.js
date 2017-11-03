const fs     = require('fs');
const glob   = require('glob');
const async  = require('async');
const parser = require('acorn/dist/acorn_loose');
const _      = require('lodash');


function capitalizeFirstLetter(string) {
  return string.charAt(0).toUpperCase() + string.slice(1);
}

const mappers = {
  type: {
    Literal: (node) => {
      if (node.value === undefined) {
        return 'LiteralUndefined';
      }
      if (node.value === null) {
        return 'LiteralNull';
      }
      if (node.value.constructor === RegExp) {
        return 'LiteralRegExp';
      }
      const suffix = capitalizeFirstLetter(typeof node.value);
      return `Literal${suffix}`;
    },
    MemberExpression: (node) => {
      if (node.computed) {
        return 'ArrayAccess';
      }
      return 'MemberExpression';
    },
    default: (node) => node.type,
  },

  children: {
    Program: (node) => node.body,

    Property: (node) => node.value ? [node.value] : [],

    VariableDeclaration: (node) => node.declarations,
    VariableDeclarator: (node) => [node.init],
    FunctionDeclaration: (node) => {
      return [node.id].concat(node.params).concat([node.body]);
    },

    ExpressionStatement: (node) => [node.expression],
    BlockStatement: (node) => node.body,
    WhileStatement: (node) => [node.test, node.body],
    ForStatement: (node) => {
      return [
        node.init || {type: 'EmptyStatement'},
        node.test || {type: 'EmptyStatement'},
        node.update || {type: 'EmptyStatement'},
        node.body || {type: 'EmptyStatement'},
      ];
    },
    ForInStatement: (node) => [node.left, node.right, node.body],
    IfStatement: (node) => [node.test, node.consequent, node.alternate],
    TryStatement: (node) => {
      const finalizer = node.finalizer || {type: 'EmptyStatement'};
      return [node.block, node.handler, finalizer];
    },
    ReturnStatement: (node) => [node.argument],
    ThrowStatement: (node) => [node.argument],
    SwitchStatement: (node) => [node.discriminant].concat(node.cases),
    SwitchCase: (node) => {
      const consequent = {type: 'BlockStatement', body: node.consequent};
      return [node.test].concat(consequent);
    },

    UnaryExpression: (node) => [node.argument],
    BinaryExpression: (node) => [node.left, node.right],
    UpdateExpression: (node) => [node.argument],
    SequenceExpression: (node) => node.expressions,
    CallExpression: (node) => [node.callee].concat(node.arguments),
    NewExpression: (node) => [node.callee].concat(node.arguments),
    FunctionExpression: (node) => node.params.concat([node.body]),
    ObjectExpression: (node) => node.properties,
    MemberExpression: (node) => {
      let property = node.property;
      if (property.type === 'Identifier') {
        property = Object.assign({}, property, {type: 'Property'});
      }
      return [node.object, property];
    },
    ArrayExpression: (node) => node.elements,
    AssignmentExpression: (node) => [node.left, node.right],
    ConditionalExpression: (node) => [node.test, node.consequent,
                                      node.alternate],
    LogicalExpression: (node) => [node.left, node.right],

    CatchClause: (node) => [node.param, node.body],
    default: (_node) => [],
  },

  value: {
    Identifier: (node) => node.name,
    Literal: (node) => {
      if (typeof node.value === 'undefined') {
        return null;
      }
      if (node.value === null) {
        return 'null';
      }
      return node.value.toString();
    },
    Property: (node) => {
      return node.name || (node.key && node.key.name || node.key.value);
    },

    VariableDeclarator: (node) => node.id.name,

    UnaryExpression: (node) => node.operator,
    BinaryExpression: (node) => node.operator,
    LogicalExpression: (node) => node.operator,
    UpdateExpression: (node) => {
      return node.prefix ? `${node.operator}?` : `?${node.operator}`;
    },

    default: (_node) => null,
  },
};

function get(property) {
  const propertyMapper = mappers[property];
  return function(node) {
    const mapper = propertyMapper[node.type];
    if (mapper) {
      return mapper(node);
    }
    return propertyMapper.default(node);
  };
}

const getType = get('type');
const getValue = get('value');
const getChildren = get('children');


class ASTGenerator {
  constructor(root) {
    this.root = root;
  }

  createAST() {
    this.nodes = [];
    this.traverse(this.root);
    return this.nodes;
  }

  computeChildrenIds(astNode) {
    const children = getChildren(astNode);

    if (!children || children.length === 0) {
      return [];
    }

    const childrenIds = [];
    for (const child of children) {
      const childId = this.traverse(child);
      if (childId) {
        childrenIds.push(childId);
      }
    }
    return childrenIds;
  }

  traverse(astNode) {
    if (!astNode) {
      return;
    }

    const node = {id: this.nodes.length, type: getType(astNode)};
    this.nodes.push(node);

    const value = getValue(astNode);
    if (value !== null) {
      node.value = value;
    }

    const childrenIds = this.computeChildrenIds(astNode);
    if (childrenIds.length > 0) {
      node.children = childrenIds;
    }

    return node.id;
  }
}

function generateAndOuputAST(filepath, streams, options, callback) {
  bigcodeASTGen.fromFile(filepath, function(err, ast) {
    if (!err && ast.length < options.minNodes) {
      err = 'too few nodes';
    }
    if (!err && ast.length > options.maxNodes) {
      err = 'too many nodes';
    }
    if (err) {
      streams.failedFiles.write(filepath + '\t' + err + '\n');
      return callback(null);
    }
    streams.asts.write(JSON.stringify(ast));
    streams.asts.write('\n');
    streams.files.write(filepath + '\n');
    callback(null);
  });
}

function waitForStream(stream, callback) {
  stream.on('open', () => callback()).on('error', (err) => callback(err));
}

function createStreams(output, callback) {
  const streams = {
    asts: fs.createWriteStream(output + '.json'),
    files: fs.createWriteStream(output + '.txt'),
    failedFiles: fs.createWriteStream(output + '_failed.txt'),
  };
  async.each(streams, waitForStream, (err) => callback(err, streams));
}

function processFiles(files, streams, options, callback) {
  console.log(`starting to process ${files.length} files`);

  const processFile = (file, index, cb) => {
    if (index % 1000 === 0) {
      console.log(`progress: ${index}/${files.length}`);
    }
    generateAndOuputAST(file, streams, options, cb);
  };
  async.eachOfLimit(files, 30, processFile, (err) => {
    if (err) {
      return callback(err);
    }

    async.each(streams, (stream, cb) => stream.end(cb), (err) => {
      callback(err, files.length);
    });
  });
}

function bigcodeASTGen(options, callback) {
  glob(options.input, {nodir: true}, function(err, files) {
    if (err) {
      return callback(err);
    }

    createStreams(options.output, function(err, streams) {
      if (err) {
        return callback(err);
      }
      const processOptions = _.take(options, ['minNodes', 'maxNodes']);
      processFiles(files, streams, processOptions, callback);
    });
  });
}

module.exports = bigcodeASTGen;

bigcodeASTGen.fromNode = function(root) {
  return new ASTGenerator(root).createAST();
};

bigcodeASTGen.fromString = function(content) {
  if (typeof content !== 'string') {
    throw new Error('bad argument passed to fromString: ' + content);
  }

  const root = parser.parse_dammit(content);
  return bigcodeASTGen.fromNode(root);
};

bigcodeASTGen.fromFile = function(path, callback) {
  fs.readFile(path, 'utf-8', (err, content) => {
    if (err !== null) {
      return callback(err);
    }
    try {
      callback(null, bigcodeASTGen.fromString(content));
    } catch (e) {
      callback(e);
    }
  });
};

bigcodeASTGen.processFile = function(path, output, callback) {
  bigcodeASTGen.fromFile(path, (err, ast) => {
    if (err !== null) {
      return callback(err);
    }
    const jsonAST = JSON.stringify(ast);
    if (output) {
      fs.writeFile(output, jsonAST, 'utf8', callback);
    } else {
      console.log(jsonAST);
    }
  });
};
