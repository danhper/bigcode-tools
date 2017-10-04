import logging
import json
import glob
from multiprocessing import Queue, Pool, Process, queues
from os import path

from bigcode_ast import ast_generator


def process_file_init(queue):
    process_file.queue = queue


def process_file(filename):
    logging.debug("processing file %s", filename)
    try:
        ast = ast_generator.parse_file(filename)
        process_file.queue.put((filename, ast))
    except Exception as e: # pylint: disable=broad-except
        logging.error("failed to parse %s: %s", filename, str(e))


def process_files(files_pattern, output_dir):
    """Process all the files matched with the `files_pattern` and
    output the results in `output_dir`

    Args:
        files_pattern: a glob pattern containing python files
        output_dir: the path to a directory where to output results
    """
    queue = Queue()
    write_results_process = Process(target=write_results, args=(queue, output_dir))
    write_results_process.start()

    files = glob.glob(files_pattern)
    pool = Pool(None, process_file_init, [queue])
    pool.map(process_file, files)
    pool.close()
    write_results_process.join()


def write_results(queue, output_dir):
    with open(path.join(output_dir, "asts.json"), "w") as asts, \
         open(path.join(output_dir, "files.txt"), "w") as files:
        while True:
            try:
                filename, ast = queue.get(timeout=0.3)
                json.dump(ast, asts)
                asts.write("\n")

                files.write(filename)
                files.write("\n")
            except queues.Empty:
                break
