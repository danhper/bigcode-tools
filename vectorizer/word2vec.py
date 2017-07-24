import math
import argparse
import threading
import time
import multiprocessing
import queue
import io
import gzip
from concurrent.futures import ThreadPoolExecutor

import tensorflow as tf


class DataReader:
    def __init__(self, input_file):
        with gzip.open(input_file, "rb") as f:
            self._raw_data = f.read()
        self.reset_input()

    def reset_input(self):
        self.lines = io.BytesIO(self._raw_data)

    def next_batch(self, batch_size):
        inputs, labels = [], []
        for _ in range(batch_size):
            datum = self.next_datum()
            # TODO: maybe handle last batch by padding data
            if not datum:
                return
            inputs.append(datum[0])
            labels.append(datum[1])
        return inputs, labels

    def next_datum(self):
        try:
            line = next(self.lines).decode("ascii")
            return [int(n) for n in line.strip().split(",")]
        except StopIteration:
            pass


class Word2VecOptions:
    def __init__(self, options):
        self.input_file = options["input_file"]
        self.vocab_size = options["vocab_size"]
        self.emb_size = options["emb_size"]
        self.batch_size = options["batch_size"]
        self.num_sampled = options["num_sampled"]
        self.output = options["output"]
        self.threads_count = options["threads_count"]
        self.epochs = options["epochs"]


class Word2Vec:
    def __init__(self, session, data, options):
        self.options = options
        self._session = session
        self._create_graph()
        self._data = data
        self._data_queue = queue.Queue(maxsize=options.threads_count * 2)

    def _create_graph(self):
        opts = self.options

        embeddings = tf.Variable(
            tf.random_uniform([opts.vocab_size, opts.emb_size],
                              -0.5 / opts.emb_size, 0.5 / opts.emb_size))
        nce_weights = tf.Variable(
            tf.truncated_normal([opts.vocab_size, opts.emb_size],
                                stddev=1.0 / math.sqrt(opts.emb_size)))
        nce_biases = tf.Variable(tf.zeros([opts.vocab_size]))

        train_inputs = tf.placeholder(tf.int32, shape=[opts.batch_size])
        train_labels = tf.placeholder(tf.int32, shape=[opts.batch_size, 1])

        embed = tf.nn.embedding_lookup(embeddings, train_inputs)

        global_step = tf.Variable(0, name="global_step")

        inc = global_step.assign_add(1)

        loss = tf.reduce_mean(
            tf.nn.nce_loss(weights=nce_weights,
                           biases=nce_biases,
                           labels=train_labels,
                           inputs=embed,
                           num_sampled=opts.num_sampled,
                           num_classes=opts.vocab_size)
        )

        lr = opts.learning_rate
        with tf.control_dependencies([inc]):
            optimizer = tf.train.GradientDescentOptimizer(learning_rate=lr).minimize(loss)

        self.global_step = global_step
        self.loss = loss
        self.embeddings = embeddings
        self.optimizer = optimizer

        self._session.run(tf.global_variables_initializer())

        self.saver = tf.train.Saver()

    def _produce_data(self):
        self._data.reset_input()
        while True:
            batch = self._data.next_batch(self.options.batch_size)
            if not batch:
                break
            self._data_queue.put(batch)

    def train(self):
        producer_thread = threading.Thread(target=self._produce_data)
        producer_thread.start()
        with ThreadPoolExecutor(max_workers=self.options.threads_count) as executor:
            future = executor.submit(self._train_thread)
            while not future.done():
                time.sleep(5)

    def _train_thread(self):
        while True:
            try:
                inputs, labels = self._data_queue.get()
                self._train_batch(tf.stack(inputs), tf.stack(labels))
            except queue.Empty:
                return

    def _train_batch(self, inputs, labels):
        feed_dict = {"train_inputs": inputs, "train_labels": labels}
        _, cur_loss = self._session.run([self.optimizer, self.loss], feed_dict=feed_dict)
        return cur_loss


def create_parser():
    parser = argparse.ArgumentParser(prog="word2vec")
    parser.add_argument("-i", "--input-file", required=True)
    parser.add_argument("-o", "--output", required=True)
    parser.add_argument("--vocab-size", type=int, required=True)
    parser.add_argument("--emb-size", type=int, default=100)
    parser.add_argument("--batch-size", type=int, default=1024)
    parser.add_argument("--num-sampled", type=int, default=10)
    parser.add_argument("--epochs", type=int, default=5)
    parser.add_argument("--threads-count", type=int, default=multiprocessing.cpu_count())
    return parser


def train(namespace):
    graph = tf.Graph()
    options = Word2VecOptions(vars(namespace))
    data_reader = DataReader(options.input_file)
    with graph.as_default(), tf.Session() as session:
        model = Word2Vec(session, data_reader, options)
        for _ in range(options.epochs):
            model.train()
            model.saver.save(session, options.output, global_step=model.global_step)
