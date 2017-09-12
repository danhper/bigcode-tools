import argparse
from os import path

import pandas as pd

import tensorflow as tf
import numpy as np
import matplotlib.pyplot as plt
from sklearn.cluster import KMeans
from sklearn.decomposition import PCA


DATA_FILE = "tmp/apc-nid-nsib-anc2-chd0-debug.txt.gz"
LABELS_FILE = "tmp/vocab-nid-apc-labels.tsv"

data = pd.read_csv(DATA_FILE, names=["input", "context"], comment="#")

data

labels = pd.read_csv(LABELS_FILE, delimiter="\t")

labels.loc[[18, 42, 43]]


embeddings = tf.get_variable("embeddings", [82, 100])

saver = tf.train.Saver()
sess = tf.Session()
sess.run(embeddings.initializer)
saver.restore(sess, "./results/tf-logs/ap-nid-nsib-anc2-chd0-adam0001-100d/w2v.bin-12633823")

embeddings_arr = sess.run(embeddings.value())

norms = np.linalg.norm(embeddings_arr, axis=1)
without_outliers_indexes = np.abs(norms - np.mean(norms)) <= (np.std(norms) * 2)
without_outliers = embeddings_arr[without_outliers_indexes]


scores = [KMeans(n_clusters=i).fit(embeddings_arr).score(embeddings_arr) for i in range(1, 15)]
plt.plot(list(range(1, 15)), scores)
plt.show()


clusters = KMeans(n_clusters=5).fit_predict(embeddings_arr)


labels = pd.read_csv("./results/data/ap-nid-vocab-labels.tsv", sep="\t")
labels["Cluster"] = clusters
labels[labels.Cluster == 4]


normalized_embeddings = embeddings_arr[:]
normalized_embeddings = np.delete(normalized_embeddings, 75, axis=0)
normalized_embeddings = np.delete(normalized_embeddings, 73, axis=0)

normalized_clusters = KMeans(n_clusters=5).fit_predict(normalized_embeddings)

normalized_labels = labels.copy()
normalized_labels.drop([normalized_labels.index[75], normalized_labels.index[73]], inplace=True)
normalized_labels = normalized_labels.reset_index(drop=True)
normalized_labels["Cluster"] = normalized_clusters
normalized_labels[normalized_labels.Cluster == 4]



embeddings_2d = pca.fit_transform(normalized_embeddings)
colors = "bgrcm"
clusters = ["expr", "type&expr", "decl", "stmt", "decl&comment"]

fig, ax = plt.subplots()
for i in range(5):
    indexes = normalized_labels[normalized_labels.Cluster == i].index.values
    ax.scatter(embeddings_2d[indexes, 0], embeddings_2d[indexes, 1], c=colors[i], label=clusters[i])

ax.legend()
plt.show()
