from os import path
import gzip
from typing import List, IO, Any
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


def tokenize_files(files_pattern: str, output: str, options: dict = None) -> None:
    if options is None:
        options = {}

    files_pattern = path.expandvars(path.expanduser(files_pattern))
    files = glob(files_pattern, recursive=True)
    total_count = len(files)
    logging.info("starting to tokenize %s files", total_count)

    queue = Queue(100) # type: Queue[QueueItem]

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


def is_gunzipped(output: str) -> bool:
    return output.endswith(".gz")


def open_output(output: str) -> IO[Any]:
    if is_gunzipped(output):
        return gzip.open(output, "wb")
    else:
        return open(output, "w")


def write_results(queue: Queue, output: str, total_count: int) -> None:
    current_count = 0
    output_is_gunzipped = is_gunzipped(output)
    with open_output(output) as f:
        while True:
            item = queue.get()
            if not item:
                break
            try:
                to_serialize = {"filename": item.filename, "tokens": item.tokens}
                line = json.dumps(to_serialize, default=lambda x: x.as_dict()) + "\n"
                if output_is_gunzipped:
                    line = line.encode("utf-8")
                f.write(line)
                current_count += 1
                if current_count % 1000 == 0:
                    logging.info("progress: %s/%s", current_count, total_count)
            except Exception as e: # pylint: disable=broad-except
                logging.info("failed to write %s: %s", item.filename, str(e))
    queue.put(current_count)
