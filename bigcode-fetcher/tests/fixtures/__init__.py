from os import path
import json

from bigcode_fetcher.project import Project

FIXTURES_DIR = path.dirname(__file__)
PROJECTS_PATH = path.join(FIXTURES_DIR, "projects.json")


with open(PROJECTS_PATH, "r") as f:
    JSON_PROJECTS = json.load(f)

PROJECTS = [Project(p) for p in JSON_PROJECTS]
