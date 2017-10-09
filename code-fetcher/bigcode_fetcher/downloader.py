import os.path as path
from concurrent.futures import ThreadPoolExecutor
import subprocess
import logging
import json

from bigcode_fetcher.project import Project


def download_git_project(project, output_dir):
    subprocess.run(["git", "clone", "--depth", "1", project.clone_url, output_dir])


def download_project(project, output_base_dir):
    try:
        output_dir = path.join(output_base_dir, project.full_name)
        if path.isdir(output_dir):
            logging.info("%s already exists", project.full_name)
            return False
        download_git_project(project, output_dir)
        return True
    except Exception as e: # pylint: disable=broad-except
        logging.warning("could not download %s: %s", project.full_name, e)


def download_projects(projects, output_dir):
    with ThreadPoolExecutor() as executor:
        executor.map(lambda p: download_project(p, output_dir), projects)


def load_projects_from_file(input_file):
    with open(input_file, "r") as f:
        return [Project(project) for project in json.load(f)]


def download_projects_command(args):
    projects = load_projects_from_file(args.input_file)
    download_projects(projects, args.output_dir)
