import argparse
import logging

from bigcode_tokenizer import parallel_tokenizer


OPTION_ATTRS = ["skip_text", "skip_comments", "tokenizer", "max_len"]


def run():
    parser = argparse.ArgumentParser("bigcode-tokenizer")
    parser.add_argument("input", help="file or glob pattern of files to parse")
    parser.add_argument("-o", "--output", help="output file")
    parser.add_argument("-v", "--verbose", help="increase verbosity", action="count", default=0)
    parser.add_argument(
        "--tokenizer", help="tokenizer to use (infers from extension name if not passed)")
    parser.add_argument(
        "--include-comments", help="include comments",
        action="store_false", dest="skip_comments")
    parser.add_argument(
        "--include-text", help="include text (whitespaces, etc)",
        action="store_false", dest="skip_text")
    parser.add_argument(
        "--max-len", help="max length files to be processed",
        default=50000, type=int)

    args = parser.parse_args()
    log_level = logging.INFO - (10 * args.verbose)
    logging.basicConfig(level=log_level,
                        format="%(asctime)-15s %(levelname)s %(message)s")


    options = {k: getattr(args, k) for k in OPTION_ATTRS}
    parallel_tokenizer.tokenize_files(args.input, args.output, options)
