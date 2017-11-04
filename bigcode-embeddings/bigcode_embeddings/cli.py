import multiprocessing
import argparse

from bigcode_embeddings import visualization, skipgram, model_utils


def create_train_parser(subparsers):
    parser = subparsers.add_parser("train", help="train skipgram model to learn embeddings")
    parser.add_argument("inputs", nargs="+")
    parser.add_argument("-o", "--output-dir", required=True)
    parser.add_argument("--vocab-size", type=int, required=True)
    parser.add_argument("--emb-size", type=int, default=100)
    parser.add_argument("--batch-size", type=int, default=1024)
    parser.add_argument("--l2-value", type=float, default=0.0)
    parser.add_argument("--num-sampled", type=int, default=10)
    parser.add_argument("--optimizer", default="adam")
    parser.add_argument("--epochs", type=int, default=5)
    parser.add_argument("--learning-rate", type=float, default=0.001)
    parser.add_argument("--threads-count", type=int, default=multiprocessing.cpu_count())
    parser.add_argument("--no-clipping", action="store_true", default=False)
    parser.add_argument("--checkpoint", default=None)
    return parser

def create_export_parser(subparsers):
    parser = subparsers.add_parser("export", help="exports the embeddings to numpy format")
    parser.add_argument("model", help="path of the trained model")
    parser.add_argument("-o", "--output", required=True, help="location to export the embeddings")


def create_visualize_clusters_parser(subparsers):
    parser = subparsers.add_parser("clusters", help="displays the clustered data in 2D")
    parser.add_argument(
        "-m", "--model-path", required=True, help="path of the trained model")
    parser.add_argument(
        "-l", "--labels-path", required=True, help="path of the vocabulary labels")
    parser.add_argument(
        "-i", "--interactive", default=False, action="store_true", help="uses interactive plot")
    parser.add_argument(
        "-n", "--clusters-count", default=visualization.DEFAULT_CLUSTERS_COUNT, type=int,
        help="number of clusters")
    parser.add_argument(
        "-o", "--output", default=None, help="the file to output the plot")


def create_elbow_graph_parser(subparsers):
    parser = subparsers.add_parser(
        "clusters-elbow-graph",
        help="displays an elbow graph to find the optimal number of clusters")
    parser.add_argument(
        "-m", "--model-path", required=True, help="path of the trained model")
    parser.add_argument(
        "-o", "--output", default=None, help="the file to output the plot")
    parser.add_argument(
        "--max-clusters", default=visualization.DEFAULT_MAX_CLUSTERS, type=int,
        help="the maximum number of clusters to try")


def create_visualize_parser(subparsers):
    visualize_parser = subparsers.add_parser("visualize", help="visualize learned embeddings")
    visualize_subparser = visualize_parser.add_subparsers(dest="subcommand")
    create_visualize_clusters_parser(visualize_subparser)
    create_elbow_graph_parser(visualize_subparser)


def create_parser():
    parser = argparse.ArgumentParser(
        prog="bigcode-embeddings", description="train and visualize embeddings from bigcode")

    subparsers = parser.add_subparsers(dest="command")
    create_visualize_parser(subparsers)
    create_train_parser(subparsers)
    create_export_parser(subparsers)

    return parser


def run_visualize(parser, args):
    if not args.subcommand:
        parser.error("no subcommand provided")
    elif args.subcommand == "clusters":
        visualization.visualize_clusters(args)
    elif args.subcommand == "clusters-elbow-graph":
        visualization.visualize_elbow_graph(args)


def run():
    parser = create_parser()
    args = parser.parse_args()

    if not args.command:
        parser.error("no command provided")
    elif args.command == "visualize":
        run_visualize(parser, args)
    elif args.command == "train":
        skipgram.run(args)
    elif args.command == "export":
        model_utils.export_embeddings(args.model, args.output)
