import argparse
from os import path

import pandas as pd


DATA_FILE = "tmp/apc-nid-nsib-anc2-chd0-debug.txt.gz"
LABELS_FILE = "tmp/vocab-nid-apc-labels.tsv"

data = pd.read_csv(DATA_FILE, names=["input", "context"], comment="#")

data

labels = pd.read_csv(LABELS_FILE, delimiter="\t")

labels.loc[[18, 42, 43]]
