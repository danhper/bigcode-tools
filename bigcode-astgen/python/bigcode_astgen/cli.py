import sys
import json
import argparse
import logging

from bigcode_astgen import ast_bulk_processor, ast_generator


def run_parse_file(args):
    try:
        result = ast_generator.parse_file(args.input, args.normalize)
    except Exception as e: # pylint: disable=broad-except
        sys.stderr.write("could not parse {0}: {1}\n".format(args.input, e))
        return False
    if args.output:
        with open(args.output, "w") as f:
            json.dump(result, f)
    else:
        print(json.dumps(result))
    return True


def run():
    parser = argparse.ArgumentParser("bigcode-ast-py")
    parser.add_argument("input", help="file or glob pattern of files to parse")
    parser.add_argument(
        "-o", "--output", help="output file for normal mode, output prefix for batch mode")
    parser.add_argument(
        "-b", "--batch", help="process a batch of files. input will be treated as a glob",
        action="store_true", default=False)
    parser.add_argument("-N", "--no-normalize", help="does not normalize the AST",
                        default=True, action="store_false", dest="normalize")
    parser.add_argument(
        "--min-nodes", help="minimum number of nodes per file (batch mode only)",
        default=20, type=int)
    parser.add_argument(
        "--max-nodes", help="maximum number of nodes per file (batch mode only)",
        default=50000, type=int)
    parser.add_argument("-v", "--verbose", help="increase verbosity", action="count", default=0)

    args = parser.parse_args()
    loglevel = logging.INFO - (10 * args.verbose)
    logging.basicConfig(level=loglevel)

    if args.batch and not args.output:
        parser.error("--batch requires --output to be set")

    if args.batch:
        options = {k: getattr(args, k) for k in ["min_nodes", "max_nodes", "normalize"]}
        ast_bulk_processor.process_files(args.input, args.output, options)
    else:
        success = run_parse_file(args)
        sys.exit(0 if success else 1)
