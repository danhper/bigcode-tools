import argparse
import logging

from bigcode_tokenizer import parallel_tokenizer


def run():
    parser = argparse.ArgumentParser("bigcode-tokenizer")
    parser.add_argument("input", help="file or glob pattern of files to parse")
    parser.add_argument("-o", "--output", help="output file")
    parser.add_argument("-v", "--verbose", help="increase verbosity", action="count", default=0)
    parser.add_argument(
        "--include-comments", help="include comments",
        action="store_false", dest="skip_comments")
    parser.add_argument(
        "--include-text", help="include text (whitespaces, etc)",
        action="store_false", dest="skip_text")

    args = parser.parse_args()
    loglevel = logging.INFO - (10 * args.verbose)
    logging.basicConfig(level=loglevel)


    options = {k: getattr(args, k) for k in ["skip_text", "skip_comments"]}
    parallel_tokenizer.tokenize_files(args.input, args.output, options)
