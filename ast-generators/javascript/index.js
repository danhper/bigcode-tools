const fs     = require('fs');
const path   = require('path');
const glob   = require('glob');
const async  = require('async');
const parser = require('acorn/dist/acorn_loose');


function capitalizeFirstLetter(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
}

const mappers = {
  type: {
    Literal: (node) => {
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


function computeChildrenIds(astNode, nodes) {
  const children = getChildren(astNode);

  if (!children || children.length === 0) {
    return [];
  }

  const childrenIds = [];
  for (const child of children) {
    const childId = createAST(child, nodes);
    if (childId) {
      childrenIds.push(childId);
    }
  }
  return childrenIds;
}

function createAST(astNode, nodes) {
  if (!astNode) {
    return;
  }

  const node = {id: nodes.length, type: getType(astNode)};
  nodes.push(node);

  const value = getValue(astNode);
  if (value !== null) {
    node.value = value;
  }

  const childrenIds = computeChildrenIds(astNode, nodes);
  if (childrenIds.length > 0) {
    node.children = childrenIds;
  }

  return node.id;
}

function generateAndOuputAST(filepath, streams, callback) {
  bigcodeAST.fromFile(filepath, function(err, ast) {
    if (err) {
      return callback(err);
    }
    streams.asts.write(JSON.stringify(ast));
    streams.asts.write('\n');
    streams.files.write(filepath + '\n');
    callback(null);
  });
}


function makeStreamCallbacks(streams, callback) {
  let callbackCalled = false;
  let successCount = 0;

  const successCallback = function() {
    successCount += 1;
    if (successCount >= 2 && !callbackCalled) {
      callbackCalled = true;
      return callback(null, streams);
    }
  };

  const errorCallback = function(err) {
    if (!callbackCalled) {
      callbackCalled = true;
      return callback(err);
    }
  };

  return {success: successCallback, error: errorCallback};
}

function createStreams(outputDir, callback) {
  const streams = {
    asts: fs.createWriteStream(path.join(outputDir, 'asts.json')),
    files: fs.createWriteStream(path.join(outputDir, 'files.txt')),
  };
  const callbacks = makeStreamCallbacks(streams, callback);
  Object.keys(streams).forEach((key) => {
    streams[key].on('open', callbacks.success);
    streams[key].on('error', callbacks.error);
  });
}

function processFiles(files, streams, callback) {
  const processFile = (file, cb) => generateAndOuputAST(file, streams, cb);
  async.each(files, processFile, (err) => {
    if (err) {
      return callback(err);
    }

    async.each(streams, (stream, cb) => stream.end(cb), (err) => {
      callback(err, files.length);
    });
  });
}

function bigcodeAST(options, callback) {
  glob(options.files, function(err, files) {
    if (err) {
      return callback(err);
    }

    createStreams(options.outputDir, function(err, streams) {
      if (err) {
        return callback(err);
      }
      processFiles(files, streams, callback);
    });
  });
}

module.exports = bigcodeAST;

bigcodeAST.fromNode = function(root) {
  const nodes = [];
  createAST(root, nodes);
  return nodes;
};

bigcodeAST.fromString = function(content) {
  if (typeof content !== 'string') {
    throw new Error('bad argument passed to fromString: ' + content);
  }

  const root = parser.parse_dammit(content);
  return bigcodeAST.fromNode(root);
};

bigcodeAST.fromFile = function(path, callback) {
  fs.readFile(path, 'utf-8', (err, content) => {
    if (err !== null) {
      return callback(err);
    }
    callback(null, bigcodeAST.fromString(content));
  });
};
