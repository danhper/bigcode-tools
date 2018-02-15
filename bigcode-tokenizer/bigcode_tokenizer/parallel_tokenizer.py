from os import path
from typing import List
import json
import logging
from glob import glob
from multiprocessing import Queue, Pool, Process

from bigcode_tokenizer import tokenizer
from bigcode_tokenizer.token import Token


class QueueItem:
    def __init__(self, filename: str, tokens: List[Token]) -> None:
        self.filename = filename
        self.tokens = tokens


def process_file_init(queue, options: dict):
    process_file.queue = queue
    process_file.options = options


def process_file(filename):
    logging.debug("processing file %s", filename)
    try:
        tokens = tokenizer.tokenize_file(filename, process_file.options)
        process_file.queue.put(QueueItem(filename, tokens))
    except Exception as e: # pylint: disable=broad-except
        logging.error("failed to process %s: %s", filename, str(e))


def tokenize_files(files_glob: str, output: str, options: dict = None) -> None:
    if options is None:
        options = {}

    files = path.realpath(files_glob)
    files = glob(files_glob, recursive=True)
    total_count = len(files)
    logging.info("starting to tokenize %s files", total_count)

    queue = Queue(100)

    write_results_process = Process(target=write_results, args=(queue, output, total_count))
    write_results_process.start()

    pool = Pool(None, process_file_init, [queue, options])
    pool.map(process_file, files)
    pool.close()
    pool.join()
    logging.debug("pool done, waiting for write results process")
    queue.put(None)
    write_results_process.join()
    logging.info("successfully processed %s files", queue.get())


def write_results(queue: Queue, output: str, total_count: int) -> None:
    current_count = 0
    with open(output, "w") as f:
        while True:
            item = queue.get()
            logging.debug("RECEIVED ITEM: %s", item)
            if not item:
                break
            try:
                json.dump(item.tokens, f, default=lambda x: x.as_dict())
                f.write("\n")
                current_count += 1
                if current_count % 1000 == 0:
                    logging.info("progress: %s/%s", current_count, total_count)
            except Exception as e: # pylint: disable=broad-except
                logging.info("failed to write %s: %s", item.filename, str(e))
    queue.put(current_count)
