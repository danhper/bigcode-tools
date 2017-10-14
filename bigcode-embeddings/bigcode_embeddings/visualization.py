import numpy as np
import matplotlib.pyplot as plt
import plotly
import plotly.graph_objs as go
from sklearn.cluster import KMeans
from sklearn.decomposition import PCA

from bigcode_embeddings import model_utils


DEFAULT_CLUSTERS_COUNT = 6
DEFAULT_MAX_CLUSTERS = 10

SVG_COLORS = [
    "#1f77b4",  # muted blue
    "#ff7f0e",  # safety orange
    "#2ca02c",  # cooked asparagus green
    "#d62728",  # brick red
    "#9467bd",  # muted purple
    "#8c564b",  # chestnut brown
    "#e377c2",  # raspberry yogurt pink
    "#7f7f7f",  # middle gray
    "#bcbd22",  # curry yellow-green
    "#17becf",  # blue-teal
]


def sanitize_data(embeddings, labels):
    norms = np.linalg.norm(embeddings, axis=1)
    valid_indexes = np.abs(norms - np.mean(norms)) <= (np.std(norms) * 2)
    sanitized_embeddings = embeddings[valid_indexes]
    sanitized_labels = labels.iloc[labels.index[valid_indexes]].reset_index(drop=True)
    return (sanitized_embeddings, sanitized_labels)


def create_elbow_graph(embeddings, max_clusters=DEFAULT_MAX_CLUSTERS, output=None):
    scores = [KMeans(n_clusters=i).fit(embeddings).score(embeddings)
              for i in range(1, max_clusters)]
    plt.plot(list(range(1, max_clusters)), scores)
    if output:
        plt.savefig(output)
    else:
        plt.show()


def reduce_dimensions(embeddings, dimensions=2):
    pca = PCA(n_components=dimensions)
    return pca.fit_transform(embeddings)


def assign_clusters(embeddings, labels, clusters_count=DEFAULT_CLUSTERS_COUNT):
    clusters = KMeans(n_clusters=clusters_count, max_iter=30000).fit_predict(embeddings)
    labels["Cluster"] = clusters


def compute_clusters_count(labels):
    return labels.Cluster.max()


def create_scatter_plot(embeddings_2d, labels, output=None):
    clusters_count = compute_clusters_count(labels)

    fig = plt.figure(figsize=(20, 20))
    axis = fig.add_subplot(111)
    for i in range(clusters_count):
        indexes = labels[labels.Cluster == i].index.values
        axis.scatter(embeddings_2d[indexes, 0], embeddings_2d[indexes, 1], c="C{0}".format(i))
        for j in indexes:
            label = labels.loc[j].value if "value" in labels.columns else labels.loc[j].type
            axis.annotate(label, (embeddings_2d[j, 0], embeddings_2d[j, 1]), fontsize=8)

    if output:
        fig.savefig(output)
    else:
        plt.show()


def create_interactive_scatter_plot(embeddings_2d, labels, output=None):
    clusters_count = compute_clusters_count(labels)
    data = []
    for i in range(clusters_count):
        indexes = labels[labels.Cluster == i].index.values
        label_column = "value" if "value" in labels.columns else "type"
        trace = go.Scatter(
            x=embeddings_2d[indexes, 0],
            y=embeddings_2d[indexes, 1],
            mode="markers",
            text=labels.loc[indexes][label_column].values,
            marker={"color": SVG_COLORS[i]}
        )
        data.append(trace)

    kwargs = {"filename": output} if output else {}
    plotly.offline.plot(data, **kwargs)


def visualize_clusters(options):
    labels = model_utils.load_labels(options.labels_path)
    embeddings = model_utils.load_embeddings(options.model_path)
    embeddings, labels = sanitize_data(embeddings, labels)
    assign_clusters(embeddings, labels, options.clusters_count)
    embeddings_2d = reduce_dimensions(embeddings)
    if options.interactive:
        create_interactive_scatter_plot(embeddings_2d, labels, options.output)
    else:
        create_scatter_plot(embeddings_2d, labels, options.output)


def visualize_elbow_graph(options):
    embeddings = model_utils.load_embeddings(options.model_path)
    create_elbow_graph(embeddings, max_clusters=options.max_clusters, output=options.output)
