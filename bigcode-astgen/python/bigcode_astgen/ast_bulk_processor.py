import logging
import json
from multiprocessing import Queue, Pool, Process

from bigcode_astgen import glob
from bigcode_astgen import ast_generator
from bigcode_astgen.queue_item import FailedFileItem, ProcessedFileItem


def process_file_init(queue, options):
    process_file.queue = queue
    process_file.options = options


def process_file(filename):
    logging.debug("processing file %s", filename)
    try:
        ast = ast_generator.parse_file(filename, process_file.options.get("normalize", False))
        item = ProcessedFileItem(filename, ast, process_file.options)
        process_file.queue.put(item)
    except Exception as e: # pylint: disable=broad-except
        logging.debug("failed to parse %s: %s", filename, str(e))
        process_file.queue.put(FailedFileItem(filename, e))


def process_files(files_pattern, output, options=None):
    """Process all the files matched with the `files_pattern` and
    output the results in `output`

    Args:
        files_pattern: a glob pattern containing python files
        output: the path to a file without extension where to output results
    """
    if options is None:
        options = {}

    queue = Queue(100)

    files = glob.glob(files_pattern, recursive=True)
    total_count = len(files)
    logging.info("starting to parse %s files", total_count)

    write_results_process = Process(target=write_results, args=(queue, output, total_count))
    write_results_process.start()

    pool = Pool(None, process_file_init, [queue, options])
    pool.map(process_file, files)
    pool.close()
    pool.join()
    queue.put(None)
    write_results_process.join()
    logging.info("successfully processed %s files", queue.get())


def write_results(queue, output, total_count):
    failure_count = 0
    success_count = 0
    with open(output + ".json", "w") as asts, \
         open(output + ".txt", "w") as files, \
         open(output + "_failed.txt", "w") as failed_files:
        while True:
            try:
                item = queue.get()
                if not item:
                    break
                if item.success:
                    write_successed_item(item, asts, files)
                    success_count += 1
                else:
                    write_failed_item(item, failed_files)
                    failure_count += 1
                current_count = success_count + failure_count
                if current_count % 1000 == 0:
                    logging.info("progress: %s/%s", current_count, total_count)
            except Exception as e: # pylint: disable=broad-except
                logging.error("failed to write %s: %s", item.filename, e)
    queue.put(success_count)


def write_successed_item(item, asts, files):
    json.dump(item.ast, asts)
    asts.write("\n")
    files.write(item.filename)
    files.write("\n")


def write_failed_item(item, failed_files):
    failed_files.write(item.filename)
    failed_files.write("\t")
    failed_files.write(item.reason)
    failed_files.write("\n")
