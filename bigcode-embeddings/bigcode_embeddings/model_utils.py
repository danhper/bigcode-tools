import pandas as pd
import tensorflow as tf
import numpy as np


def memoize(f):
    memoized = {}
    def wrapper(*args):
        tupled_args = tuple(args)
        if tupled_args not in memoized:
            memoized[tupled_args] = f(*args)
        return memoized[tupled_args]
    return wrapper


@memoize
def load_model(model_path):
    metadata_path = model_path + ".meta"
    saver = tf.train.import_meta_graph(metadata_path)
    sess = tf.Session()
    saver.restore(sess, model_path)
    return sess


def load_embeddings(model_path, embeddings_name="embeddings:0"):
    sess = load_model(model_path)
    return sess.run(sess.graph.get_tensor_by_name(embeddings_name))


def load_labels(labels_path):
    return pd.read_csv(labels_path, sep="\t")


def export_embeddings(model_path, output):
    embeddings = load_embeddings(model_path)
    if output.endswith(".txt"):
        np.savetxt(output, embeddings)
    else:
        np.save(output, embeddings)
