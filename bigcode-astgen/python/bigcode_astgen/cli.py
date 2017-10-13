import argparse
import logging

from bigcode_astgen import ast_bulk_processor


def run():
    parser = argparse.ArgumentParser("bigcode-ast-py")
    parser.add_argument("-f", "--files", help="glob pattern of files to parse", required=True)
    parser.add_argument("-o", "--output", help="output file without extension", required=True)
    parser.add_argument(
        "--min-nodes", help="minimum number of nodes per file", default=20, type=int)
    parser.add_argument(
        "--max-nodes", help="maximum number of nodes per file", default=50000, type=int)
    parser.add_argument("-v", "--verbose", help="increase verbosity", action="count", default=0)
    args = parser.parse_args()
    loglevel = logging.INFO - (10 * args.verbose)
    logging.basicConfig(level=loglevel)
    options = {k: getattr(args, k) for k in ["min_nodes", "max_nodes"]}
    ast_bulk_processor.process_files(args.files, args.output, options)
