import pandas as pd

import tensorflow as tf
import numpy as np
import matplotlib.pyplot as plt
from sklearn.cluster import KMeans
from sklearn.decomposition import PCA


embeddings = tf.get_variable("embeddings", [82, 100])

saver = tf.train.Saver()
sess = tf.Session()
sess.run(embeddings.initializer)
saver.restore(sess, "./results/tf-logs/ap-nid-nsib-anc2-chd0-adam0001-100d/w2v.bin-12633823")

embeddings_arr = sess.run(embeddings.value())

norms = np.linalg.norm(embeddings_arr, axis=1)
without_outliers_indexes = np.abs(norms - np.mean(norms)) <= (np.std(norms) * 2)
without_outliers = embeddings_arr[without_outliers_indexes]


scores = [KMeans(n_clusters=i).fit(without_outliers).score(without_outliers) for i in range(1, 15)]
plt.plot(list(range(1, 15)), scores)
plt.show()


labels = pd.read_csv("./results/data/ap-nid-vocab-labels.tsv", sep="\t")
labels = labels.iloc[labels.index[without_outliers_indexes]].reset_index(drop=True)

pca = PCA(n_components=2)
embeddings_2d = pca.fit_transform(without_outliers)

CLUSTERS_COUNT = 6

clusters = KMeans(n_clusters=CLUSTERS_COUNT, max_iter=30000).fit_predict(without_outliers)

labels["Cluster"] = clusters

colors = "bgrcmky"

fig = plt.figure(figsize=(30, 30))
ax = fig.add_subplot(111)
for i in range(CLUSTERS_COUNT):
    indexes = labels[labels.Cluster == i].index.values
    ax.scatter(embeddings_2d[indexes, 0], embeddings_2d[indexes, 1], c=colors[i])
    for j in indexes:
        ax.annotate(labels.loc[j].Name, (embeddings_2d[j, 0], embeddings_2d[j, 1]))

plt.show()
fig.savefig('clusters.png')
