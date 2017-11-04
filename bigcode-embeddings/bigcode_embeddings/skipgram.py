import threading
import queue
import io
import gzip
import itertools
from concurrent.futures import ThreadPoolExecutor
from os import path

import tensorflow as tf
import numpy as np


OPTIMIZERS = {
    "gradient-descent" : tf.train.GradientDescentOptimizer,
    "adam"             : tf.train.AdamOptimizer,
    "rmsprop"          : tf.train.RMSPropOptimizer
}


class DataReader:
    def __init__(self, input_files):
        self._raw_data = []
        for input_file in input_files:
            with gzip.open(input_file, "rb") as f:
                self._raw_data.append(f.read())
        self.lines = None
        self.reset_input()
        self._count_inputs()
        self.reset_input()

    def reset_input(self):
        self.lines = itertools.chain(*[io.BytesIO(data) for data in self._raw_data])

    def _count_inputs(self):
        inputs_count = 0
        for _ in self.lines:
            inputs_count += 1
        self.inputs_count = inputs_count

    def next_batch(self, batch_size):
        inputs, labels = [], []
        for _ in range(batch_size):
            datum = self.next_datum()
            if not datum:
                break
            inputs.append(datum[0])
            labels.append(datum[1])
        if not inputs:
            return
        return inputs, labels

    def next_datum(self):
        try:
            line = next(self.lines).decode("ascii")
            return [int(n) for n in line.strip().split(",")]
        except StopIteration:
            pass


class SkipgramOptions:
    def __init__(self, options):
        self.inputs = options["inputs"]
        self.vocab_size = options["vocab_size"]
        self.emb_size = options["emb_size"]
        self.batch_size = options["batch_size"]
        self.num_sampled = options["num_sampled"]
        self.output_dir = options["output_dir"]
        self.threads_count = options["threads_count"]
        self.epochs = options["epochs"]
        self.learning_rate = options["learning_rate"]
        self.l2_value = options["l2_value"]
        self.optimizer = options["optimizer"]
        self.no_clipping = options["no_clipping"]
        self.checkpoint = options["checkpoint"]


class Skipgram:
    def __init__(self, session, data, options):
        self.options = options
        self._session = session
        self._create_graph()
        self._initialize()
        self._data = data
        self._data_queue = queue.Queue(maxsize=options.threads_count * 2)

    def _initialize(self):
        if self.options.checkpoint:
            self.saver.restore(self._session, self.options.checkpoint)
        else:
            self._session.run(tf.global_variables_initializer())

    def _create_graph(self):
        opts = self.options

        embeddings = tf.Variable(
            tf.random_uniform([opts.vocab_size, opts.emb_size],
                              -0.5 / opts.emb_size, 0.5 / opts.emb_size),
            name="embeddings")
        nce_weights = tf.Variable(
            tf.truncated_normal([opts.vocab_size, opts.emb_size],
                                stddev=1.0 / np.sqrt(opts.emb_size)),
            name="nce_weights")
        nce_biases = tf.Variable(tf.zeros([opts.vocab_size]), name="nce_biases")

        train_inputs = tf.placeholder(tf.int32, shape=[None])
        train_labels = tf.placeholder(tf.int32, shape=[None, 1])

        embed = tf.nn.embedding_lookup(embeddings, train_inputs)

        total_loss = tf.Variable(0, dtype=tf.float32, name="total_loss", trainable=False)
        reset_loss = total_loss.assign(tf.constant(0, dtype=tf.float32), use_locking=True)

        global_step = tf.Variable(0, name="global_step", trainable=False)
        temporary_step = tf.Variable(0, name="temporary_step", trainable=False)

        epoch = tf.Variable(0, name="epoch", trainable=False)
        inc_epoch = epoch.assign_add(1)

        average_loss = tf.divide(total_loss, tf.cast(temporary_step, tf.float32))

        inc_global_step = global_step.assign_add(1)
        inc_temporary_step = temporary_step.assign_add(1)
        reset_temporary_step = tf.assign(temporary_step, tf.constant(0), use_locking=True)

        loss = tf.reduce_mean(
            tf.nn.nce_loss(weights=nce_weights,
                           biases=nce_biases,
                           labels=train_labels,
                           inputs=embed,
                           num_sampled=opts.num_sampled,
                           num_classes=opts.vocab_size)
        )

        if opts.l2_value > 0.0:
            regularization_value = opts.l2_value * tf.nn.l2_loss(nce_weights) / opts.batch_size
            loss += regularization_value

        update_loss = tf.assign_add(total_loss, loss)

        optimizer_class = OPTIMIZERS[opts.optimizer]
        with tf.control_dependencies([inc_global_step, inc_temporary_step, update_loss]):
            optimizer = optimizer_class(learning_rate=opts.learning_rate)
            grads_and_vars = optimizer.compute_gradients(loss)
            if not opts.no_clipping:
                grads_and_vars = [(g, v) if g is None else (tf.clip_by_value(g, -1., 1.), v) for (g, v) in grads_and_vars]
            optimization_step = optimizer.apply_gradients(grads_and_vars)

        tf.summary.scalar("loss", average_loss)
        merged_summary = tf.summary.merge_all()
        train_writer = tf.summary.FileWriter(self.options.output_dir, self._session.graph)

        self.global_step = global_step
        self.loss = loss
        self.embeddings = embeddings
        self.optimization_step = optimization_step
        self.train_inputs = train_inputs
        self.train_labels = train_labels
        self.merged_summary = merged_summary
        self.train_writer = train_writer
        self.average_loss = average_loss
        self.reset_loss = reset_loss
        self.temporary_step = temporary_step
        self.reset_temporary_step = reset_temporary_step
        self.epoch = epoch
        self.inc_epoch = inc_epoch
        self.saver = tf.train.Saver()

    def _produce_data(self):
        self._data.reset_input()
        while True:
            batch = self._data.next_batch(self.options.batch_size)
            if not batch:
                break
            self._data_queue.put(batch)

    def train(self):
        self._session.run(self.inc_epoch)
        producer_thread = threading.Thread(target=self._produce_data)
        producer_thread.start()
        with ThreadPoolExecutor(max_workers=self.options.threads_count) as executor:
            for i in range(self.options.threads_count):
                record_results = i == 0
                executor.submit(self._train_thread, record_results=record_results)
        producer_thread.join(timeout=1)

    def _train_thread(self, record_results=False):
        while True:
            try:
                inputs, labels = self._get_batch()
                self._train_batch(inputs, labels, record_results=record_results)
            except queue.Empty:
                break
            except Exception as e:
                print(e)
                raise e
        if record_results:
            self._record_step()

    def _get_batch(self):
        inputs, labels = self._data_queue.get(timeout=1)
        return np.array(inputs), np.array(labels).reshape((len(labels), 1))

    def _train_batch(self, inputs, labels, record_results):
        feed_dict = {self.train_inputs: inputs, self.train_labels: labels}
        _, tmp_step = self._session.run(
            [self.optimization_step, self.temporary_step], feed_dict=feed_dict)
        if tmp_step > 10000 and record_results:
            self._record_step()

    def _record_step(self):
        epoch, current_step, loss, summary = self._session.run([
            self.epoch, self.global_step, self.average_loss, self.merged_summary
        ])
        self.train_writer.add_summary(summary, current_step)
        progress = (current_step * self.options.batch_size) / self._data.inputs_count % 1 * 100
        print("epoch: {0}, step: {1}, loss: {2:7.4f}, progress: {3:.2f}%".format(
            epoch, current_step, loss, progress))
        self._session.run([self.reset_loss, self.reset_temporary_step])


def train(options):
    graph = tf.Graph()
    data_reader = DataReader(options.inputs)
    output_file = path.join(options.output_dir, "embeddings.bin")
    with graph.as_default(), tf.Session() as session:
        model = Skipgram(session, data_reader, options)
        current_epoch = session.run(model.epoch)
        while current_epoch < options.epochs:
            model.train()
            model.saver.save(session, output_file, global_step=model.global_step)
            current_epoch = session.run(model.epoch)


def run(namespace):
    options = SkipgramOptions(vars(namespace))
    train(options)
